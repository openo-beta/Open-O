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

import ca.openosp.openo.commn.dao.HrmLogDao;
import ca.openosp.openo.commn.dao.HrmLogEntryDao;
import ca.openosp.openo.commn.model.HrmLog;
import ca.openosp.openo.hospitalReportManager.dao.HRMSendingFacilityDao;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * Unit tests for the deletion step of {@link SFTPConnector#startAutoFetch(LoggedInInfo, String)}.
 * <p>
 * A downloaded report is removed from the sFTP server only once it has reached the document
 * directory. A file that fails before that point stays on the server so the next fetch retries it,
 * rather than being deleted ahead of the processing that stores it.
 * <p>
 * The connector is built without its constructor so no sFTP session or Spring context is needed;
 * the collaborators that touch the network, the filesystem and the database are stubbed.
 *
 * @since 2026-07-31
 */
@Tag("unit")
@Tag("hrm")
class SFTPConnectorAutoFetchDeleteUnitTest {

    private static final String REMOTE_DIR = "hrmfolder";

    private static final String REMOTE_1 = "20260731_MIS1_001_encrypted.xml";
    private static final String REMOTE_2 = "20260731_MIS1_002_encrypted.xml";
    private static final String REMOTE_3 = "20260731_0911_003_encrypted.xml";

    private static final String LOCAL_1 = "/tmp/oscar-sftp/31072026/" + REMOTE_1;
    private static final String LOCAL_2 = "/tmp/oscar-sftp/31072026/" + REMOTE_2;
    private static final String LOCAL_3 = "/tmp/oscar-sftp/31072026/" + REMOTE_3;

    private SFTPConnector connector;
    private LoggedInInfo loggedInInfo;
    private MockedStatic<HRMReportParser> parser;
    private MockedStatic<SpringUtils> springUtils;

    @BeforeEach
    void setUp() throws Exception {
        setAutoFetchRunning(false);

        springUtils = mockStatic(SpringUtils.class);
        springUtils.when(() -> SpringUtils.getBean(any(Class.class)))
                .thenAnswer(invocation -> mock((Class<?>) invocation.getArgument(0)));
        suppressAdminNotifications();

        connector = mock(SFTPConnector.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        loggedInInfo = mock(LoggedInInfo.class);

        inject("hrmLog", persistedHrmLog());
        // The daily file log is opened by the constructor, which a mock built this way never runs.
        inject("fLogger", java.util.logging.Logger.getLogger(SFTPConnectorAutoFetchDeleteUnitTest.class.getName()));
        inject("hrmLogDao", mock(HrmLogDao.class));
        inject("hrmLogEntryDao", mock(HrmLogEntryDao.class));
        inject("hrmSendingFacilityDao", mock(HRMSendingFacilityDao.class));

        doNothing().when(connector).close();
        doNothing().when(connector).deleteDirectoryContents(anyString(), any(String[].class));

        doReturn(List.of(REMOTE_DIR)).when(connector).listFoldersToFetch(anyString(), anyInt());
        doReturn(new String[]{REMOTE_1, REMOTE_2, REMOTE_3}).when(connector).ls(REMOTE_DIR);
        doReturn(new String[]{LOCAL_1, LOCAL_2, LOCAL_3})
                .when(connector).downloadDirectoryContents(REMOTE_DIR);

        // Every file decrypts, is written out and reaches the document directory unless a test
        // overrides one of these for a specific file.
        doReturn("<xml/>").when(connector).decryptFile(anyString(), any());
        doAnswerEcho();

        HRMReport report = mock(HRMReport.class);
        doReturn(null).when(report).getSendingFacilityId();

        parser = mockStatic(HRMReportParser.class);
        parser.when(() -> HRMReportParser.parseReport(any(), anyString(), anyList())).thenReturn(report);
        parser.when(() -> HRMReportParser.addReportToInbox(any(), any())).thenReturn(null);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (parser != null) {
            parser.close();
        }
        if (springUtils != null) {
            springUtils.close();
        }
        doNotSendList().clear();
        setAutoFetchRunning(false);
    }

    @Test
    @DisplayName("should remove every file from the server when all of them are stored")
    void shouldRemoveAll_whenEveryFileIsStored() {
        connector.startAutoFetch(loggedInInfo, REMOTE_DIR);

        assertThat(capturedDeletions()).containsExactly(REMOTE_1, REMOTE_2, REMOTE_3);
    }

    @Test
    @DisplayName("should leave a file on the server when it cannot be decrypted")
    void shouldKeepOnServer_whenDecryptionFails() throws Exception {
        doThrow(new Exception("bad AES key")).when(connector).decryptFile(eq(LOCAL_2), any());

        connector.startAutoFetch(loggedInInfo, REMOTE_DIR);

        assertThat(capturedDeletions()).containsExactly(REMOTE_1, REMOTE_3);
    }

    @Test
    @DisplayName("should leave a file on the server when it never reaches the document directory")
    void shouldKeepOnServer_whenCopyToDocumentDirectoryReturnsNothing() throws Exception {
        doReturn(null).when(connector).copyFileToDocumentDir(any(), eq(LOCAL_2));

        connector.startAutoFetch(loggedInInfo, REMOTE_DIR);

        assertThat(capturedDeletions()).containsExactly(REMOTE_1, REMOTE_3);
    }

    @Test
    @DisplayName("should remove a stored file even when the report cannot be parsed")
    void shouldRemove_whenReportIsStoredButUnparseable() {
        parser.when(() -> HRMReportParser.parseReport(any(), anyString(), anyList())).thenReturn(null);

        connector.startAutoFetch(loggedInInfo, REMOTE_DIR);

        assertThat(capturedDeletions()).containsExactly(REMOTE_1, REMOTE_2, REMOTE_3);
    }

    @Test
    @DisplayName("should remove nothing from the server when no file is stored")
    void shouldRemoveNothing_whenNoFileIsStored() throws Exception {
        doThrow(new Exception("bad AES key")).when(connector).decryptFile(anyString(), any());

        connector.startAutoFetch(loggedInInfo, REMOTE_DIR);

        verify(connector, never()).deleteDirectoryContents(anyString(), any(String[].class));
    }

    @Test
    @DisplayName("should remove files from the server only after they have been stored")
    void shouldRemoveAfterStoring_notBefore() throws Exception {
        connector.startAutoFetch(loggedInInfo, REMOTE_DIR);

        // A single deletion, after the last file has reached the document directory. Deleting ahead
        // of the loop shows up here as an extra call.
        verify(connector, times(1)).deleteDirectoryContents(anyString(), any(String[].class));

        InOrder order = inOrder(connector);
        order.verify(connector).copyFileToDocumentDir(any(), eq(LOCAL_1));
        order.verify(connector).copyFileToDocumentDir(any(), eq(LOCAL_3));
        order.verify(connector).deleteDirectoryContents(anyString(), any(String[].class));
    }

    @Test
    @DisplayName("should report success when every file is processed")
    void shouldReportSuccess_whenEveryFileIsProcessed() {
        assertThat(connector.startAutoFetch(loggedInInfo, REMOTE_DIR)).isTrue();
    }

    @Test
    @DisplayName("should report failure when the download step fails")
    void shouldReportFailure_whenDownloadFails() throws Exception {
        doThrow(new Exception("no such folder")).when(connector).downloadDirectoryContents(REMOTE_DIR);

        assertThat(connector.startAutoFetch(loggedInInfo, REMOTE_DIR)).isFalse();
    }

    @Test
    @DisplayName("should download from a subfolder and delete each file from the folder it came from")
    void shouldDownloadFromSubfolder_andDeleteFromItsOwnFolder() throws Exception {
        String subFolder = REMOTE_DIR + "/2026";
        String remoteInSub = "20260731_MIS1_004_encrypted.xml";
        String localInSub = "/tmp/oscar-sftp/31072026/" + subFolder + "/" + remoteInSub;

        doReturn(List.of(REMOTE_DIR, subFolder)).when(connector).listFoldersToFetch(anyString(), anyInt());
        doReturn(new String[]{localInSub}).when(connector).downloadDirectoryContents(subFolder);
        doReturn(localInSub).when(connector).saveDecryptedData(eq(localInSub), anyString());
        doReturn(localInSub).when(connector).copyFileToDocumentDir(any(), eq(localInSub));

        connector.startAutoFetch(loggedInInfo, REMOTE_DIR);

        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(connector).deleteDirectoryContents(eq(REMOTE_DIR), captor.capture());
        assertThat(captor.getValue()).containsExactly(REMOTE_1, REMOTE_2, REMOTE_3);

        verify(connector).deleteDirectoryContents(eq(subFolder), captor.capture());
        assertThat(captor.getValue()).containsExactly(remoteInSub);
    }

    @Test
    @DisplayName("should count the files downloaded from each folder")
    void shouldCountFiles_downloadedFromEachFolder() throws Exception {
        String subFolder = REMOTE_DIR + "/2026";
        String localInSub = "/tmp/oscar-sftp/31072026/" + subFolder + "/20260731_MIS1_004_encrypted.xml";

        doReturn(List.of(REMOTE_DIR, subFolder)).when(connector).listFoldersToFetch(anyString(), anyInt());
        doReturn(new String[]{localInSub}).when(connector).downloadDirectoryContents(subFolder);

        connector.startAutoFetch(loggedInInfo, REMOTE_DIR);

        assertThat(connector.getTotalDownloaded()).isEqualTo(4);
        assertThat(connector.getDownloadedByFolder()).containsExactly(
                org.assertj.core.api.Assertions.entry(REMOTE_DIR, 3),
                org.assertj.core.api.Assertions.entry(subFolder, 1));
    }

    @Test
    @DisplayName("should count a walked folder that holds no files as zero")
    void shouldCountEmptyFolder_asZero() throws Exception {
        String emptyFolder = REMOTE_DIR + "/archive";

        doReturn(List.of(REMOTE_DIR, emptyFolder)).when(connector).listFoldersToFetch(anyString(), anyInt());
        doReturn(null).when(connector).downloadDirectoryContents(emptyFolder);

        connector.startAutoFetch(loggedInInfo, REMOTE_DIR);

        assertThat(connector.getDownloadedByFolder()).containsEntry(emptyFolder, 0);
        assertThat(connector.getTotalDownloaded()).isEqualTo(3);
    }

    /**
     * Returns the remote filenames handed to the single deletion call.
     *
     * @return List<String> the filenames removed from the sFTP server
     */
    private List<String> capturedDeletions() {
        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        try {
            verify(connector).deleteDirectoryContents(eq(REMOTE_DIR), captor.capture());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return List.of(captor.getValue());
    }

    /**
     * Makes saveDecryptedData and copyFileToDocumentDir return the path they were handed, so a file
     * keeps the same name from download through to the document directory.
     */
    private void doAnswerEcho() throws Exception {
        doReturn(LOCAL_1).when(connector).saveDecryptedData(eq(LOCAL_1), anyString());
        doReturn(LOCAL_2).when(connector).saveDecryptedData(eq(LOCAL_2), anyString());
        doReturn(LOCAL_3).when(connector).saveDecryptedData(eq(LOCAL_3), anyString());

        doReturn(LOCAL_1).when(connector).copyFileToDocumentDir(any(), eq(LOCAL_1));
        doReturn(LOCAL_2).when(connector).copyFileToDocumentDir(any(), eq(LOCAL_2));
        doReturn(LOCAL_3).when(connector).copyFileToDocumentDir(any(), eq(LOCAL_3));
    }

    /**
     * Returns an HrmLog carrying an id, as it does in the connector once the row has been written.
     *
     * @return HrmLog the log row the fetch records its progress against
     */
    private static HrmLog persistedHrmLog() throws Exception {
        HrmLog hrmLog = new HrmLog();
        Field id = HrmLog.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(hrmLog, 1);
        return hrmLog;
    }

    /**
     * Puts the fallback recipient on the do-not-send list so a failing file records its error
     * without the notification reaching the messaging layer.
     */
    private static void suppressAdminNotifications() throws Exception {
        doNotSendList().add("999998");
    }

    @SuppressWarnings("unchecked")
    private static java.util.Set<String> doNotSendList() throws Exception {
        Field field = SFTPConnector.class.getDeclaredField("doNotSentMsgForOuttage");
        field.setAccessible(true);
        return (java.util.Set<String>) field.get(null);
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = SFTPConnector.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(connector, value);
    }

    private static void setAutoFetchRunning(boolean running) throws Exception {
        Field field = SFTPConnector.class.getDeclaredField("isAutoFetchRunning");
        field.setAccessible(true);
        field.setBoolean(null, running);
    }
}
