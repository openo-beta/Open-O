//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */


package ca.openosp.openo.web;

import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.dao.ClinicDAO;
import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.dao.DrugDao;
import ca.openosp.openo.commn.dao.PrescriptionDao;
import ca.openosp.openo.commn.hl7.v2.oscar_to_oscar.OmpO09;
import ca.openosp.openo.commn.hl7.v2.oscar_to_oscar.OscarToOscarUtils;
import ca.openosp.openo.commn.model.Clinic;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.Drug;
import ca.openosp.openo.commn.model.Prescription;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.ProviderPreference;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.QrCodeUtils;
import ca.openosp.openo.utility.QrCodeUtils.QrCodesOrientation;
import ca.openosp.openo.utility.SpringUtils;
import ca.openosp.openo.web.admin.ProviderPreferencesUIBean;

import ca.openosp.OscarProperties;
import ca.uhn.hl7v2.model.v26.message.OMP_O09;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Prescription QR code generation utility for enhanced medication safety and interoperability.
 *
 * This utility class generates QR codes containing HL7-formatted prescription data to enhance
 * medication safety, reduce transcription errors, and improve prescription portability in
 * healthcare environments. The QR codes embed comprehensive prescription information including
 * patient demographics, provider details, medication specifications, and dosing instructions
 * in standardized HL7 v2.6 OMP_O09 message format.
 *
 * <p>QR codes on prescriptions provide multiple clinical benefits:</p>
 * <ul>
 *   <li>Reduces medication errors by eliminating manual data entry</li>
 *   <li>Enables rapid prescription verification by pharmacists</li>
 *   <li>Supports prescription portability between healthcare systems</li>
 *   <li>Facilitates medication reconciliation processes</li>
 *   <li>Provides backup method for prescription transmission</li>
 * </ul>
 *
 * <p>The generated QR codes use configurable error correction levels and scaling
 * factors to ensure reliable scanning across various devices and printing conditions.
 * Multiple QR codes may be generated for large prescriptions to maintain scanability
 * while preserving all essential clinical information.</p>
 *
 * <p>HL7 OMP_O09 message format ensures interoperability with other healthcare
 * systems and maintains standard clinical data structures for prescription exchange.
 * This enables integration with pharmacy systems, electronic health records, and
 * medication management platforms.</p>
 *
 * @since August 2010
 * @see ca.openosp.openo.commn.hl7.v2.oscar_to_oscar.OmpO09
 * @see ca.openosp.openo.utility.QrCodeUtils
 * @see ca.openosp.openo.commn.model.Prescription
 */
public final class PrescriptionQrCodeUIBean {

    private static final Logger logger = MiscUtils.getLogger();

    private static ClinicDAO clinicDAO = (ClinicDAO) SpringUtils.getBean(ClinicDAO.class);
    private static ProviderDao providerDao = (ProviderDao) SpringUtils.getBean(ProviderDao.class);
    private static DemographicDao demographicDao = (DemographicDao) SpringUtils.getBean(DemographicDao.class);
    private static PrescriptionDao prescriptionDao = (PrescriptionDao) SpringUtils.getBean(PrescriptionDao.class);
    private static DrugDao drugDao = (DrugDao) SpringUtils.getBean(DrugDao.class);

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private PrescriptionQrCodeUIBean() {
        // Utility class - not meant to be instantiated
    }

    /**
     * Generates a QR code image containing HL7-formatted prescription data.
     *
     * Creates a standardized QR code embedding comprehensive prescription information
     * in HL7 v2.6 OMP_O09 message format. The QR code includes patient demographics,
     * provider information, medication details, dosing instructions, and clinic
     * information to enable complete prescription verification and processing.
     *
     * <p>The generated QR code enhances medication safety by:</p>
     * <ul>
     *   <li>Eliminating manual transcription errors during prescription processing</li>
     *   <li>Providing rapid access to complete prescription details for pharmacists</li>
     *   <li>Enabling automated prescription verification workflows</li>
     *   <li>Supporting prescription portability across healthcare systems</li>
     * </ul>
     *
     * <p>For large prescriptions, multiple QR codes may be generated vertically
     * to maintain scanability while preserving all clinical information. The
     * error correction level and scaling factors are configurable to optimize
     * scanning reliability across different devices and printing conditions.</p>
     *
     * @param prescriptionId int unique identifier for the prescription to encode
     * @return byte[] PNG image data containing the QR code, or null if generation fails
     *
     * @see ca.openosp.openo.commn.hl7.v2.oscar_to_oscar.OmpO09#makeOmpO09
     * @see ca.openosp.openo.utility.QrCodeUtils#toMultipleQrCodePngs
     */
    public static byte[] getPrescriptionHl7QrCodeImage(int prescriptionId) {
        logger.debug("Generating QR Code for prescriptionId=" + prescriptionId);

        try {
            // Retrieve all prescription-related data
            Clinic clinic = clinicDAO.getClinic();
            Prescription prescription = prescriptionDao.find(prescriptionId);
            Provider provider = providerDao.getProvider(prescription.getProviderNo());
            Demographic demographic = demographicDao.getDemographicById(prescription.getDemographicId());
            List<Drug> drugs = drugDao.findByPrescriptionId(prescription.getId().intValue());

            // Create HL7 OMP_O09 message with complete prescription data
            OMP_O09 hl7PrescriptionMessage = OmpO09.makeOmpO09(clinic, provider, demographic, prescription, drugs);
            String hl7PrescriptionString = OscarToOscarUtils.pipeParser.encode(hl7PrescriptionMessage);
            logger.debug("HL7 prescription data: " + hl7PrescriptionString);

            // Generate QR code with configurable scaling and error correction
            int qrCodeScale = Integer.valueOf(OscarProperties.getInstance().getProperty("QR_CODE_IMAGE_SCALE_FACTOR"));
            byte[] image = QrCodeUtils.toMultipleQrCodePngs(hl7PrescriptionString, getEcLevel(), QrCodesOrientation.VERTICAL, qrCodeScale);

            return (image);
        } catch (Exception e) {
            logger.error("Failed to generate prescription QR code for prescriptionId=" + prescriptionId, e);
        }

        return (null);
    }

    /**
     * Retrieves the configured QR code error correction level for prescription scanning reliability.
     *
     * Maps configuration properties to ZXing library error correction levels to optimize
     * QR code scanning reliability in healthcare environments. Higher error correction
     * levels enable successful scanning even when QR codes are partially obscured or
     * damaged, which is critical for prescription safety.
     *
     * <p>Error correction levels provide different scanning reliability:</p>
     * <ul>
     *   <li>L (Low): ~7% error correction - suitable for clean printing conditions</li>
     *   <li>M (Medium): ~15% error correction - recommended for most applications</li>
     *   <li>Q (Quartile): ~25% error correction - better for challenging conditions</li>
     *   <li>H (High): ~30% error correction - maximum reliability for critical data</li>
     * </ul>
     *
     * <p>The method returns static ErrorCorrectionLevel instances to maintain
     * compatibility with the ZXing library's singleton pattern for error correction
     * level handling.</p>
     *
     * @return ErrorCorrectionLevel configured level for QR code generation
     */
    private static ErrorCorrectionLevel getEcLevel() {
        String ecLevelString = OscarProperties.getInstance().getProperty("QR_CODE_ERROR_CORRECTION_LEVEL");

        if ("L".equals(ecLevelString)) return (ErrorCorrectionLevel.L);
        if ("M".equals(ecLevelString)) return (ErrorCorrectionLevel.M);
        if ("Q".equals(ecLevelString)) return (ErrorCorrectionLevel.Q);
        if ("H".equals(ecLevelString)) return (ErrorCorrectionLevel.H);

        return null;
    }

    /**
     * Determines if prescription QR code printing is enabled for a specific healthcare provider.
     *
     * Checks provider-specific preferences to determine whether QR codes should be
     * included on printed prescriptions. This allows individual providers to control
     * QR code usage based on their practice patterns, patient populations, or
     * integration requirements with specific pharmacy systems.
     *
     * <p>Provider preferences enable flexible QR code adoption, allowing healthcare
     * facilities to implement QR codes gradually or selectively based on clinical
     * workflows and technology capabilities of partner pharmacies.</p>
     *
     * @param providerNo String healthcare provider identifier
     * @return boolean true if QR codes should be printed on this provider's prescriptions
     *
     * @see ca.openosp.openo.commn.model.ProviderPreference
     * @see ca.openosp.openo.web.admin.ProviderPreferencesUIBean
     */
    public static boolean isPrescriptionQrCodeEnabledForProvider(String providerNo) {
        ProviderPreference providerPreference = ProviderPreferencesUIBean.getProviderPreference(providerNo);
        if (providerPreference == null) providerPreference = new ProviderPreference();

        return (providerPreference.isPrintQrCodeOnPrescriptions());
    }
}
