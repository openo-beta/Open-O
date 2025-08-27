//CHECKSTYLE:OFF
package oscar.oscarDemographic.pageUtil;

import cds.LaboratoryResultsDocument;
import cdsDt.DateTimeFullOrPartial;
import org.apache.logging.log4j.Logger;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.util.MiscUtils;
import oscar.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class HL7CreateFile {
    private Demographic demographic;
    String LAB_TYPE = "CML";
    Integer resultCount = 1;
    private static final Logger logger = MiscUtils.getLogger();
    private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat inputDateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat xmlTimezoneOffSetDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final SimpleDateFormat fullDateTime = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat fullDate = new SimpleDateFormat("yyyyMMdd");


    public HL7CreateFile(Demographic demographic) {
        this.demographic = demographic;
    }

    public String generateHL7(List<LaboratoryResultsDocument.LaboratoryResults> labs) {
        StringBuilder hl7 = new StringBuilder();

        if (labs != null && !labs.isEmpty()) {
            resultCount = labs.size();
            LaboratoryResultsDocument.LaboratoryResults firstLab = labs.get(0);

            String labType = "";
            if (StringUtils.filled(firstLab.getLaboratoryName())) {
                if (firstLab.getLaboratoryName().split("\\^").length > 1) {
                    labType = StringUtils.noNull(firstLab.getLaboratoryName().split("\\^")[1]);
                } else {
                    labType = StringUtils.noNull(firstLab.getLaboratoryName());
                }
            }
            if (labType.equalsIgnoreCase("LifeLabs") || labType.equalsIgnoreCase("MDS")) {
                LAB_TYPE = "MDS";
            } else if (labType.equalsIgnoreCase("Gamma") || labType.equalsIgnoreCase("GDML")) {
                LAB_TYPE = "GDML";
            } else if (labType.equalsIgnoreCase("PATHL7")) {
                LAB_TYPE = "PATHL7";
            } else if (labType.equalsIgnoreCase("ExcellerisON")) {
                LAB_TYPE = "ExcellerisON";
            }

            hl7.append(generateMSH(firstLab)).append("\n");

            if (LAB_TYPE.equals("MDS")) {
                hl7.append("ZLB||||||||||||||||").append("\n");
                hl7.append(generateZRG(labs));
                hl7.append(generateZMN(labs));
                hl7.append(generateZMC(labs));
                hl7.append("ZCL||^^^^^^^^^^^^|^^^|||||||||").append("\n");
            }

            hl7.append(generatePID(demographic, firstLab)).append("\n");
            if (firstLab.getBlockedTestResult() != null && "Y".equals(firstLab.getBlockedTestResult().toString())) {
                hl7.append("ZPD|||Y|").append("\n");
            }

            if (LAB_TYPE.equals("MDS")) {
                hl7.append("PV1||R|^^^^^^^^|||||^^^^^|||||||||^^^^^^^^^^^^||||||||||||||||||||||||1|||").append("\n");
                hl7.append(generateZFR(firstLab)).append("\n");
                hl7.append("ZCT|||||||").append("\n");
            }

            if (LAB_TYPE.equals("CML") || LAB_TYPE.equals("PATHL7") || LAB_TYPE.equals("ExcellerisON")) {
                hl7.append(generateORC(firstLab)).append("\n");
            }

            hl7.append(generateOBR(firstLab)).append("\n");
            hl7.append(generateOBX(labs));

            if (LAB_TYPE.equals("PATHL7") || LAB_TYPE.equals("ExcellerisON")) {
                addXMLWrapper(hl7);
            }
        }

        return hl7.toString();
    }


    private String generateMSH(LaboratoryResultsDocument.LaboratoryResults lab) {
        String labName = "";
        if (StringUtils.filled(lab.getLaboratoryName())) {
            if (lab.getLaboratoryName().split("\\^").length > 1) {
                labName = StringUtils.noNull(lab.getLaboratoryName().split("\\^")[0]);
            } else {
                labName = StringUtils.noNull(lab.getLaboratoryName());
            }
        }
        String labNameType = LAB_TYPE + "|" + labName;
        DateTimeFullOrPartial labDateString = lab.getLabRequisitionDateTime() != null ? lab.getLabRequisitionDateTime() : lab.getCollectionDateTime();
        String requisitionDate = getDateTime(labDateString);
        String version = "2.3";
        if (LAB_TYPE.equals("MDS")) {
            labNameType = labName + "|" + LAB_TYPE;
            version = version + ".0";
        }
        if (LAB_TYPE.equals("ExcellerisON")) {
            labNameType = "PATHL7" + "|" + labName;
            version = version + ".1";
        }

        return "MSH|^~\\&|" + labNameType + "|||" + requisitionDate + "||ORU^R01|" + StringUtils.noNull(lab.getAccessionNumber()) + "-" + resultCount + "|P|" + version + "||||";
    }

    private String generateNTE(LaboratoryResultsDocument.LaboratoryResults lab) {
        StringBuilder nte = new StringBuilder();

        if (StringUtils.filled(lab.getNotesFromLab())) {
            if (LAB_TYPE.equals("MDS")) {
                nte.append("NTE||MC|^").append(lab.getLabTestCode()).append("\n");
            } else {
                String[] noteParts = lab.getNotesFromLab().split("\n");

                StringBuilder nteSegment = new StringBuilder();
                for (int n = 0; n < noteParts.length; n++) {
                    int noteNum = (n + 1);
                    nteSegment.append("NTE|" + noteNum + "|L|" + noteParts[n]).append("\n");
                }

                nte.append(nteSegment.toString());
            }
        }

        return nte.toString();
    }

    private String generateOBR(LaboratoryResultsDocument.LaboratoryResults lab) {
        DateTimeFullOrPartial reqDate = lab.getLabRequisitionDateTime();
        DateTimeFullOrPartial collectDate = lab.getCollectionDateTime();
        String requisitionDate = getDateTime(reqDate != null ? reqDate : collectDate);
        String collectionDate = getDateTime(collectDate != null ? collectDate : reqDate);
        String orderObservation = "";

        if (!LAB_TYPE.equals("GDML")) {
            orderObservation = "1";
        }

        return "OBR|" + orderObservation + "|101||" + lab.getLabTestCode() + "^" + lab.getTestNameReportedByLab() + "^0000^Imported Test Results|R|" + requisitionDate + "|" + collectionDate + "|||||||" + requisitionDate + "||||||||" + collectionDate + "||LAB|F|||";
    }

    private String generateOBX(List<LaboratoryResultsDocument.LaboratoryResults> labs) {
        int obxNo = 0;
        StringBuilder obx = new StringBuilder();

        for (LaboratoryResultsDocument.LaboratoryResults lab : labs) {
            String result = "";
            String unit = "";
            String valueType = "ST";
            if (lab.getResult() != null) {
                result = StringUtils.noNull(lab.getResult().getValue());
                result = result.replaceAll("\\n", "<br \\\\>");
                if (isBase64Pdf(result)) {
                    if (!LAB_TYPE.equals("ExcellerisON")) { result = "^TEXT^PDF^Base64^" + result; }
                    valueType = "ED";
                }

                unit = StringUtils.noNull(lab.getResult().getUnitOfMeasure());
            }
            String collectionDate = getDateTime(lab.getCollectionDateTime());
            String referenceRange = "";
            String resultNormalAbnormalFlag = "";
            String testResultStatus = StringUtils.noNull(lab.getTestResultStatus());

            if (lab.getResultNormalAbnormalFlag() != null) {
                if (lab.getResultNormalAbnormalFlag().isSetResultNormalAbnormalFlagAsPlainText()) {
                    resultNormalAbnormalFlag = lab.getResultNormalAbnormalFlag().getResultNormalAbnormalFlagAsPlainText();
                } else if (lab.getResultNormalAbnormalFlag().isSetResultNormalAbnormalFlagAsEnum()) {
                    resultNormalAbnormalFlag = lab.getResultNormalAbnormalFlag().getResultNormalAbnormalFlagAsEnum().toString();
                }
            }
            if (lab.getReferenceRange() != null) {
                if (lab.getReferenceRange().getReferenceRangeText() != null) {
                    referenceRange = lab.getReferenceRange().getReferenceRangeText();
                } else if (lab.getReferenceRange().getLowLimit() != null && lab.getReferenceRange().getHighLimit() != null) {
                    referenceRange = lab.getReferenceRange().getLowLimit() + "-" + lab.getReferenceRange().getHighLimit();
                }
            }

            obxNo += 1;
            String labTest = lab.getLabTestCode() + "^" + lab.getTestNameReportedByLab() + "^" + lab.getTestName();

            if (isFinal(testResultStatus)) {
                testResultStatus = "F";
            }

            String obxSegment = "OBX|" + obxNo + "|" + valueType + "|" + labTest+ "|Imported Test Results|" + result+ "|" +unit+ "|" + referenceRange + "|" + resultNormalAbnormalFlag+ "|||" + testResultStatus + "|||" + collectionDate;
            obx.append(obxSegment).append("\n");
            obx.append(generateNTE(lab));
        }

        return obx.toString();
    }

    private String generateORC(LaboratoryResultsDocument.LaboratoryResults lab) {
        String collectionDate = getDateTime(lab.getCollectionDateTime());
        String testResultStatus = StringUtils.noNull(lab.getTestResultStatus());
        if (isFinal(testResultStatus)) {
            testResultStatus = "F";
        }

        return "ORC|RE|" + lab.getAccessionNumber() + "|||" + testResultStatus + "||||||||||" + collectionDate;
    }

    private String generatePID(Demographic demographic, LaboratoryResultsDocument.LaboratoryResults lab) {
        String demographicPhone = StringUtils.noNull(demographic.getPhone());
        String demographicPhone2 = StringUtils.noNull(demographic.getPhone2());
        String healthCard = StringUtils.noNull(demographic.getHin());
        String pid19 = healthCard + " " + StringUtils.noNull(demographic.getVer());
        if (LAB_TYPE.equals("MDS")) {
            pid19 = "X" + healthCard;
        }

        return "PID|1|" + StringUtils.noNull(demographic.getHin()) + "|" + lab.getAccessionNumber() + "|" + healthCard + "|" + demographic.getLastName() + "^" + demographic.getFirstName() + "||" + fullDate.format(demographic.getBirthDay().getTime()) + "|" + demographic.getSex() + "|||||" + demographicPhone + "|" + demographicPhone2 + "|||||" + pid19;
    }

    private String generateZFR(LaboratoryResultsDocument.LaboratoryResults lab) {
        String testResultStatus = StringUtils.noNull(lab.getTestResultStatus());
        if (isFinal(testResultStatus)) {
            testResultStatus = "1";
        } else {
            testResultStatus = "0";
        }

        return "ZFR||1|" + testResultStatus + "|||0|0";
    }

    private String generateZMC(List<LaboratoryResultsDocument.LaboratoryResults> labs) {
        StringBuilder zmc = new StringBuilder();
        Integer zmcNo = 0;

        for (LaboratoryResultsDocument.LaboratoryResults lab : labs) {
            zmcNo += 1;
            if (StringUtils.filled(lab.getNotesFromLab())) {
                String[] noteParts = lab.getNotesFromLab().split("\n");

                StringBuilder zmcSegment = new StringBuilder();
                for (int n = 0; n < noteParts.length; n++) {
                    int noteNum = (n + 1);
                    zmcSegment.append("ZMC|" + zmcNo + "." + (n + 1) + "|" + lab.getLabTestCode() + "||" + noteParts.length + "|Y|" + noteParts[n]).append("\n");
                }

                zmc.append(zmcSegment.toString());
            }
        }

        return zmc.toString();
    }

    private String generateZMN(List<LaboratoryResultsDocument.LaboratoryResults> labs) {
        StringBuilder zmn = new StringBuilder();

        for (LaboratoryResultsDocument.LaboratoryResults lab : labs) {
            if (lab.getResult() != null) {
                String referenceRange = "";
                String unit = StringUtils.noNull(lab.getResult().getUnitOfMeasure());

                if (lab.getReferenceRange() != null) {
                    if (lab.getReferenceRange().getReferenceRangeText() != null) {
                        referenceRange = lab.getReferenceRange().getReferenceRangeText();
                    } else if (lab.getReferenceRange().getLowLimit() != null && lab.getReferenceRange().getHighLimit() != null) {
                        referenceRange = lab.getReferenceRange().getLowLimit() + "-" + lab.getReferenceRange().getHighLimit();
                    }
                }

                String zmnSegment = "ZMN||" + lab.getTestNameReportedByLab() + "||" + lab.getTestName() + "|" + unit + "||" + referenceRange + "|Imported Test Results||" + lab.getLabTestCode();

                zmn.append(zmnSegment).append("\n");
            }
        }

        return zmn.toString();
    }

    private String generateZRG(List<LaboratoryResultsDocument.LaboratoryResults> labs) {
        StringBuilder zrg = new StringBuilder();
        int zrgNo = 0;

        for (LaboratoryResultsDocument.LaboratoryResults lab : labs) {
            if (lab.getResult() != null) {
                zrgNo += 1;
                String zrgSegment = "ZRG|" + zrgNo + ".1|" + lab.getLabTestCode() + "|||Imported Test Results|1|";
                zrg.append(zrgSegment).append("\n");
            }
        }

        return zrg.toString();
    }

    /**
     * Attempts to parse a Date object from the provided DateTimeFullOrPartial
     *
     * @param dateObj The provided DateTimeFullOrPartial object
     * @return A parsed date string of the DateTimeFullOrPartial or if not parsable it takes the current Date()
     */
    private String getDateTime(DateTimeFullOrPartial dateObj) {
        Date date = null;
        if (dateObj != null) {
            SimpleDateFormat[] formats = {inputFormat, xmlTimezoneOffSetDateTime, inputDateOnlyFormat};
            for (SimpleDateFormat format : formats) {
                try {
                    if (dateObj.isSetFullDate()) {
                        date = format.parse(dateObj.getFullDate().toString() + " 00:00:00");
                    } else if (dateObj.isSetFullDateTime()) {
                        date = format.parse(dateObj.getFullDateTime().toString());
                    }
                } catch (ParseException e) { /* Do nothing */ }
                if (date != null) {
                    break;
                }
            }
        }
        if (date == null) {
            date = new Date();
        }

        return fullDateTime.format(date);
    }

    private boolean isFinal(String testResultStatus) {
        testResultStatus = StringUtils.noNull(testResultStatus);

        return testResultStatus.equalsIgnoreCase("Final") || testResultStatus.isEmpty();
    }

    private boolean isBase64Pdf(String str) {
        // Check if the string is null or empty
        if (str == null || str.isEmpty()) {
            return false; // Null or empty strings are not valid Base64
        }

        try {
            // Attempt to decode the string as Base64
            byte[] decodedBytes = Base64.getDecoder().decode(str);

            // Check if the decoded bytes represent a PDF file
            // PDF files start with the signature "%PDF-" (in ASCII)
            String pdfSignature = "%PDF-";
            if (decodedBytes.length < pdfSignature.length()) {
                return false; // Not enough bytes to match the PDF signature
            }

            // Convert the first few bytes to a string and compare with the PDF signature
            String header = new String(decodedBytes, 0, pdfSignature.length(), "UTF-8");
            return pdfSignature.equals(header);

        } catch (IllegalArgumentException e) {
            // Decoding failed, so it's not valid Base64
            return false;
        } catch (Exception e) {
            // Handle unexpected exceptions (e.g., character encoding issues)
            return false;
        }
    }

    private void addXMLWrapper(StringBuilder hl7Message) {
        if (hl7Message == null || hl7Message.length() == 0) {
            throw new IllegalArgumentException("HL7 message cannot be null or empty");
        }

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                  .append("<HL7Messages MessageFormat=\"ORUR01\" MessageCount=\"1\" Version=\"2.3\">")
                  .append("<Message MsgID=\"1\"><![CDATA[")
                  .append(hl7Message)
                  .append("]]></Message>")
                  .append("</HL7Messages>");

        hl7Message.setLength(0); // Clear the original content
        hl7Message.append(xmlBuilder); // Replace it with the XML-wrapped content
    }
}
