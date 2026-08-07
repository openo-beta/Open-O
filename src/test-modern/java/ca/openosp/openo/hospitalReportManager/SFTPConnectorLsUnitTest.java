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

import ca.openosp.openo.utility.SpringUtils;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.withSettings;

/**
 * Unit tests for {@link SFTPConnector#ls(String)}.
 * <p>
 * The reports are fetched from the configured folder only. A subdirectory listed alongside them is
 * left out, because downloading it fails and ends the fetch before any report is processed.
 *
 * @since 2026-08-07
 */
@Tag("unit")
@Tag("hrm")
class SFTPConnectorLsUnitTest {

    private static final String REMOTE_DIR = "Clinic1";

    private SFTPConnector connector;
    private ChannelSftp channel;
    private MockedStatic<SpringUtils> springUtils;

    @BeforeEach
    void setUp() throws Exception {
        springUtils = mockStatic(SpringUtils.class);
        springUtils.when(() -> SpringUtils.getBean(any(Class.class)))
                .thenAnswer(invocation -> mock((Class<?>) invocation.getArgument(0)));

        connector = mock(SFTPConnector.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        channel = mock(ChannelSftp.class);

        Field field = SFTPConnector.class.getDeclaredField("cmd");
        field.setAccessible(true);
        field.set(connector, channel);
    }

    @AfterEach
    void tearDown() {
        if (springUtils != null) {
            springUtils.close();
        }
    }

    @Test
    @DisplayName("should return the files when the folder holds only files")
    void shouldReturnFiles_whenFolderHoldsOnlyFiles() throws Exception {
        listing(file("report1_encrypted.xml"), file("report2_encrypted.xml"));

        assertThat(connector.ls(REMOTE_DIR)).containsExactly("report1_encrypted.xml", "report2_encrypted.xml");
    }

    @Test
    @DisplayName("should leave out a subdirectory listed beside the files")
    void shouldLeaveOutSubdirectory_whenListedBesideFiles() throws Exception {
        listing(file("report1_encrypted.xml"), directory("archive"), file("report2_encrypted.xml"));

        assertThat(connector.ls(REMOTE_DIR)).containsExactly("report1_encrypted.xml", "report2_encrypted.xml");
    }

    @Test
    @DisplayName("should return nothing when the folder holds only subdirectories")
    void shouldReturnNothing_whenFolderHoldsOnlySubdirectories() throws Exception {
        listing(directory("Clinic1"), directory("Clinic2"));

        assertThat(connector.ls(REMOTE_DIR)).isEmpty();
    }

    @Test
    @DisplayName("should leave out the current and parent directory entries")
    void shouldLeaveOutCurrentAndParentEntries() throws Exception {
        listing(directory("."), directory(".."), file("report1_encrypted.xml"));

        assertThat(connector.ls(REMOTE_DIR)).containsExactly("report1_encrypted.xml");
    }

    @Test
    @DisplayName("should read the same folder however the path is written")
    void shouldReadSameFolder_howeverPathIsWritten() {
        assertThat(SFTPConnector.normalizeRemoteDir("Clinic1")).isEqualTo("Clinic1");
        assertThat(SFTPConnector.normalizeRemoteDir("/Clinic1")).isEqualTo("Clinic1");
        assertThat(SFTPConnector.normalizeRemoteDir("/Clinic1/")).isEqualTo("Clinic1");
        assertThat(SFTPConnector.normalizeRemoteDir("  /Clinic1/  ")).isEqualTo("Clinic1");
        assertThat(SFTPConnector.normalizeRemoteDir("//Clinic1//2026//")).isEqualTo("Clinic1/2026");
        assertThat(SFTPConnector.normalizeRemoteDir("aaa/bbb/ccc")).isEqualTo("aaa/bbb/ccc");
    }

    @Test
    @DisplayName("should read the home directory when no folder is given")
    void shouldReadHomeDirectory_whenNoFolderGiven() {
        assertThat(SFTPConnector.normalizeRemoteDir("/")).isEqualTo(".");
        assertThat(SFTPConnector.normalizeRemoteDir("")).isEqualTo(".");
        assertThat(SFTPConnector.normalizeRemoteDir(null)).isEqualTo(".");
    }

    @Test
    @DisplayName("should return the subdirectories of a folder")
    void shouldReturnSubdirectories_ofFolder() throws Exception {
        listing(file("report1_encrypted.xml"), directory("2026"), directory("archive"));

        assertThat(connector.lsDirectories(REMOTE_DIR)).containsExactly("2026", "archive");
    }

    @Test
    @DisplayName("should walk the configured folder and everything below it")
    void shouldWalkConfiguredFolder_andEverythingBelowIt() throws Exception {
        listingOf(REMOTE_DIR, file("report1_encrypted.xml"), directory("2026"));
        listingOf(REMOTE_DIR + "/2026", directory("august"));
        listingOf(REMOTE_DIR + "/2026/august", file("report2_encrypted.xml"));

        assertThat(connector.listFoldersToFetch(REMOTE_DIR, 5))
                .containsExactly(REMOTE_DIR, REMOTE_DIR + "/2026", REMOTE_DIR + "/2026/august");
    }

    @Test
    @DisplayName("should stop walking at the depth limit")
    void shouldStopWalking_atDepthLimit() throws Exception {
        listingOf(REMOTE_DIR, directory("2026"));
        listingOf(REMOTE_DIR + "/2026", directory("august"));

        assertThat(connector.listFoldersToFetch(REMOTE_DIR, 1)).containsExactly(REMOTE_DIR, REMOTE_DIR + "/2026");
    }

    @Test
    @DisplayName("should walk only the configured folder when it holds no subdirectories")
    void shouldWalkOnlyConfiguredFolder_whenNoSubdirectories() throws Exception {
        listing(file("report1_encrypted.xml"), file("report2_encrypted.xml"));

        assertThat(connector.listFoldersToFetch(REMOTE_DIR, 5)).containsExactly(REMOTE_DIR);
    }

    /**
     * Makes the channel answer an ls of the given folder with the given entries.
     *
     * @param folder  String the folder path the entries belong to
     * @param entries LsEntry... the entries the sFTP server reports for that folder
     */
    private void listingOf(String folder, LsEntry... entries) throws Exception {
        Vector<LsEntry> fileList = new Vector<LsEntry>();
        for (LsEntry entry : entries) {
            fileList.add(entry);
        }
        doReturn(fileList).when(channel).ls(folder);
    }

    /**
     * Makes the channel answer an ls of the remote directory with the given entries.
     *
     * @param entries LsEntry... the entries the sFTP server reports for the folder
     */
    private void listing(LsEntry... entries) throws Exception {
        Vector<LsEntry> fileList = new Vector<LsEntry>();
        for (LsEntry entry : entries) {
            fileList.add(entry);
        }
        doReturn(fileList).when(channel).ls(anyString());
    }

    /**
     * @param name String the filename reported by the server
     * @return LsEntry an entry for a regular file
     */
    private static LsEntry file(String name) {
        return entry(name, false);
    }

    /**
     * @param name String the directory name reported by the server
     * @return LsEntry an entry for a directory
     */
    private static LsEntry directory(String name) {
        return entry(name, true);
    }

    private static LsEntry entry(String name, boolean isDirectory) {
        SftpATTRS attrs = mock(SftpATTRS.class);
        doReturn(isDirectory).when(attrs).isDir();

        LsEntry entry = mock(LsEntry.class);
        doReturn(name).when(entry).getFilename();
        doReturn(attrs).when(entry).getAttrs();
        return entry;
    }
}
