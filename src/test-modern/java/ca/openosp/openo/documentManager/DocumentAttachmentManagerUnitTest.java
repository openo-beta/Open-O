/**
 *
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
 * This software was written for
 * Magenta Health
 * Toronto, Ontario, Canada
 */
package ca.openosp.openo.documentManager;

import ca.openosp.openo.commn.dao.DocumentDao;
import ca.openosp.openo.commn.dao.EFormDataDao;
import ca.openosp.openo.commn.dao.PatientLabRoutingDao;
import ca.openosp.openo.commn.dao.TicklerDocsDao;
import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.commn.model.PatientLabRouting;
import ca.openosp.openo.commn.model.TicklerDocs;
import ca.openosp.openo.commn.model.enumerator.DocumentType;
import ca.openosp.openo.documentManager.data.AttachmentLabResultData;
import ca.openosp.openo.documentManager.data.TicklerAttachmentData;
import ca.openosp.openo.encounter.data.EctFormData;
import ca.openosp.openo.hospitalReportManager.HRMUtil;
import ca.openosp.openo.managers.FormsManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DocumentAttachmentManagerImpl#getTicklerAttachmentDetails}.
 *
 * <p>LAB display names depend on lab infrastructure ({@code CommonLabResultData}), so the LAB
 * view URL tests stub the lab-name lookup on a spy instead.</p>
 *
 * @since 2026-07-19
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentAttachmentManager Tickler Attachment Details Unit Tests")
@Tag("unit")
@Tag("fast")
@Tag("manager")
@Tag("tickler")
public class DocumentAttachmentManagerUnitTest extends OpenOUnitTestBase {

    private static final Integer TICKLER_ID = 42;
    private static final Integer DEMOGRAPHIC_NO = 12345;
    private static final String PROVIDER_NO = "999990";

    @Mock
    private TicklerDocsDao mockTicklerDocsDao;

    @Mock
    private DocumentDao mockDocumentDao;

    @Mock
    private EFormDataDao mockEFormDataDao;

    @Mock
    private FormsManager mockFormsManager;

    @Mock
    private SecurityInfoManager mockSecurityInfoManager;

    @Mock
    private LoggedInInfo mockLoggedInInfo;

    private DocumentAttachmentManagerImpl manager;

    @BeforeEach
    void setUp() {
        // HRMUtil's static initializer fetches these beans; register them before mockStatic(HRMUtil)
        registerMock(SecurityInfoManager.class, mockSecurityInfoManager);
        createAndRegisterMock(ca.openosp.openo.hospitalReportManager.dao.HRMDocumentDao.class);
        createAndRegisterMock(ca.openosp.openo.hospitalReportManager.dao.HRMDocumentToDemographicDao.class);
        createAndRegisterMock(ca.openosp.openo.hospitalReportManager.dao.HRMDocumentToProviderDao.class);
        createAndRegisterMock(ca.openosp.openo.hospitalReportManager.dao.HRMSubClassDao.class);
        createAndRegisterMock(ca.openosp.openo.hospitalReportManager.dao.HRMDocumentSubClassDao.class);
        createAndRegisterMock(ca.openosp.openo.hospitalReportManager.dao.HRMCategoryDao.class);
        createAndRegisterMock(ca.openosp.openo.commn.dao.IncomingLabRulesDao.class);
        createAndRegisterMock(ca.openosp.openo.managers.NioFileManager.class);

        // Security manager grants the "_tickler" read privilege by default
        lenient().when(mockSecurityInfoManager.hasPrivilege(any(), anyString(), anyString(), anyInt()))
                .thenReturn(true);
        // The "_form" check passes a null demographic, so stub the String overload as well
        lenient().when(mockSecurityInfoManager.hasPrivilege(any(), anyString(), anyString(), nullable(String.class)))
                .thenReturn(true);

        manager = new DocumentAttachmentManagerImpl();

        injectDependency(manager, "ticklerDocsDao", mockTicklerDocsDao);
        injectDependency(manager, "documentDao", mockDocumentDao);
        injectDependency(manager, "eFormDataDao", mockEFormDataDao);
        injectDependency(manager, "formsManager", mockFormsManager);
        injectDependency(manager, "securityInfoManager", mockSecurityInfoManager);
    }

    @Nested
    @DisplayName("Security")
    class Security {

        @Test
        @DisplayName("should throw exception when user lacks tickler read privilege")
        void shouldThrowException_whenUserLacksTicklerReadPrivilege() {
            // Given
            when(mockSecurityInfoManager.hasPrivilege(any(), anyString(), anyString(), anyInt()))
                    .thenReturn(false);

            // When / Then
            assertThatThrownBy(() -> manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("_tickler");
            verifyNoInteractions(mockTicklerDocsDao);
        }
    }

    @Nested
    @DisplayName("Display Name Resolution")
    class DisplayNameResolution {

        @Test
        @DisplayName("should return empty list when tickler has no attachments")
        void shouldReturnEmptyList_whenTicklerHasNoAttachments() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.emptyList());

            // When
            List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            assertThat(details).isEmpty();
        }

        @Test
        @DisplayName("should resolve document description when a document is attached")
        void shouldResolveDocumentDescription_whenDocAttached() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 12, TicklerDocs.DOCTYPE_DOC, PROVIDER_NO)));
            Document document = new Document();
            document.setDocdesc("Discharge Summary");
            when(mockDocumentDao.getDocument("12")).thenReturn(document);

            // When
            List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            assertThat(details).hasSize(1);
            assertThat(details.get(0).getDocumentType()).isEqualTo(DocumentType.DOC);
            assertThat(details.get(0).getDocumentId()).isEqualTo("12");
            assertThat(details.get(0).getDisplayName()).isEqualTo("Discharge Summary");
        }

        @Test
        @DisplayName("should resolve eForm name when an eForm is attached")
        void shouldResolveEFormName_whenEFormAttached() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 33, TicklerDocs.DOCTYPE_EFORM, PROVIDER_NO)));
            EFormData eForm = new EFormData();
            eForm.setFormName("Diabetes Care Plan");
            when(mockEFormDataDao.find(33)).thenReturn(eForm);

            // When
            List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            assertThat(details).hasSize(1);
            assertThat(details.get(0).getDocumentType()).isEqualTo(DocumentType.EFORM);
            assertThat(details.get(0).getDisplayName()).isEqualTo("Diabetes Care Plan");
        }

        @Test
        @DisplayName("should resolve encounter form name when a form is attached")
        void shouldResolveFormName_whenEncounterFormAttached() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 55, TicklerDocs.DOCTYPE_FORM, PROVIDER_NO)));
            EctFormData.PatientForm patientForm = mock(EctFormData.PatientForm.class);
            when(patientForm.getFormId()).thenReturn("55");
            when(patientForm.getFormName()).thenReturn("Rourke Baby Record");
            when(mockFormsManager.getEncounterFormsByDemographicNumbers(eq(mockLoggedInInfo), anyCollection(), eq(true), eq(true)))
                    .thenReturn(Map.of(DEMOGRAPHIC_NO, Collections.singletonList(patientForm)));

            // When
            List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            assertThat(details).hasSize(1);
            assertThat(details.get(0).getDocumentType()).isEqualTo(DocumentType.FORM);
            assertThat(details.get(0).getDisplayName()).isEqualTo("Rourke Baby Record");
        }

        @Test
        @DisplayName("should resolve HRM report name when an HRM document is attached")
        void shouldResolveHrmName_whenHrmAttached() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 7, TicklerDocs.DOCTYPE_HRM, PROVIDER_NO)));
            HashMap<String, Object> hrmDocument = new HashMap<>();
            hrmDocument.put("id", 7);
            hrmDocument.put("name", "Cardiology Consult Report");
            ArrayList<HashMap<String, ? extends Object>> allHrmDocuments = new ArrayList<>();
            allHrmDocuments.add(hrmDocument);

            try (MockedStatic<HRMUtil> hrmUtilMock = mockStatic(HRMUtil.class)) {
                hrmUtilMock.when(() -> HRMUtil.listHRMDocuments(any(), anyString(), anyBoolean(), anyString(), anyBoolean()))
                        .thenReturn(allHrmDocuments);

                // When
                List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

                // Then
                assertThat(details).hasSize(1);
                assertThat(details.get(0).getDocumentType()).isEqualTo(DocumentType.HRM);
                assertThat(details.get(0).getDisplayName()).isEqualTo("Cardiology Consult Report");
            }
        }

        @Test
        @DisplayName("should fall back to type and id when the referenced document is missing")
        void shouldFallBackToTypeAndId_whenDocumentMissing() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 12, TicklerDocs.DOCTYPE_DOC, PROVIDER_NO)));
            when(mockDocumentDao.getDocument("12")).thenReturn(null);

            // When
            List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            assertThat(details).hasSize(1);
            assertThat(details.get(0).getDisplayName()).isEqualTo("doc #12");
        }

        @Test
        @DisplayName("should skip attachments with an unknown doctype")
        void shouldSkipAttachment_whenDocTypeUnknown() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 99, "X", PROVIDER_NO)));

            // When
            List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            assertThat(details).isEmpty();
        }
    }

    /**
     * Covers the viewer URLs the Edit Tickler window links attachments to.
     */
    @Nested
    @DisplayName("View URL Resolution")
    class ViewUrlResolution {

        @Mock
        private PatientLabRoutingDao mockPatientLabRoutingDao;

        @BeforeEach
        void setUpProvider() {
            lenient().when(mockLoggedInInfo.getLoggedInProviderNo()).thenReturn(PROVIDER_NO);
            injectDependency(manager, "patientLabRoutingDao", mockPatientLabRoutingDao);
        }

        /**
         * Spies the manager so the lab-name lookup, which needs lab infrastructure, can be stubbed.
         */
        private DocumentAttachmentManagerImpl spyWithLabs(String... segmentIds) {
            DocumentAttachmentManagerImpl spyManager = spy(manager);
            List<AttachmentLabResultData> labs = new ArrayList<>();
            for (String segmentId : segmentIds) {
                labs.add(new AttachmentLabResultData(segmentId, "CBC " + segmentId, null));
            }
            doReturn(labs).when(spyManager).getAllLabsSortedByVersions(mockLoggedInInfo, String.valueOf(DEMOGRAPHIC_NO));
            return spyManager;
        }

        @Test
        @DisplayName("should link a document to the document viewer")
        void shouldBuildDocumentViewerUrl_whenDocAttached() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 12, TicklerDocs.DOCTYPE_DOC, PROVIDER_NO)));

            // When
            List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            assertThat(details.get(0).getViewUrl()).isEqualTo(
                    "/documentManager/ManageDocument.do?method=display&doc_no=12&providerNo=999990&searchProviderNo=999990&status=");
        }

        @Test
        @DisplayName("should link an eForm to the eForm viewer")
        void shouldBuildEFormViewerUrl_whenEFormAttached() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 33, TicklerDocs.DOCTYPE_EFORM, PROVIDER_NO)));

            // When
            List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            assertThat(details.get(0).getViewUrl()).isEqualTo("/eform/efmshowform_data.jsp?fdid=33");
        }

        @Test
        @DisplayName("should link an HRM report to the HRM viewer")
        void shouldBuildHrmViewerUrl_whenHrmAttached() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 7, TicklerDocs.DOCTYPE_HRM, PROVIDER_NO)));

            try (MockedStatic<HRMUtil> hrmUtilMock = mockStatic(HRMUtil.class)) {
                hrmUtilMock.when(() -> HRMUtil.listHRMDocuments(any(), anyString(), anyBoolean(), anyString(), anyBoolean()))
                        .thenReturn(new ArrayList<>());

                // When
                List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

                // Then
                assertThat(details.get(0).getViewUrl()).isEqualTo("/hospitalReportManager/Display.do?id=7&segmentID=7");
            }
        }

        @Test
        @DisplayName("should link an encounter form by its URL-encoded name")
        void shouldBuildFormViewerUrl_whenFormNameResolves() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 55, TicklerDocs.DOCTYPE_FORM, PROVIDER_NO)));
            EctFormData.PatientForm patientForm = mock(EctFormData.PatientForm.class);
            when(patientForm.getFormId()).thenReturn("55");
            when(patientForm.getFormName()).thenReturn("Rourke Baby Record");
            when(mockFormsManager.getEncounterFormsByDemographicNumbers(eq(mockLoggedInInfo), anyCollection(), eq(true), eq(true)))
                    .thenReturn(Map.of(DEMOGRAPHIC_NO, Collections.singletonList(patientForm)));

            // When
            List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            assertThat(details.get(0).getViewUrl()).isEqualTo(
                    "/form/forwardshortcutname.do?formname=Rourke%20Baby%20Record&demographic_no=12345&formId=55");
        }

        @Test
        @DisplayName("should leave an encounter form unlinked when its name no longer resolves")
        void shouldReturnNullViewUrl_whenFormNameUnresolved() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 55, TicklerDocs.DOCTYPE_FORM, PROVIDER_NO)));
            when(mockFormsManager.getEncounterFormsByDemographicNumbers(eq(mockLoggedInInfo), anyCollection(), eq(true), eq(true)))
                    .thenReturn(Collections.emptyMap());

            // When
            List<TicklerAttachmentData> details = manager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            assertThat(details.get(0).getDisplayName()).isEqualTo("form #55");
            assertThat(details.get(0).getViewUrl()).isNull();
        }

        @ParameterizedTest(name = "lab type {0} -> {1}")
        @CsvSource(value = {
                "HL7, /lab/CA/ALL/labDisplay.jsp",
                "MDS, /oscarMDS/SegmentDisplay.jsp",
                "CML, /lab/CA/ON/CMLDisplay.jsp",
                "BCP, /lab/CA/BC/labDisplay.jsp",
                "NULL, /lab/CA/BC/labDisplay.jsp"
        }, nullValues = "NULL")
        @DisplayName("should route a lab to the viewer for its sub-type, falling back to the BC viewer")
        void shouldBuildLabViewerUrl_forEachLabType(String labType, String expectedPath) {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(Collections.singletonList(
                    new TicklerDocs(TICKLER_ID, 88, TicklerDocs.DOCTYPE_LAB, PROVIDER_NO)));
            when(mockPatientLabRoutingDao.findByLabNos(Collections.singletonList(88))).thenReturn(labType == null
                    ? Collections.emptyList()
                    : Collections.singletonList(new PatientLabRouting(88, labType, DEMOGRAPHIC_NO)));
            DocumentAttachmentManagerImpl spyManager = spyWithLabs("88");

            // When
            List<TicklerAttachmentData> details = spyManager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            assertThat(details.get(0).getDisplayName()).isEqualTo("CBC 88");
            assertThat(details.get(0).getViewUrl()).isEqualTo(
                    expectedPath + "?segmentID=88&providerNo=999990&searchProviderNo=999990&status=");
        }

        @Test
        @DisplayName("should look up lab sub-types once for all of a tickler's labs")
        void shouldBatchLabTypeLookup_whenSeveralLabsAttached() {
            // Given
            when(mockTicklerDocsDao.findByTicklerId(TICKLER_ID)).thenReturn(List.of(
                    new TicklerDocs(TICKLER_ID, 88, TicklerDocs.DOCTYPE_LAB, PROVIDER_NO),
                    new TicklerDocs(TICKLER_ID, 12, TicklerDocs.DOCTYPE_DOC, PROVIDER_NO),
                    new TicklerDocs(TICKLER_ID, 89, TicklerDocs.DOCTYPE_LAB, PROVIDER_NO)));
            when(mockPatientLabRoutingDao.findByLabNos(List.of(88, 89))).thenReturn(List.of(
                    new PatientLabRouting(88, "HL7", DEMOGRAPHIC_NO),
                    new PatientLabRouting(89, "MDS", DEMOGRAPHIC_NO)));
            DocumentAttachmentManagerImpl spyManager = spyWithLabs("88", "89");

            // When
            List<TicklerAttachmentData> details = spyManager.getTicklerAttachmentDetails(mockLoggedInInfo, TICKLER_ID, DEMOGRAPHIC_NO);

            // Then
            verify(mockPatientLabRoutingDao, times(1)).findByLabNos(anyList());
            assertThat(details).extracting(TicklerAttachmentData::getViewUrl).containsExactly(
                    "/lab/CA/ALL/labDisplay.jsp?segmentID=88&providerNo=999990&searchProviderNo=999990&status=",
                    "/documentManager/ManageDocument.do?method=display&doc_no=12&providerNo=999990&searchProviderNo=999990&status=",
                    "/oscarMDS/SegmentDisplay.jsp?segmentID=89&providerNo=999990&searchProviderNo=999990&status=");
        }
    }

    /**
     * Covers the shared form-name recovery that makes attached encounter forms addressable.
     * Attachment tables store only {@code (document_no, doctype)}, which cannot identify a form.
     */
    @Nested
    @DisplayName("Form Name Recovery")
    class FormNameRecovery {

        @Test
        @DisplayName("should map form ids to form names for the patient")
        void shouldMapFormIdsToNames_whenPatientHasForms() {
            // Given
            List<EctFormData.PatientForm> forms =
                    List.of(patientForm("7", "Annual"), patientForm("9", "Rourke2020"));
            when(mockFormsManager.getEncounterFormsByDemographicNumbers(eq(mockLoggedInInfo), anyCollection(), eq(true), eq(true)))
                    .thenReturn(Map.of(DEMOGRAPHIC_NO, forms));

            // When
            Map<String, String> formNames = manager.getFormNamesByFormId(mockLoggedInInfo, DEMOGRAPHIC_NO);

            // Then
            assertThat(formNames).containsExactlyInAnyOrderEntriesOf(
                    Map.of("7", "Annual", "9", "Rourke2020"));
        }

        @Test
        @DisplayName("should request every version so a superseded form still resolves")
        void shouldRequestAllVersions_whenResolvingFormNames() {
            // Given
            when(mockFormsManager.getEncounterFormsByDemographicNumbers(any(), anyCollection(), anyBoolean(), anyBoolean()))
                    .thenReturn(Collections.emptyMap());

            // When
            manager.getFormNamesByFormId(mockLoggedInInfo, DEMOGRAPHIC_NO);

            // Then
            verify(mockFormsManager).getEncounterFormsByDemographicNumbers(
                    eq(mockLoggedInInfo), anyCollection(), eq(true), eq(true));
        }

        @Test
        @DisplayName("should omit a form id shared by two form types rather than guess")
        void shouldOmitFormId_whenTwoFormTypesShareIt() {
            // Given
            List<EctFormData.PatientForm> forms =
                    List.of(patientForm("7", "Annual"), patientForm("7", "Rourke2020"),
                            patientForm("9", "Annual"));
            when(mockFormsManager.getEncounterFormsByDemographicNumbers(eq(mockLoggedInInfo), anyCollection(), eq(true), eq(true)))
                    .thenReturn(Map.of(DEMOGRAPHIC_NO, forms));

            // When
            Map<String, String> formNames = manager.getFormNamesByFormId(mockLoggedInInfo, DEMOGRAPHIC_NO);

            // Then
            assertThat(formNames).containsExactlyInAnyOrderEntriesOf(Map.of("9", "Annual"));
        }

        @Test
        @DisplayName("should return empty map when user lacks form read privilege")
        void shouldReturnEmptyMap_whenFormPrivilegeDenied() {
            // Given
            when(mockSecurityInfoManager.hasPrivilege(any(), eq("_form"), anyString(), nullable(String.class)))
                    .thenReturn(false);

            // When
            Map<String, String> formNames = manager.getFormNamesByFormId(mockLoggedInInfo, DEMOGRAPHIC_NO);

            // Then
            assertThat(formNames).isEmpty();
            verifyNoInteractions(mockFormsManager);
        }

        @Test
        @DisplayName("should return empty map when demographic is null")
        void shouldReturnEmptyMap_whenDemographicIsNull() {
            // When
            Map<String, String> formNames = manager.getFormNamesByFormId(mockLoggedInInfo, null);

            // Then
            assertThat(formNames).isEmpty();
            verifyNoInteractions(mockFormsManager);
        }

        private EctFormData.PatientForm patientForm(String formId, String formName) {
            EctFormData.PatientForm form = mock(EctFormData.PatientForm.class);
            when(form.getFormId()).thenReturn(formId);
            when(form.getFormName()).thenReturn(formName);
            return form;
        }
    }

    /**
     * Helper method to inject dependencies into the manager using reflection.
     */
    private void injectDependency(Object target, String fieldName, Object dependency) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, dependency);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject " + fieldName, e);
        }
    }
}
