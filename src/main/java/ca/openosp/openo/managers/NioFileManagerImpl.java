//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * Copyright (c) 2015-2019. The Pharmacists Clinic, Faculty of Pharmaceutical Sciences, University of British Columbia. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * The Pharmacists Clinic
 * Faculty of Pharmaceutical Sciences
 * University of British Columbia
 * Vancouver, British Columbia, Canada
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package ca.openosp.openo.managers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.ServletContext;

import ca.openosp.OscarProperties;
import org.apache.logging.log4j.Logger;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.fonts.FontMappings;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * the NioFileManager handles all file input and output of all OscarDocument files
 * by providing several convenience utilities.
 * <p>
 * One goal is to eliminate the use of "OscarProperties.getInstance().getProperty("DOCUMENT_DIR")"
 * in every single page of OSCAR code.
 */
@Service
public class NioFileManagerImpl implements NioFileManager {

    @Autowired(required=false)
    private ServletContext context;

    @Autowired
    private SecurityInfoManager securityInfoManager;

    private static Logger log = MiscUtils.getLogger();
    private static final String DOCUMENT_CACHE_DIRECTORY = "document_cache";
    private static final String TEMP_PDF_DIRECTORY = "tempPDF";
    private static final String DEFAULT_FILE_SUFFIX = "pdf";
    private static final String DEFAULT_GENERIC_TEMP = "tempDirectory";
    private static final String BASE_DOCUMENT_DIR = OscarProperties.getInstance().getProperty("BASE_DOCUMENT_DIR");

    public Path hasCacheVersion2(LoggedInInfo loggedInInfo, String filename, Integer pageNum) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_edoc", SecurityInfoManager.READ, "")) {
            throw new RuntimeException("Read Access Denied _edoc for provider " + loggedInInfo.getLoggedInProviderNo());
        }

        // Validate input parameters
        if (filename == null || filename.trim().isEmpty()) {
            log.error("Invalid filename provided: null or empty");
            return null;
        }
        
        if (pageNum == null || pageNum < 1) {
            log.error("Invalid page number provided: " + pageNum);
            return null;
        }

        // Sanitize the filename to prevent path traversal
        String sanitizedFilename = sanitizeFileName(filename);
        
        // Additional validation after sanitization
        if (sanitizedFilename.isEmpty() || "invalid_filename".equals(sanitizedFilename)) {
            log.error("Filename failed sanitization: " + filename);
            return null;
        }

        Path documentCacheDir = getDocumentCacheDirectory(loggedInInfo);
        Path normalizedCacheDir = documentCacheDir.normalize().toAbsolutePath();
        
        // Construct the cache filename securely
        String cacheFileName = sanitizedFilename + "_" + pageNum + ".png";
        
        // Validate the cache filename doesn't contain any path separators
        if (cacheFileName.contains("/") || cacheFileName.contains("\\") || cacheFileName.contains("..")) {
            log.error("Invalid characters in cache filename: " + cacheFileName);
            return null;
        }
        
        // Safely construct the path using resolve() with the sanitized filename
        Path outfile = normalizedCacheDir.resolve(cacheFileName);
        outfile = outfile.normalize().toAbsolutePath();
        
        // Verify the file is within the cache directory (defense in depth)
        if (!outfile.startsWith(normalizedCacheDir)) {
            log.error("Path traversal attempt detected in hasCacheVersion2: " + filename);
            return null;
        }
        
        // Additional check: ensure the resolved path is not a directory
        if (Files.exists(outfile) && Files.isDirectory(outfile)) {
            log.error("Resolved path is a directory, not a file: " + outfile);
            return null;
        }

        if (!Files.exists(outfile)) {
            outfile = null;
        }
        return outfile;
    }

    public Path getDocumentCacheDirectory(LoggedInInfo loggedInInfo) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_edoc", SecurityInfoManager.READ, "")) {
            throw new RuntimeException("Read Access Denied _edoc for provider " + loggedInInfo.getLoggedInProviderNo());
        }

        Path cacheDir = Paths.get(BASE_DOCUMENT_DIR, context.getContextPath(), DOCUMENT_CACHE_DIRECTORY);

        if (!Files.exists(cacheDir)) {
            try {
                Files.createDirectory(cacheDir);
            } catch (IOException e) {
                log.error("Error creating DocumentCache directory", e);
            }
        }
        return cacheDir;
    }

    /**
     * First checks to see if a cache version is already available.  If one is not available then a
     * new cached version is created.
     * <p>
     * Returns a file path to the cached version of the given PDF
     */
    public Path createCacheVersion2(LoggedInInfo loggedInInfo, String sourceDirectory, String filename, Integer pageNum) {

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_edoc", SecurityInfoManager.WRITE, "")) {
            throw new RuntimeException("Read Access Denied _edoc for provider " + loggedInInfo.getLoggedInProviderNo());
        }

        // Sanitize the filename to prevent path traversal
        String sanitizedFilename = sanitizeFileName(filename);

        Path cacheFilePath = hasCacheVersion2(loggedInInfo, sanitizedFilename, pageNum);

        /*
         * create a new cache file if an existing cache file is not returned.
         */
        if (cacheFilePath == null) {
            // Validate source directory input
            if (sourceDirectory == null || sourceDirectory.trim().isEmpty()) {
                log.error("Invalid source directory: null or empty");
                return null;
            }
            
            // Define the allowed base directory for documents
            Path baseDocumentPath = Paths.get(BASE_DOCUMENT_DIR).normalize().toAbsolutePath();
            
            // Validate and normalize the source directory - ensure it's within the allowed base path
            Path normalizedSourceDir;
            try {
                normalizedSourceDir = Paths.get(sourceDirectory).normalize().toAbsolutePath();
                
                // Ensure the source directory is within the allowed base document directory
                if (!normalizedSourceDir.startsWith(baseDocumentPath)) {
                    log.error("Source directory is outside allowed base path: " + sourceDirectory);
                    return null;
                }
                
                // Verify the source directory exists and is actually a directory
                if (!Files.exists(normalizedSourceDir) || !Files.isDirectory(normalizedSourceDir)) {
                    log.error("Source directory does not exist or is not a directory: " + sourceDirectory);
                    return null;
                }
            } catch (Exception e) {
                log.error("Invalid source directory path: " + sourceDirectory, e);
                return null;
            }
            
            Path sourceFile = normalizedSourceDir.resolve(sanitizedFilename).normalize().toAbsolutePath();
            
            // Ensure source file is within the source directory
            if (!sourceFile.startsWith(normalizedSourceDir)) {
                log.error("Path traversal attempt in source file: " + filename);
                return null;
            }
            
            Path documentCacheDir = getDocumentCacheDirectory(loggedInInfo);
            Path normalizedCacheDir = documentCacheDir.normalize().toAbsolutePath();
            cacheFilePath = normalizedCacheDir.resolve(sanitizedFilename + "_" + pageNum + ".png");
            cacheFilePath = cacheFilePath.normalize().toAbsolutePath();
            
            // Verify the cache file path is within the cache directory
            if (!cacheFilePath.startsWith(normalizedCacheDir)) {
                log.error("Path traversal attempt in cache file creation: " + filename);
                return null;
            }

            PdfDecoder decode_pdf = new PdfDecoder(true);
            FontMappings.setFontReplacements();
            decode_pdf.useHiResScreenDisplay(true);
            decode_pdf.setExtractionMode(0, 96, 96 / 72f);

            try (InputStream inputStream = Files.newInputStream(sourceFile)) {
                decode_pdf.openPdfFileFromInputStream(inputStream, false);
                BufferedImage image_to_save = decode_pdf.getPageAsImage(pageNum);
                decode_pdf.getObjectStore().saveStoredImage(cacheFilePath.toString(), image_to_save, true, false, "png");
            } catch (IOException e) {
                log.error("Error", e);
            } catch (PdfException e) {
                log.error("Error", e);
            } finally {
                decode_pdf.flushObjectValues(true);
                decode_pdf.closePdfFile();
            }
        }

        return cacheFilePath;

    }

    /**
     * Remove the given file from the cache directory.
     * This is highly recommended function for temporary document preview images.
     *
     * @param loggedInInfo
     * @param fileName
     */
    public final boolean removeCacheVersion(LoggedInInfo loggedInInfo, final String fileName) {

        // Validate input to prevent null pointer exceptions
        if (fileName == null || fileName.trim().isEmpty()) {
            log.error("Invalid fileName provided: null or empty");
            return false;
        }

        // Sanitize the filename - remove any path traversal attempts
        String sanitizedFileName = sanitizeFileName(fileName);
        
        Path documentCacheDir = getDocumentCacheDirectory(loggedInInfo);
        
        try {
            // Get the normalized cache directory first
            Path normalizedCacheDir = documentCacheDir.normalize().toAbsolutePath();
            
            // Construct the file path safely using only the filename part
            // This prevents absolute paths or path traversal sequences from being used
            Path cacheFilePath = normalizedCacheDir.resolve(sanitizedFileName);
            
            // Normalize the constructed path
            Path normalizedPath = cacheFilePath.normalize().toAbsolutePath();
            
            // Double-check that the file is within the cache directory after normalization
            if (!normalizedPath.startsWith(normalizedCacheDir)) {
                log.error("Attempt to delete file outside of cache directory: " + fileName);
                throw new SecurityException("Path traversal attempt detected");
            }
            
            // Additional check - ensure we're not deleting directories
            if (Files.isDirectory(normalizedPath)) {
                log.error("Attempt to delete a directory instead of a file: " + fileName);
                return false;
            }
            
            return Files.deleteIfExists(normalizedPath);
        } catch (SecurityException e) {
            log.error("Security violation while attempting to delete cache file: " + fileName, e);
            throw e; // Re-throw security exceptions
        } catch (IOException e) {
            log.error("Error while deleting temp cache image file " + fileName, e);
        }
        return false;
    }
    
    /**
     * Sanitize filename to prevent path traversal attacks.
     * Removes any directory separators and path traversal sequences.
     *
     * @param fileName the filename to sanitize
     * @return sanitized filename with only the base name
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "";
        }
        
        // First, get just the filename component (removes any path)
        Path path = Paths.get(fileName);
        String baseName = path.getFileName() != null ? path.getFileName().toString() : "";
        
        // Remove any remaining path traversal sequences or special characters
        // that could be used maliciously
        baseName = baseName.replaceAll("\\.\\.", "")  // Remove ..
                          .replaceAll("[\\\\/]", "")   // Remove any slashes
                          .replaceAll("\\$", "")        // Remove $
                          .replaceAll("~", "");         // Remove ~
        
        // Additional validation - ensure the filename is not empty after sanitization
        if (baseName.trim().isEmpty()) {
            log.warn("Filename became empty after sanitization: " + fileName);
            return "invalid_filename";
        }
        
        return baseName;
    }

    /**
     * Save a file to the temporary directory from ByteArrayOutputStream
     *
     * @throws IOException
     */
    public Path saveTempFile(final String fileName, ByteArrayOutputStream os, String fileType) throws IOException {
        Path directory = Files.createTempDirectory(TEMP_PDF_DIRECTORY + System.currentTimeMillis());
        if (fileType == null) {
            fileType = DEFAULT_FILE_SUFFIX;
        }
        Path file = Files.createFile(Paths.get(directory.toString(), String.format("%1$s.%2$s", fileName, fileType)));
        return Files.write(file, os.toByteArray());
    }

    public final Path saveTempFile(final String fileName, ByteArrayOutputStream os) throws IOException {
        return saveTempFile(fileName, os, null);
    }

    public Path createTempFile(final String fileName, ByteArrayOutputStream os) throws IOException {
        Path directory = Files.createTempDirectory(DEFAULT_GENERIC_TEMP + System.currentTimeMillis());
        Path file = Files.createFile(Paths.get(directory.toString(), fileName));
        Files.write(file, os.toByteArray());
        return directory;
    }

    /**
     * Delete a temp file. Do this often.
     *
     * @param fileName
     */
    public final boolean deleteTempFile(final String fileName) {
        try {
            // Get the system temp directory as the base directory
            Path systemTempDir = Paths.get(System.getProperty("java.io.tmpdir")).toRealPath().normalize();
            
            // Parse and normalize the provided file path
            Path tempfile = Paths.get(fileName).normalize().toAbsolutePath();
            
            // Resolve to real path to handle symlinks
            if (Files.exists(tempfile)) {
                tempfile = tempfile.toRealPath();
            }
            
            // Validate that the file is within the system temp directory
            if (!tempfile.startsWith(systemTempDir)) {
                log.error("Attempt to delete file outside of temp directory: " + fileName);
                throw new SecurityException("Path traversal attempt detected");
            }
            
            // Additional check: ensure the file is actually a temporary file created by this system
            // Check if it's in one of our known temp subdirectories
            String filePathStr = tempfile.toString();
            boolean isValidTempFile = filePathStr.contains(TEMP_PDF_DIRECTORY) || 
                                     filePathStr.contains(DEFAULT_GENERIC_TEMP) ||
                                     tempfile.getParent().equals(systemTempDir);
            
            if (!isValidTempFile) {
                log.error("Attempt to delete non-temporary file: " + fileName);
                return false;
            }
            
            return Files.deleteIfExists(tempfile);
        } catch (SecurityException e) {
            log.error("Security violation while attempting to delete file: " + fileName, e);
            throw e; // Re-throw security exceptions
        } catch (IOException e) {
            log.error("Error while deleting temp cache image file " + fileName, e);
        }
        return false;
    }


    /**
     * retrieve given filename from Oscar's document directory path as defined in
     * Oscar properties.
     * Filename string in File out
     */
    public File getOscarDocument(String fileName) {
        // Sanitize the filename to prevent path traversal
        String sanitizedFileName = sanitizeFileName(fileName);
        
        Path documentDir = Paths.get(getDocumentDirectory()).normalize().toAbsolutePath();
        Path oscarDocument = documentDir.resolve(sanitizedFileName).normalize().toAbsolutePath();
        
        // Ensure the file is within the document directory
        if (!oscarDocument.startsWith(documentDir)) {
            log.error("Path traversal attempt in getOscarDocument: " + fileName);
            throw new SecurityException("Path traversal attempt detected");
        }
        
        return oscarDocument.toFile();
    }

    /**
     * Path NIO object in Path out.
     * The incoming path could have been derived from a temporary file.
     */
    public Path getOscarDocument(Path fileNamePath) {
        return getOscarDocument(fileNamePath.getFileName().toString()).toPath();
    }

    /**
     * Copy file from given file path into the default OscarDocuments directory.
     * This method deletes the temporary file after successful copy
     */
    public String copyFileToOscarDocuments(String tempFilePath) {
        try {
            // Normalize and validate the source file path
            Path sourcePath = Paths.get(tempFilePath).normalize().toAbsolutePath();
            
            // Verify source file exists and is a regular file
            if (!Files.exists(sourcePath) || !Files.isRegularFile(sourcePath)) {
                log.error("Source file does not exist or is not a regular file: " + tempFilePath);
                return null;
            }
            
            // Get just the filename and sanitize it
            String fileName = sourcePath.getFileName().toString();
            String sanitizedFileName = sanitizeFileName(fileName);
            
            // Get and validate destination directory
            Path destinationDir = Paths.get(getDocumentDirectory()).normalize().toAbsolutePath();
            Path destinationFilePath = destinationDir.resolve(sanitizedFileName).normalize().toAbsolutePath();
            
            // Ensure destination is within the document directory
            if (!destinationFilePath.startsWith(destinationDir)) {
                log.error("Path traversal attempt in copyFileToOscarDocuments: " + tempFilePath);
                throw new SecurityException("Path traversal attempt detected");
            }
            
            Files.copy(sourcePath, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
            if (Files.exists(destinationFilePath)) {
                deleteTempFile(sourcePath.toString());
            }
            return destinationFilePath.toString();
        } catch (IOException e) {
            log.error("An error occurred while moving the PDF file", e);
            return null;
        }
    }

    /**
     * Get the default OscarDocument directory.
     * Newer versions of OSCAR will only define the path for the BASE_DOCUMENT and
     * not for the full DOCUMENT_DIRECTORY path in Oscar.properties.
     * This method considers both locations.
     */
    private String getDocumentDirectory() {
        String document_dir = DOCUMENT_DIRECTORY;
        if (document_dir == null || !Files.isDirectory(Paths.get(document_dir))) {
            document_dir = String.valueOf(Paths.get(BASE_DOCUMENT_DIR, "document"));
        }
        return document_dir;
    }

    /**
     * True if given filename exists in OscarDocument directory.
     * False if file not found.
     */
    public boolean isOscarDocument(String fileName) {
        File oscarDocument = getOscarDocument(fileName);
        return Files.exists(oscarDocument.toPath());
    }


}
