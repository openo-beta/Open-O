/**
 * Copyright (c) 2026. OpenO EMR. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package ca.openosp.openo.documentManager;

import ca.openosp.openo.commn.dao.EFormDataDao;
import ca.openosp.openo.commn.dao.TicklerDao;
import ca.openosp.openo.commn.dao.TicklerDocsDao;
import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.commn.model.Tickler;
import ca.openosp.openo.commn.model.TicklerDocs;
import ca.openosp.openo.encounter.data.EctFormData;
import ca.openosp.openo.managers.FormsManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the tickler side of the attachment picker in {@link DocumentAttachmentManagerImpl}:
 * looking up a tickler's attached docs, eForms and encounter form versions so the picker can list
 * and pre-check them, and merging attached form versions into the "latest version only" form list.
 *
 * <p>Regression cover for attachments to a superseded form version (the form saved again after it
 * was attached) being left unchecked in the picker and then dropped on "Save and Close".</p>
 *
 * @since 2026-11-13
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentAttachmentManager Tickler Picker Unit Tests")
@Tag("unit")
@Tag("fast")
@Tag("manager")
@Tag("tickler")
public class DocumentAttachmentManagerTicklerPickerUnitTest extends OpenOUnitTestBase {

    private static final Integer TICKLER_ID = 42;
    private static final Integer DEMOGRAPHIC_NO = 12345;
    private static final String DEMOGRAPHIC_NO_STR = "12345";
    private static final String PROVIDER_NO = "999990";

    @Mock
    private TicklerDocsDao mockTicklerDocsDao;

    @Mock
    private TicklerDao mockTicklerDao;

    @Mock
    private EFormDataDao mockEFormDataDao;

    @Mock
    private FormsManager mockFormsManager;

    @Mock
    private SecurityInfoManager mockSecurityInfoManager;

    @Mock
    private LoggedInInfo mockLoggedInInfo;

    private Map<String, EDoc> docsById;

    private DocumentAttachmentManagerImpl manager;

    @BeforeEach
    void setUp() {
        registerMock(SecurityInfoManager.class, mockSecurityInfoManager);

        // "_tickler" is checked with the int demographic overload, "_form" with a null String
        lenient().when(mockSecurityInfoManager.hasPrivilege(any(), anyString(), anyString(), anyInt()))
                .thenReturn(true);
        lenient().when(mockSecurityInfoManager.hasPrivilege(any(), anyString(), anyString(), nullable(String.class)))
                .thenReturn(true);

        // The tickler belongs to the patient unless a test says otherwise
        lenient().when(mockTicklerDao.find(TICKLER_ID)).thenReturn(tickler(DEMOGRAPHIC_NO));

        docsById = new java.util.HashMap<>();
        // EDocUtil resolves its DAOs statically, so stub the manager's single doc lookup instead
        manager = new DocumentAttachmentManagerImpl() {
            @Override
            EDoc loadEDoc(String docId) {
                return docsById.getOrDefault(docId, new EDoc());
            }
        };

        injectDependency(manager, "ticklerDocsDao", mockTicklerDocsDao);
        injectDependency(manager, "ticklerDao", mockTicklerDao);
        injectDependency(manager, "eFormDataDao", mockEFormDataDao);
        injectDependency(manager, "formsManager", mockFormsManager);
        injectDependency(manager, "securityInfoManager", mockSecurityInfoManager);
    }

    @Nested
    @DisplayName("Security and ownership")
    class SecurityAndOwnership {

        @Test
        @DisplayName("should throw exception when user lacks tickler read privilege")
        void shouldThrowException_whenUserLacksTicklerReadPrivilege() {
            when(mockSecurityInfoManager.hasPrivilege(any(), eq("_tickler"), anyString(), anyInt()))
                    .thenReturn(false);

            assertThatThrownBy(() -> manager.getAttachedFormsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("_tickler");
            verifyNoInteractions(mockTicklerDocsDao);
        }

        @Test
        @DisplayName("should return nothing when the tickler belongs to another patient")
        void shouldReturnEmpty_whenTicklerBelongsToAnotherPatient() {
            when(mockTicklerDao.find(TICKLER_ID)).thenReturn(tickler(99999));

            assertThat(manager.getAttachedDocsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID)).isEmpty();
            assertThat(manager.getAttachedEFormsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID)).isEmpty();
            assertThat(manager.getAttachedFormsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID)).isEmpty();
            verifyNoInteractions(mockTicklerDocsDao);
        }

        @Test
        @DisplayName("should return nothing for a new tickler with no id")
        void shouldReturnEmpty_whenTicklerIdIsNull() {
            assertThat(manager.getAttachedFormsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, null)).isEmpty();
            verifyNoInteractions(mockTicklerDocsDao, mockTicklerDao);
        }

        @Test
        @DisplayName("should return no forms when user lacks form read privilege")
        void shouldReturnNoForms_whenFormPrivilegeDenied() {
            attach(TicklerDocs.DOCTYPE_FORM, 12);
            when(mockSecurityInfoManager.hasPrivilege(any(), eq("_form"), anyString(), nullable(String.class)))
                    .thenReturn(false);

            assertThat(manager.getAttachedFormsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID)).isEmpty();
            verifyNoInteractions(mockFormsManager);
        }
    }

    @Nested
    @DisplayName("Attached form lookup")
    class AttachedFormLookup {

        @Test
        @DisplayName("should resolve the attached version when the form was saved again after attaching")
        void shouldResolveAttachedVersion_whenFormSavedAgainAfterAttaching() {
            // Given: Annual #12 is attached, and a later save created #1014
            attach(TicklerDocs.DOCTYPE_FORM, 12);
            givenFormVersions(form(1014, "Annual"), form(12, "Annual"), form(11, "Annual"));

            // When
            List<EctFormData.PatientForm> attached =
                    manager.getAttachedFormsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID);

            // Then
            assertThat(attached).extracting(EctFormData.PatientForm::getFormId).containsExactly("12");
        }

        @Test
        @DisplayName("should search every form version, not just the latest")
        void shouldRequestAllVersions_whenResolvingAttachedForms() {
            attach(TicklerDocs.DOCTYPE_FORM, 12);
            givenFormVersions();

            manager.getAttachedFormsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID);

            verify(mockFormsManager).getEncounterFormsByDemographicNumbers(
                    eq(mockLoggedInInfo), anyCollection(), eq(true), eq(true));
        }

        @Test
        @DisplayName("should skip a form id shared by two form types rather than guess")
        void shouldSkipFormId_whenTwoFormTypesShareIt() {
            attach(TicklerDocs.DOCTYPE_FORM, 7);
            givenFormVersions(form(7, "Annual"), form(7, "Rourke2020"));

            assertThat(manager.getAttachedFormsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID)).isEmpty();
        }

        @Test
        @DisplayName("should not query forms when no form is attached")
        void shouldNotQueryForms_whenNoFormAttached() {
            when(mockTicklerDocsDao.findByTicklerIdDocType(TICKLER_ID, TicklerDocs.DOCTYPE_FORM))
                    .thenReturn(Collections.emptyList());

            assertThat(manager.getAttachedFormsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID)).isEmpty();
            verify(mockFormsManager, never()).getEncounterFormsByDemographicNumbers(any(), anyCollection(), anyBoolean(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("Attached eForm and doc lookup")
    class AttachedEFormAndDocLookup {

        @Test
        @DisplayName("should return attached eForms including superseded versions")
        void shouldReturnAttachedEForms_whenSuperseded() {
            attach(TicklerDocs.DOCTYPE_EFORM, 33);
            EFormData superseded = eForm(33, DEMOGRAPHIC_NO, false);
            when(mockEFormDataDao.find(33)).thenReturn(superseded);

            assertThat(manager.getAttachedEFormsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID))
                    .containsExactly(superseded);
        }

        @Test
        @DisplayName("should drop an attached eForm that belongs to another patient")
        void shouldDropEForm_whenItBelongsToAnotherPatient() {
            attach(TicklerDocs.DOCTYPE_EFORM, 33);
            when(mockEFormDataDao.find(33)).thenReturn(eForm(33, 99999, true));

            assertThat(manager.getAttachedEFormsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID)).isEmpty();
        }

        @Test
        @DisplayName("should return attached docs and skip ones that no longer exist")
        void shouldReturnAttachedDocs_andSkipMissingOnes() {
            attach(TicklerDocs.DOCTYPE_DOC, 5, 6);
            EDoc doc = new EDoc();
            doc.setDocId("5");
            docsById.put("5", doc);

            assertThat(manager.getAttachedDocsForTickler(mockLoggedInInfo, DEMOGRAPHIC_NO_STR, TICKLER_ID))
                    .extracting(EDoc::getDocId).containsExactly("5");
        }
    }

    @Nested
    @DisplayName("Form merge")
    class FormMerge {

        @Test
        @DisplayName("should add a superseded attached version beneath the latest version of the same form")
        void shouldAddSupersededVersion_belowLatestOfSameForm() {
            List<EctFormData.PatientForm> current = Arrays.asList(form(1014, "Annual"), form(3, "Rourke2020"));

            List<EctFormData.PatientForm> merged = manager.mergeAttachedForms(current, Collections.singletonList(form(12, "Annual")));

            assertThat(merged).extracting(EctFormData.PatientForm::getFormId).containsExactly("1014", "12", "3");
        }

        @Test
        @DisplayName("should not duplicate an attached version that is already the latest")
        void shouldNotDuplicate_whenAttachedVersionIsLatest() {
            List<EctFormData.PatientForm> current = Collections.singletonList(form(12, "Annual"));

            List<EctFormData.PatientForm> merged = manager.mergeAttachedForms(current, Collections.singletonList(form(12, "Annual")));

            assertThat(merged).extracting(EctFormData.PatientForm::getFormId).containsExactly("12");
        }

        @Test
        @DisplayName("should leave the current list unchanged when nothing is attached")
        void shouldLeaveListUnchanged_whenNothingAttached() {
            List<EctFormData.PatientForm> current = Collections.singletonList(form(1014, "Annual"));

            assertThat(manager.mergeAttachedForms(current, Collections.emptyList()))
                    .extracting(EctFormData.PatientForm::getFormId).containsExactly("1014");
            assertThat(manager.mergeAttachedForms(current, null))
                    .extracting(EctFormData.PatientForm::getFormId).containsExactly("1014");
        }

        @Test
        @DisplayName("should append an attached form when no version of it is listed")
        void shouldAppend_whenFormNotListed() {
            List<EctFormData.PatientForm> merged =
                    manager.mergeAttachedForms(null, Collections.singletonList(form(12, "Annual")));

            assertThat(merged).extracting(EctFormData.PatientForm::getFormId).containsExactly("12");
        }

        @Test
        @DisplayName("should not modify the list it was given")
        void shouldNotModifyInputList() {
            List<EctFormData.PatientForm> current = Collections.singletonList(form(1014, "Annual"));

            manager.mergeAttachedForms(current, Collections.singletonList(form(12, "Annual")));

            assertThat(current).extracting(EctFormData.PatientForm::getFormId).containsExactly("1014");
        }
    }

    private void attach(String docType, int... documentNos) {
        List<TicklerDocs> ticklerDocs = new java.util.ArrayList<>();
        for (int documentNo : documentNos) {
            ticklerDocs.add(new TicklerDocs(TICKLER_ID, documentNo, docType, PROVIDER_NO));
        }
        when(mockTicklerDocsDao.findByTicklerIdDocType(TICKLER_ID, docType)).thenReturn(ticklerDocs);
    }

    private void givenFormVersions(EctFormData.PatientForm... forms) {
        when(mockFormsManager.getEncounterFormsByDemographicNumbers(eq(mockLoggedInInfo), anyCollection(), eq(true), eq(true)))
                .thenReturn(Map.of(DEMOGRAPHIC_NO, Arrays.asList(forms)));
    }

    private static EctFormData.PatientForm form(int formId, String formName) {
        Date now = new Date();
        return new EctFormData.PatientForm("form" + formName, formName, formId, DEMOGRAPHIC_NO, now, now);
    }

    private static EFormData eForm(int fdid, int demographicNo, boolean current) {
        EFormData eForm = new EFormData();
        eForm.setId(fdid);
        eForm.setDemographicId(demographicNo);
        eForm.setCurrent(current);
        return eForm;
    }

    private static Tickler tickler(int demographicNo) {
        Tickler tickler = new Tickler();
        tickler.setId(TICKLER_ID);
        tickler.setDemographicNo(demographicNo);
        return tickler;
    }

    private void injectDependency(Object target, String fieldName, Object dependency) {
        try {
            java.lang.reflect.Field field = DocumentAttachmentManagerImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, dependency);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject " + fieldName, e);
        }
    }
}
