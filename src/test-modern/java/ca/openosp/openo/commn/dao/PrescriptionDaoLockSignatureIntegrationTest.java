package ca.openosp.openo.commn.dao;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ca.openosp.openo.commn.model.Prescription;
import ca.openosp.openo.test.base.OpenOTestBase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link PrescriptionDao#lockPrescriberSignature}: a prescription's signature is
 * saved once, with its text copy, and neither a second save nor saving a copy loaded earlier changes it.
 *
 * @since 2026-10-26
 */
@DisplayName("PrescriptionDao prescriber signature lock integration tests")
@Tag("integration")
@Tag("database")
@Tag("dao")
@Tag("update")
public class PrescriptionDaoLockSignatureIntegrationTest extends OpenOTestBase {

    private static final String UNDER_ARMSTRONG = "Alex Openodoc, PA. Prescribed under the authority of Dr. Ty Armstrong";
    private static final String UNDER_WELCH = "Alex Openodoc, PA. Prescribed under the authority of Dr. Kirsten Welch";

    @Autowired
    private PrescriptionDao prescriptionDao;

    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    private Prescription unprintedPrescription() {
        Prescription prescription = new Prescription();
        prescription.setProviderNo("999998");
        prescription.setDemographicId(1);
        prescription.setDatePrescribed(new Date());
        prescription.setTextView("Alex\nMHI Org\nAMOXICILLIN 125MG");
        prescriptionDao.persist(prescription);
        entityManager.flush();
        return prescription;
    }

    private Prescription reload(Prescription prescription) {
        entityManager.clear();
        return prescriptionDao.find(prescription.getId());
    }

    @Test
    @DisplayName("should save the signature and text copy when the prescription has no signature yet")
    void shouldSaveSignatureAndTextCopy_whenNoneSavedYet() {
        Prescription prescription = unprintedPrescription();

        int saved = prescriptionDao.lockPrescriberSignature(prescription.getId(), UNDER_ARMSTRONG, UNDER_ARMSTRONG + "\nMHI Org\nAMOXICILLIN 125MG");

        assertThat(saved).isEqualTo(1);
        assertThat(reload(prescription))
                .extracting(Prescription::getPrescriberSignature, Prescription::getTextView)
                .containsExactly(UNDER_ARMSTRONG, UNDER_ARMSTRONG + "\nMHI Org\nAMOXICILLIN 125MG");
    }

    @Test
    @DisplayName("should change nothing when the prescription already has a signature")
    void shouldChangeNothing_whenSignatureAlreadySaved() {
        Prescription prescription = unprintedPrescription();
        prescriptionDao.lockPrescriberSignature(prescription.getId(), UNDER_ARMSTRONG, UNDER_ARMSTRONG + "\nMHI Org\nAMOXICILLIN 125MG");

        int saved = prescriptionDao.lockPrescriberSignature(prescription.getId(), UNDER_WELCH, UNDER_WELCH + "\nMHI Org\nAMOXICILLIN 125MG");

        assertThat(saved).isZero();
        assertThat(reload(prescription))
                .extracting(Prescription::getPrescriberSignature, Prescription::getTextView)
                .containsExactly(UNDER_ARMSTRONG, UNDER_ARMSTRONG + "\nMHI Org\nAMOXICILLIN 125MG");
    }

    @Test
    @DisplayName("should keep the signature and text copy when a copy loaded before they were saved is saved")
    void shouldKeepSignatureAndTextCopy_whenCopyLoadedBeforeTheyWereSavedIsSaved() {
        Prescription prescription = unprintedPrescription();
        // Loaded and let go, as PrescriptionManager.print() and setPrescriptionSignature() do in their own transactions
        Prescription loadedBefore = reload(prescription);
        entityManager.clear();
        prescriptionDao.lockPrescriberSignature(prescription.getId(), UNDER_ARMSTRONG, UNDER_ARMSTRONG + "\nMHI Org\nAMOXICILLIN 125MG");

        loadedBefore.setDigitalSignatureId(7);
        prescriptionDao.merge(loadedBefore);
        entityManager.flush();

        assertThat(reload(prescription))
                .extracting(Prescription::getPrescriberSignature, Prescription::getTextView, Prescription::getDigitalSignatureId)
                .containsExactly(UNDER_ARMSTRONG, UNDER_ARMSTRONG + "\nMHI Org\nAMOXICILLIN 125MG", 7);
    }
}
