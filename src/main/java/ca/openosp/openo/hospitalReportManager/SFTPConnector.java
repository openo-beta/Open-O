//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.hospitalReportManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import ca.openosp.openo.messenger.data.MsgMessageData;
import org.apache.commons.io.FileUtils;
import ca.openosp.openo.PMmodule.dao.SecUserRoleDao;
import ca.openosp.openo.PMmodule.model.SecUserRole;
import ca.openosp.openo.commn.dao.HrmLogDao;
import ca.openosp.openo.commn.dao.HrmLogEntryDao;
import ca.openosp.openo.commn.dao.SecObjPrivilegeDao;
import ca.openosp.openo.hospitalReportManager.dao.HRMSendingFacilityDao;
import ca.openosp.openo.commn.model.HrmLog;
import ca.openosp.openo.commn.model.HrmLogEntry;
import ca.openosp.openo.commn.model.OscarMsgType;
import ca.openosp.openo.commn.model.SecObjPrivilege;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.PathValidationUtils;
import ca.openosp.openo.utility.SpringUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import ca.openosp.OscarProperties;
import ca.openosp.openo.messenger.data.MsgProviderData;

/**
 * SFTP Connector to interact with servers and return the server's reply/file data.
 */
public class SFTPConnector {

    private static org.apache.logging.log4j.Logger logger = MiscUtils.getLogger();

    private JSch jsch;
    private ChannelSftp cmd;
    private Session sess;
    private Logger fLogger; //file logger

    /** Socket timeout for the sFTP connection, so an unreachable host fails quickly. */
    private static final int CONNECT_TIMEOUT_MS = 30000;

    /** Interval between keepalive messages sent while the connection is idle. */
    private static final int SERVER_ALIVE_INTERVAL_MS = 60000;

    /** CBC ciphers, offered in addition to the JSch defaults. */
    private static final String CBC_CIPHERS = "aes256-cbc,aes192-cbc,aes128-cbc";

    /** How many levels of subfolders below the configured folder are fetched. */
    private static final int MAX_FOLDER_DEPTH = 5;

    private static final String OMD_HRM_USER = OscarProperties.getInstance().getProperty("OMD_HRM_USER");
    private static final String OMD_HRM_IP = OscarProperties.getInstance().getProperty("OMD_HRM_IP");
    private static final int OMD_HRM_PORT = Integer.parseInt(OscarProperties.getInstance().getProperty("OMD_HRM_PORT"));

    //this file needs chmod 444 permissions for the connection to go through
    public static String OMD_directory = OscarProperties.getInstance().getProperty("OMD_directory");
    private static String OMD_keyLocation = OMD_directory + OscarProperties.getInstance().getProperty("OMD_HRM_AUTH_KEY_FILENAME");
    public static final String XSD_ontariomd = OMD_directory + "ontariomd_cds_dt.xsd";
    public static final String XSD_reportmanager = OMD_directory + "report_manager_cds.xsd";

    //where all the daily logs will be saved
    public final String logDirectory = OscarProperties.getInstance().getProperty("OMD_log_directory");

    //root folder for daily downloads
    public static String downloadsDirectory = OscarProperties.getInstance().getProperty("OMD_downloads");

    public final String fileDirectory = OscarProperties.getInstance().getProperty("OMD_stored");

    //set when initialized, to change keys, manually do it in the main constructor
    public static String decryptionKey = null;

    private HrmLogDao hrmLogDao = SpringUtils.getBean(HrmLogDao.class);
    private HrmLogEntryDao hrmLogEntryDao = SpringUtils.getBean(HrmLogEntryDao.class);
    private HRMSendingFacilityDao hrmSendingFacilityDao = SpringUtils.getBean(HRMSendingFacilityDao.class);

    /**
     * String is the providerNo of people who don't want to see anymore messages.
     * This is cleared each time the run succeeds as it would be a new outtage after one success.
     */
    private static HashSet<String> doNotSentMsgForOuttage = new HashSet<String>();

    private HrmLog hrmLog;

    /** Number of files downloaded in the last fetch, keyed by the folder they came from. */
    private Map<String, Integer> downloadedByFolder = new LinkedHashMap<String, Integer>();

    /**
     * This is called for MANUAL download
     *
     * @throws Exception
     */
    public SFTPConnector(LoggedInInfo loggedInInfo) throws Exception {
        this(loggedInInfo, OMD_HRM_IP, OMD_HRM_PORT, OMD_HRM_USER, getOMD_keyLocation(), "Manual");
    }

    public SFTPConnector(LoggedInInfo loggedInInfo, String triggerType) throws Exception {
        this(loggedInInfo, OMD_HRM_IP, OMD_HRM_PORT, OMD_HRM_USER, getOMD_keyLocation(), triggerType);
    }

    /**
     * Main constructor. To change keys, manually set the references below.
     */
    public SFTPConnector(LoggedInInfo loggedInInfo, String host, int port, String user, String keyLocation, String triggerType) throws Exception {

        logger.debug("Host " + host + " port " + port + " user " + user + " keyLocation " + keyLocation);

        hrmLog = new HrmLog();
        hrmLog.setTransactionType(triggerType);
        hrmLog.setStarted(new Date());
        hrmLog.setInitiatingProviderNo(loggedInInfo.getLoggedInProviderNo());
        hrmLogDao.persist(hrmLog);

        String logName = SFTPConnector.getDayMonthYearTimestamp() + ".log";
        String fullLogPath = this.logDirectory + logName;
        FileHandler handler = new FileHandler(fullLogPath, true);
        fLogger = Logger.getLogger("SFTPConnector");
        fLogger.addHandler(handler);

        try {
            jsch = new JSch();
            System.out.println(String.format("[HRM-DEBUG] loading identity %s", keyLocation));
            jsch.addIdentity(keyLocation);
            System.out.println("[HRM-DEBUG] identity loaded");
            sess = jsch.getSession(user, host, port);
            System.out.println(String.format("[HRM-DEBUG] session created for %s@%s:%d", user, host, port));
        } catch (JSchException je) {
            System.out.println(String.format("[HRM-DEBUG] identity or session failed: %s", je.getMessage()));
            hrmLog.setError(je.getMessage());
            hrmLogDao.merge(hrmLog);
            throw je;
        }

        java.util.Properties confProp = new java.util.Properties();
        confProp.put("StrictHostKeyChecking", "no");
        // The HRM sFTP server offers only CBC ciphers, which JSch leaves out of its defaults. They are
        // appended after the defaults so a server that offers CTR or GCM still gets one of those.
        confProp.put("cipher.c2s", JSch.getConfig("cipher.c2s") + "," + CBC_CIPHERS);
        confProp.put("cipher.s2c", JSch.getConfig("cipher.s2c") + "," + CBC_CIPHERS);
        sess.setConfig(confProp);

        // Keeps the session open while downloaded reports are decrypted and parsed, which happens
        // between the last download and the deletion of the files from the server.
        sess.setServerAliveInterval(SERVER_ALIVE_INTERVAL_MS);

        try {
            System.out.println(String.format("[HRM-DEBUG] opening TCP socket to %s:%d, timeout %dms", host, port, CONNECT_TIMEOUT_MS));
            long startedAt = System.currentTimeMillis();
            sess.connect(CONNECT_TIMEOUT_MS);
            System.out.println(String.format("[HRM-DEBUG] authenticated after %dms", System.currentTimeMillis() - startedAt));
            Channel channel = sess.openChannel("sftp");
            channel.connect();
            cmd = (ChannelSftp) channel;
            System.out.println(String.format("[HRM-DEBUG] sftp channel open, remote cwd %s", cmd.pwd()));
            fLogger.info("SFTP connection established with " + host + ":" + port + ". Current path on server is: " + cmd.pwd());
        } catch (JSchException je) {
            System.out.println(String.format("[HRM-DEBUG] connect failed: %s", je.getMessage()));
            hrmLog.setError(je.getMessage());
            hrmLogDao.merge(hrmLog);
            throw je;
        }

        hrmLog.setConnected(true);
        hrmLogDao.merge(hrmLog);
    }

    public static String getOMD_keyLocation() {
        return OMD_keyLocation;

    }

    /**
     * Ensure the specified folder exists within the SFTP download folder. If folder is null, then ensure that the
     * download folder exists.
     *
     * @throws Exception
     */
    private static String prepareForDownload(String folder) throws Exception {

        //ensure the downloads directory exists
        String path = checkFolder(downloadsDirectory);

        //if it's a simple "do i have my downloads folder" check, then we're done!
        //no other folder is specified
        if (folder == null)
            return path;

        //if code gets to here then we're ensuring that specified folder exists within SFTP download folder.
        //-also fixes the beginning if the specified folder already begins with a '/' slash it ignores the slash
        String dir = downloadsDirectory
                + (folder == null ? "" : (folder.charAt(0) == '/' ? folder.substring(1, folder.length() - 1) : folder));

        //return the full path of the existing folder
        return checkFolder(dir);
    }

    /**
     * ls print - issue an 'ls' command and simply print the results to System out (rather than returning a String array
     * of elements listed from command)
     *
     * @param folder
     * @throws SftpException
     */
    public void lsP(String folder) throws SftpException {
        ls(folder, true);
    }

    /**
     * Issue an 'ls' command and return the objects in an array
     *
     * @param folder
     * @return
     * @throws SftpException
     */
    public String[] ls(String folder) throws SftpException {
        return ls(folder, false);
    }

    /**
     * Reduce a configured folder path to the form used against the server. Paths are relative to the
     * home directory of the sFTP account, so "Clinic1", "/Clinic1" and "/Clinic1/" all name the same
     * folder. The home directory itself is returned as ".".
     *
     * @param folder String the folder path as entered on the HRM Configuration page
     * @return String the folder path without leading, trailing or repeated slashes
     */
    public static String normalizeRemoteDir(String folder) {
        if (folder == null) {
            return ".";
        }
        String normalized = folder.trim().replaceAll("/+", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.isEmpty() ? "." : normalized;
    }

    /**
     * Join a folder path and one of its entries.
     *
     * @param folder String the folder path, or "." for the home directory
     * @param name   String the entry listed in that folder
     * @return String the path of the entry relative to the home directory
     */
    private static String remotePath(String folder, String name) {
        return ".".equals(folder) ? name : folder + "/" + name;
    }

    /**
     * Issue an 'ls' command and return the subdirectories of the folder.
     *
     * @param folder String the folder to list
     * @return String[] the names of the subdirectories, without "." and ".."
     * @throws SftpException
     */
    public String[] lsDirectories(String folder) throws SftpException {
        List fileList = cmd.ls(folder);
        List<String> directories = new ArrayList<String>();

        if (fileList != null) {
            for (Object obj : fileList) {
                if (obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
                    LsEntry lsEntry = (LsEntry) obj;
                    String fn = lsEntry.getFilename();

                    if (fn == null || fn.equals(".") || fn.equals("..") || !lsEntry.getAttrs().isDir()) {
                        continue;
                    }
                    // A name carrying a separator would climb out of the folder being walked.
                    if (fn.contains("/") || fn.contains("\\")) {
                        logger.warn("HRM: ignoring subdirectory with a separator in its name: " + fn);
                        continue;
                    }
                    directories.add(fn);
                }
            }
        }

        return directories.toArray(new String[directories.size()]);
    }

    /**
     * Walk a folder and its subdirectories, deepest folders last.
     *
     * @param root     String the configured folder path to start from
     * @param maxDepth int how many levels below the root to descend into
     * @return List<String> the root followed by every subdirectory found under it
     * @throws SftpException
     */
    public List<String> listFoldersToFetch(String root, int maxDepth) throws SftpException {
        List<String> folders = new ArrayList<String>();
        folders.add(root);

        int firstOfLevel = 0;
        for (int depth = 0; depth < maxDepth; depth++) {
            int lastOfLevel = folders.size();
            if (firstOfLevel == lastOfLevel) {
                break;
            }
            for (int i = firstOfLevel; i < lastOfLevel; i++) {
                String folder = folders.get(i);
                for (String child : lsDirectories(folder)) {
                    folders.add(remotePath(folder, child));
                }
            }
            firstOfLevel = lastOfLevel;
        }

        return folders;
    }

    /**
     * Issue an 'ls' command on remote server and exclussively print the values or return them in a String array.
     * Subdirectories are left out of the returned array.
     *
     * @param folder    to issue the 'ls' command on
     * @param printInfo
     * @return String[] the names of the files in the folder, without subdirectories
     * @throws SftpException
     */
    public String[] ls(String folder, boolean printInfo) throws SftpException {
        List fileList = cmd.ls(folder);
        List<String> files = new ArrayList<String>();

        if (fileList != null) {

            logger.debug("ls " + folder);
            for (Object obj : fileList) {

                if (obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
                    LsEntry lsEntry = (LsEntry) obj;

                    //either print or store each element
                    if (printInfo) {
                        logger.debug(lsEntry.getFilename());
                    } else {
                        String fn = lsEntry.getFilename(); //filename
                        if (fn == null || fn.equals(".") || fn.equals("..")) {
                            continue;
                        }
                        // Only the reports in the configured folder are fetched. A subdirectory is
                        // skipped rather than downloaded, which would fail and end the whole fetch.
                        if (lsEntry.getAttrs().isDir()) {
                            logger.info("HRM: skipping subdirectory " + fn + " in " + folder);
                            continue;
                        }
                        files.add(fn);
                    }
                }
            }
        }

        return files.toArray(new String[files.size()]);
    }

    /**
     * Download the contents of the specified directory on the server side. The presumption is made for OMD where the
     * user has to fetch all contents from the user's home directory. Thus a "./" is prepended to each folder requested
     * on the server. If you don't prepend the "./" before the folder directory, JSch will use the "/" directory (root
     * dir).
     *
     * @param serverDirectory directory on server side to fetch contents
     * @return array of full path filenames
     * @throws Exception custom error messages if Java is unable to create a folder in /tmp/oscar-sftp and parent dirs
     */
    public String[] downloadDirectoryContents(String serverDirectory) throws Exception {
        //get the filenames of all files in the directory
        String[] files = ls(serverDirectory);
        String[] fullPathFilenames = new String[files.length];

        String todaysFolderName = SFTPConnector.getDayMonthYearTimestamp();

        fLogger.info("About to download all contents of directory: " + serverDirectory);
        if (files.length == 0) {
            fLogger.info("No files to download from server folder: " + serverDirectory);
            return null;
        }

        // Downloads keep the folder they came from, so two folders holding the same filename do not
        // overwrite one another on the way down.
        String localFolder = ".".equals(serverDirectory)
                ? todaysFolderName : todaysFolderName + File.separator + serverDirectory;
        String fullPath = prepareForDownload(localFolder);

        int i = 0;
        //not too sure whether multiple connections are handled by the JSch library
        //such that multiple calls to "get" has a sync limit until one or more other
        //files have finished downloading.
        for (String file : files) {
            if (file != null) {
                String fullFilePath = fullPath + file;
                cmd.get(remotePath(serverDirectory, file), fullFilePath);
                fullPathFilenames[i++] = fullFilePath;
                fLogger.info("Downloaded File: " + fullFilePath);
                logger.debug("SFTP::Downloaded file: " + fullFilePath);
            }
        }

        return fullPathFilenames;
    }

    /**
     * Given a server-side directory, go in and delete all files
     *
     * @param serverDirectory
     * @throws SftpException
     */
    public void deleteDirectory(String serverDirectory) throws SftpException {
        String[] files = ls(serverDirectory);
        deleteDirectoryContents(serverDirectory, files);
    }

    /**
     * Given a directory and the filenames of the directories (already pre-determined) go in the directory and delete
     * each file.
     *
     * @param serverDirectory the directory onto which to remove contents
     * @param filenames       a String array of filenames of the directory, pre-fetched specifically for the directory.
     * @throws SftpException
     */
    public void deleteDirectoryContents(String serverDirectory, String[] filenames) throws SftpException {
        fLogger.info("About to delete all contents from server directory: " + serverDirectory);
        logger.debug("Deleting contents from directory: " + serverDirectory);
        for (String file : filenames) {
            if (file != null) {
                logger.debug("About to delete server file " + file);
                if (file.indexOf("/") != -1) {
                    file = file.substring(file.lastIndexOf("/") + 1, file.length());
                    logger.debug("file to delete is now " + file);
                }
                try {
                    cmd.rm(remotePath(serverDirectory, file));
                } catch (SftpException e) {
                    logger.error("Error deleting file", e);
                }

                fLogger.info("Deleted file " + file + " from server");
                logger.debug("Deleted server file " + file);
            }
        }

    }

    public String[] decryptFiles(String[] fullPaths) throws Exception {
        return decryptFiles(fullPaths, getDecryptionKey());
    }

    /**
     * Given a String array of absolute filenames of encrypted files, proceed to decrypt them in today's folder under
     * the specified folder below.
     *
     * @param fullPaths
     * @throws Exception
     */
    public String[] decryptFiles(String[] fullPaths, String decryptionKey) throws Exception {
        if (fullPaths.length == 0)
            return null;

        String[] decryptedFilePaths = new String[fullPaths.length];


        //placed under each daily's folder for all files needing decryption to store the decrypted version
        String decryptedFolderName = "decrypted";
        //ensure that the given folder exists (by trying to create it if it dne)
        //return the full path with last slash always there
        String saveToDirectoryFullPath = prepareForDownload(getDayMonthYearTimestamp() + "/" + decryptedFolderName);

        //we'll get each file's contents in a string then dump that onto a file
        String decryptedContent = null;
        String filename = "";

        FileWriter handler = null;
        BufferedWriter out = null;
        for (int x = 0; x < fullPaths.length; x++) {
            String sfile = fullPaths[x];
            if (sfile == null)
                continue;

            try {
                decryptedContent = decryptFile(sfile, decryptionKey);
                filename = sfile.substring(sfile.lastIndexOf("/"));
                String newFullPath = saveToDirectoryFullPath + filename;
                handler = new FileWriter(newFullPath);
                out = new BufferedWriter(handler);
                out.write(decryptedContent);
                decryptedFilePaths[x] = newFullPath;
            } catch (Exception e) {
                //Don't want this to fail on all other files in the directory just because one doesn't decrypt;
                logger.error("Error decrypting file - " + sfile);
                decryptedFilePaths[x] = null;
            } finally {
                if (out != null) out.close();
                if (handler != null) handler.close();
            }
        }

        return decryptedFilePaths;
    }

    public String decryptFile(String fullPath) throws Exception {
        return decryptFile(fullPath, null);
    }

    /**
     * Given the absolute path of an encrypted file, decrypt the file using the specified AES key at the top. Return the
     * string value of the decrypted file.
     *
     * @param fullPath
     * @return
     * @throws Exception
     */
    public String decryptFile(String fullPath, String decryptionKey) throws Exception {

        if (decryptionKey == null || decryptionKey.trim().isEmpty()) {
            decryptionKey = OscarProperties.getInstance().getProperty("OMD_HRM_DECRYPTION_KEY");
        }
        if (decryptionKey == null || decryptionKey.trim().isEmpty()) {
            throw new IllegalStateException("HRM decryption key is not configured. Set it on the HRM Configuration page.");
        }
        logger.info("About to decrypt: " + fullPath);
        File encryptedFile = new File(fullPath);
        if (!encryptedFile.exists()) {
            throw new Exception("Could not find file '" + fullPath + "' to decrypt.");
        }

        int fileLength = (int) encryptedFile.length();
        byte[] fileInBytes = new byte[fileLength];
        FileInputStream fin = new FileInputStream(encryptedFile);
        try {
            fin.read(fileInBytes);
        } finally {
            fin.close();
        }

        byte keyBytes[] = toHex(decryptionKey);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decode = cipher.doFinal(fileInBytes);

        return new String(decode);
    }

    /**
     * Close channels, disconnect sessions, release/close file handlers.
     */
    public void close() {
        if (cmd != null) {
            cmd.exit();
        }
        if (sess != null) {
            sess.disconnect();
        }
        if (fLogger != null && fLogger.getHandlers() != null && fLogger.getHandlers().length > 0) {
            fLogger.getHandlers()[0].close();
        }
    }

    /********************************************************/
    /////////////////// HELPERS / STATIC /////////////////////

    /********************************************************/

    public static String getDayMonthYearTimestamp() {
        Calendar cal = Calendar.getInstance();

        String day = cal.get(Calendar.DAY_OF_MONTH) + "";
        if (day.length() == 1)
            day = "0" + day;

        String month = (cal.get(Calendar.MONTH) + 1) + "";
        if (month.length() == 1)
            month = "0" + month;

        String year = cal.get(Calendar.YEAR) + "";

        return day + month + year;
    }

    /**
     * Check that the given folder exists, if it doesn't exist, create it. Object method for convenience.
     *
     * @param fullPath
     * @throws Exception
     */
    private static String checkFolder(String fullPath) throws Exception {
        return SFTPConnector.ensureFolderExists(fullPath);
    }

    /**
     * Ensure that the given folder exists by creating it if it isn't present.
     * <p>
     * Static method so other external Classes may use this feature.
     *
     * @param fullPath
     * @throws Exception
     */
    public static String ensureFolderExists(String fullPath) throws Exception {
        File tmpFolder = new File(fullPath);
        if (!tmpFolder.exists()) {
            boolean res = tmpFolder.mkdirs();
            if (!res)
                throw new Exception("Unable to create folder " + tmpFolder.getAbsolutePath()
                        + " required for SFTP operations. Please check permissions.");
        }

        return tmpFolder.getAbsolutePath() + "/";
    }

    public static byte[] toHex(String encoded) {
        if ((encoded.length() % 2) != 0)
            throw new IllegalArgumentException("Input string must contain an even number of characters");

        final byte result[] = new byte[encoded.length() / 2];
        final char enc[] = encoded.toCharArray();
        for (int i = 0; i < enc.length; i += 2) {
            StringBuilder curr = new StringBuilder(2);
            curr.append(enc[i]).append(enc[i + 1]);
            result[i / 2] = (byte) Integer.parseInt(curr.toString(), 16);
        }
        return result;

    }

    /********************************************************/
    //////////////////Auto Fetcher////////////////////////
    /**
     * @throws Exception
     ******************************************************/

    private static boolean isAutoFetchRunning = false;

    public static boolean isFetchRunning() {
        return SFTPConnector.isAutoFetchRunning;
    }


    public synchronized void startAutoFetch(LoggedInInfo loggedInInfo) {
        startAutoFetch(loggedInInfo, OscarProperties.getInstance().getProperty("OMD_HRM_REMOTE_DIR"));
    }

    /*
     * Called by HRMDownloadJob (the scheduled auto-poll) and by the manual fetch on hospitalReportManager.jsp.
     */
    public synchronized boolean startAutoFetch(LoggedInInfo loggedInInfo, String remoteDir) {
        if (remoteDir == null || remoteDir.trim().isEmpty()) {
            throw new IllegalStateException("HRM remote folder path is not configured. Set it on the HRM Configuration page.");
        }

        boolean seenError = false;

        if (!isAutoFetchRunning) {
            SFTPConnector.isAutoFetchRunning = true;
            logger.info("HRM: starting auto fetch");

            logger.info("HRM: remoteDir:" + remoteDir);

            downloadedByFolder = new LinkedHashMap<String, Integer>();

            try {

                String root = normalizeRemoteDir(remoteDir);
                List<String> folders = listFoldersToFetch(root, MAX_FOLDER_DEPTH);
                System.out.println(String.format("[HRM-DEBUG] walking %s found %d folder(s): %s",
                        root, folders.size(), folders));

                // The folder each downloaded file came from, so it can be deleted from that folder
                // once it has been stored.
                Map<String, String> folderByLocalFile = new LinkedHashMap<String, String>();

                for (String folder : folders) {
                    String[] downloaded = downloadDirectoryContents(folder);

                    int downloadedFromFolder = 0;
                    if (downloaded != null) {
                        for (String localFilePath : downloaded) {
                            if (localFilePath != null) {
                                folderByLocalFile.put(localFilePath, folder);
                                downloadedFromFolder++;
                            }
                        }
                    }

                    downloadedByFolder.put(folder, downloadedFromFolder);
                    fLogger.info("Downloaded " + downloadedFromFolder + " file(s) from " + folder);
                    logger.info("HRM: downloaded " + downloadedFromFolder + " file(s) from " + folder);
                }

                System.out.println(String.format("[HRM-DEBUG] downloaded %d file(s) in total", getTotalDownloaded()));

                hrmLog.setDownloadedFiles(true);
                hrmLog.setNumFilesDownloaded(getTotalDownloaded());
                hrmLogDao.merge(hrmLog);

                if (folderByLocalFile.isEmpty()) {
                    System.out.println("[HRM-DEBUG] nothing on the server, finishing");
                    return true;
                }

                // Remote filenames of the reports that reached the document directory, kept against
                // the folder they came from. These are the only files removed from the server;
                // anything that failed earlier is left for the next fetch to retry.
                Map<String, List<String>> storedByFolder = new LinkedHashMap<String, List<String>>();

                for (Map.Entry<String, String> download : folderByLocalFile.entrySet()) {

                    String encryptedFile = download.getKey();
                    String sourceFolder = download.getValue();

                    HrmLogEntry hrmLogEntry = new HrmLogEntry();
                    hrmLogEntry.setHrmLogId(hrmLog.getId());
                    hrmLogEntry.setEncryptedFileName(encryptedFile);
                    hrmLogEntryDao.persist(hrmLogEntry);

                    String decryptedContent = null;
                    try {
                        decryptedContent = decryptFile(encryptedFile, decryptionKey);
                        hrmLogEntry.setDecrypted(true);
                        hrmLogEntryDao.merge(hrmLogEntry);

                        String decryptedFileName = saveDecryptedData(encryptedFile, decryptedContent);
                        hrmLogEntry.setDecryptedFileName(decryptedFileName);
                        hrmLogEntryDao.merge(hrmLogEntry);

                        String filename = copyFileToDocumentDir(loggedInInfo, decryptedFileName);
                        hrmLogEntry.setFilename(filename);
                        hrmLogEntryDao.merge(hrmLogEntry);

                        if (filename != null) {

                            List<String> stored = storedByFolder.get(sourceFolder);
                            if (stored == null) {
                                stored = new ArrayList<String>();
                                storedByFolder.put(sourceFolder, stored);
                            }
                            stored.add(new File(encryptedFile).getName());

                            List<Throwable> errors = new ArrayList<Throwable>();
                            HRMReport report = HRMReportParser.parseReport(loggedInInfo, filename, errors);
                            if (report != null) {
                                hrmLogEntry.setParsed(true);
                                hrmLogEntry.setRecipientId(report.getDeliverToUserId());
                                hrmLogEntry.setRecipientName(report.getDeliveryToUserIdFormattedName());
                                hrmLogEntryDao.merge(hrmLogEntry);

                                HRMReportParser.addReportToInbox(loggedInInfo, report);

                                hrmLogEntry.setDistributed(true);
                                String sfId = report.getSendingFacilityId();
                                if (sfId != null && !sfId.trim().isEmpty()
                                        && hrmSendingFacilityDao.findBySendingFacilityId(sfId) == null) {
                                    hrmLogEntry.setError("WARNING: Unregistered Sending Facility: " + sfId);
                                }
                                hrmLogEntryDao.merge(hrmLogEntry);

                            } else {
                                for (Throwable e : errors) {
                                    hrmLogEntry.setError(e.getMessage());
                                    seenError = true;
                                }
                                hrmLogEntryDao.merge(hrmLogEntry);

                            }

                        }

                    } catch (Exception e) {
                        hrmLogEntry.setError(e.getMessage());
                        hrmLogEntryDao.merge(hrmLogEntry);
                        notifyHrmError(loggedInInfo, encryptedFile.substring(encryptedFile.lastIndexOf("/") + 1) + ": " + e.getMessage());
                        //	throw e;
                    }


                }

                int removed = 0;
                for (Map.Entry<String, List<String>> stored : storedByFolder.entrySet()) {
                    deleteDirectoryContents(stored.getKey(), stored.getValue().toArray(new String[0]));
                    removed += stored.getValue().size();
                }

                if (removed > 0) {
                    System.out.println("[HRM-DEBUG] server cleanup: " + removed
                            + " removed, " + (folderByLocalFile.size() - removed) + " kept for retry");

                    hrmLog.setDeleted(true);
                    hrmLogDao.merge(hrmLog);
                }

                logger.debug("Closed SFTP connection");
                logger.debug("Clearing doNotSend list");
                doNotSentMsgForOuttage.clear();
            } catch (Exception e) {
                seenError = true;
                logger.error("Couldn't perform SFTP fetch for HRM - notifying user of failure", e);
                notifyHrmError(loggedInInfo, e.getMessage());
            } finally {
                close();
                SFTPConnector.isAutoFetchRunning = false;
            }

        } else {
            logger.warn("There is currently an HRM fetch running -- will not run another until it has completed or timed out.");
        }
        return !seenError;
    }

    /**
     * Number of files downloaded by the last fetch, keyed by the folder they were downloaded from.
     * Folders that held no files are listed with a count of zero.
     *
     * @return Map<String, Integer> the folders walked, each with the number of files taken from it
     */
    public Map<String, Integer> getDownloadedByFolder() {
        if (downloadedByFolder == null) {
            return java.util.Collections.emptyMap();
        }
        return java.util.Collections.unmodifiableMap(downloadedByFolder);
    }

    /**
     * @return int the number of files downloaded by the last fetch across every folder
     */
    public int getTotalDownloaded() {
        int total = 0;
        for (Integer count : getDownloadedByFolder().values()) {
            total += count;
        }
        return total;
    }

    protected String saveDecryptedData(String encryptedFilePath, String decryptedFileContents) throws IOException {
        String decryptedFolderName = "decrypted";
        String path = encryptedFilePath.substring(0, encryptedFilePath.lastIndexOf("/"));
        String filename = encryptedFilePath.substring(encryptedFilePath.lastIndexOf("/") + 1);

        String saveToDirectoryFullPath = path + File.separator + decryptedFolderName + File.separator + filename.replaceAll("_encrypted", "");

        FileUtils.write(new File(saveToDirectoryFullPath), decryptedFileContents);


        return saveToDirectoryFullPath;
    }

    protected String copyFileToDocumentDir(LoggedInInfo loggedInInfo, String f) throws IOException {
        String destDir = OscarProperties.getInstance().getDocumentDirectory();
        String result = null;

        if (f != null) {
            FileUtils.copyFileToDirectory(new File(f), new File(destDir));
            result = new File(destDir, new File(f).getName()).getAbsolutePath();
        }

        return result;
    }

    protected static String[] copyFilesToDocumentDir(LoggedInInfo loggedInInfo, String[] paths) {
        String destDir = OscarProperties.getInstance().getDocumentDirectory();
        List<String> results = new ArrayList<String>();

        for (int x = 0; x < paths.length; x++) {
            String f = paths[x];
            if (f != null) {
                try {
                    FileUtils.copyFileToDirectory(new File(f), new File(destDir));
                    results.add(new File(destDir, new File(f).getName()).getAbsolutePath());
                } catch (IOException e) {
                    logger.error("Error copying HRM file. Will not be viewable from Inbox!", e);
                    notifyHrmError(loggedInInfo, "Failed to copy HRM file to DOCUMENT_DIR. Please contact admin (" + f + ")");
                }
            }
        }

        return results.toArray(new String[results.size()]);
    }

    public static int parsePort(String port) {
        if (port == null || port.trim().isEmpty()) {
            throw new IllegalStateException("HRM port is not configured. Set it on the HRM Configuration page.");
        }
        try {
            return Integer.parseInt(port.trim());
        } catch (NumberFormatException nfe) {
            throw new IllegalStateException("HRM port '" + port + "' is not a valid number.");
        }
    }

    /**
     * Validates the directory that holds the sFTP private key, as configured by the OMD_DIRECTORY
     * property or derived from the document directory when that property is unset.
     * <p>
     * A relative directory would resolve against the servlet container's working directory, and the
     * filesystem root is never a legitimate place to keep a private key. Both are rejected here so
     * that reading and writing the key fail with the same explanation.
     *
     * @param privateKeyDirectory String the configured or derived directory
     * @return String the directory with surrounding whitespace removed
     * @throws IllegalStateException if the directory is unset, relative, or resolves to the filesystem root
     */
    public static String requirePrivateKeyDirectory(String privateKeyDirectory) {
        if (privateKeyDirectory == null || privateKeyDirectory.trim().isEmpty()) {
            throw new IllegalStateException("HRM private key directory is not configured. Set the OMD_DIRECTORY property, or leave it unset to use the default location under the document directory.");
        }

        String trimmed = privateKeyDirectory.trim();
        File dir = new File(trimmed);
        if (!dir.isAbsolute()) {
            throw new IllegalStateException("HRM private key directory '" + trimmed + "' is not an absolute path. Correct the OMD_DIRECTORY property.");
        }

        File resolved;
        try {
            resolved = dir.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalStateException("HRM private key directory '" + trimmed + "' could not be resolved. Correct the OMD_DIRECTORY property.");
        }
        if (resolved.getParentFile() == null) {
            throw new IllegalStateException("HRM private key directory resolves to the filesystem root. Correct the OMD_DIRECTORY property to the directory that holds the sFTP private key, or leave it unset to use the default location under the document directory.");
        }

        return trimmed;
    }

    public static String requirePrivateKeyPath(String privateKeyDirectory, String privateKeyFile) {
        if (privateKeyFile == null || privateKeyFile.trim().isEmpty()) {
            throw new IllegalStateException("HRM private key file is not configured. Upload one on the HRM Configuration page.");
        }
        File dir = new File(requirePrivateKeyDirectory(privateKeyDirectory));
        File keyFile = new File(dir, privateKeyFile);
        PathValidationUtils.validateExistingPath(keyFile, dir);
        return keyFile.getAbsolutePath();
    }

    public static void notifyHrmError(LoggedInInfo loggedInInfo, String errorMsg) {
        String errorDetails = (errorMsg == null || errorMsg.trim().isEmpty())
                ? "No additional error details were provided. See the HRM log for more information."
                : errorMsg;
        String message = "OpenO attempted to perform a fetch of HRM data at " + new Date() + " but there was an error during the task.\n\nSee below and HRM log for further details:\n" + errorDetails
                + "\n\nTo stop receiving these notifications for the current outage, open the \"Hospital Report Manager (HRM) Status\" page under Administration and click \"I don't want to receive any more HRM outage messages for this outage instance\". Notifications resume automatically after the next successful fetch.";
        notifyHrmAdmin(loggedInInfo, "HRM Retrieval Error", message);
    }

    public static void notifyHrmAdmin(LoggedInInfo loggedInInfo, String subject, String message) {
        HashSet<String> sendToProviderList = new HashSet<String>();

        if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
            String providerNoTemp = loggedInInfo.getLoggedInProviderNo();
            if (!doNotSentMsgForOuttage.contains(providerNoTemp)) sendToProviderList.add(providerNoTemp);
        }

        //load all _hrm.administrators
        SecObjPrivilegeDao secObjPrivilegeDao = SpringUtils.getBean(SecObjPrivilegeDao.class);
        SecUserRoleDao secUserRoleDao = SpringUtils.getBean(SecUserRoleDao.class);

        for (SecObjPrivilege sop : secObjPrivilegeDao.findByObjectName("_hrm.administrator")) {
            if ("x".equals(sop.getPrivilege()) || "w".equals(sop.getPrivilege()) || "r".equals(sop.getPrivilege())) {
                for (SecUserRole sur : secUserRoleDao.getSecUserRolesByRoleName(sop.getId().getRoleUserGroup())) {
                    if (sur.getActive()) {

                        if (!doNotSentMsgForOuttage.contains(sur.getProviderNo())) {
                            sendToProviderList.add(sur.getProviderNo());
                        }
                    }
                }

            }
        }

        if (sendToProviderList.size() == 0) {
            String providerNoTemp = "999998";
            if (!doNotSentMsgForOuttage.contains(providerNoTemp)) {
                sendToProviderList.add(providerNoTemp);
            }
        }

        // no one wants to hear about the problem
        if (sendToProviderList.size() == 0) {
            return;
        }


        MsgMessageData messageData = new MsgMessageData();

        ArrayList<MsgProviderData> sendToProviderListData = new ArrayList<MsgProviderData>();
        for (String providerNo : sendToProviderList) {
            MsgProviderData mpd = new MsgProviderData();
            mpd.getId().setContactId(providerNo);
            mpd.getId().setClinicLocationNo(145);
            sendToProviderListData.add(mpd);
            logger.info("HRM admin notify [" + subject + "]: " + providerNo);
        }

        String sentToString = messageData.createSentToString(sendToProviderListData);
        messageData.sendMessage2(message, subject, "System", sentToString, "-1", sendToProviderListData, null, null, OscarMsgType.GENERAL_TYPE);
    }

    /**
     * adds the currently logged in user to the do not send anymore messages for this outtage list
     */
    public static void addMeToDoNotSendList(LoggedInInfo loggedInInfo) {
        if (loggedInInfo != null && loggedInInfo.getLoggedInProvider() != null) {
            String providerNo = loggedInInfo.getLoggedInProviderNo();
            boolean alreadyPresent = doNotSentMsgForOuttage.contains(providerNo);
            doNotSentMsgForOuttage.add(providerNo);
            logger.info("HRM do-not-send list: provider " + providerNo + (alreadyPresent ? " was already suppressed" : " added") + " (current size: " + doNotSentMsgForOuttage.size() + ")");
        } else {
            logger.warn("HRM do-not-send list: dismiss requested but no logged-in provider in session; nothing suppressed");
        }
    }

    public static void setOMD_keyLocation(String oMD_keyLocation) {
        OMD_keyLocation = oMD_keyLocation;
    }

    public static String getDownloadsDirectory() {
        String dd = downloadsDirectory;
        if (dd == null || dd.equals("")) {
            dd = "webapps/OscarDocument/hrm/sftp_downloads/";
            return dd;

        } else {
            return downloadsDirectory;
        }

    }

    public static void setDownloadsDirectory(String downloadsDir) {
        downloadsDirectory = downloadsDir;
    }

    public static String getDecryptionKey() {
        return decryptionKey;
    }

    public static void setDecryptionKey(String decryptKey) {
        decryptionKey = decryptKey;
    }

}
/*
class MyUserInfo implements UserInfo {

	@Override
	public String getPassphrase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPassword() {
		return "password";
	}

	@Override
	public boolean promptPassword(String message) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean promptPassphrase(String message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean promptYesNo(String message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void showMessage(String message) {
		// TODO Auto-generated method stub
		
	}
	
}
*/
