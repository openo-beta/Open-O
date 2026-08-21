package ca.openosp.openo.form.pdfservlet;

import ca.openosp.openo.encounter.data.EctFormData;
import ca.openosp.openo.form.FrmGrowth0_36Record;
import ca.openosp.openo.form.FrmGrowthChartRecord;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.PDFGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Renders saved standalone growth-chart encounter forms for consultation attachments.
 */
@Service
public class GrowthChartConsultPdfRenderer {
    @Autowired
    private FormTemplatePdfRenderer templateRenderer;

    /**
     * Renders both clinically relevant sheets for a saved growth-chart form.
     */
    public Path render(HttpServletRequest request, EctFormData.PatientForm form)
            throws PDFGenerationException {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        int demographicNo = Integer.parseInt(form.getDemoNo());
        int formId = Integer.parseInt(form.getFormId());
        try {
            if ("formGrowthChart".equals(form.getTable())) {
                Properties values = new FrmGrowthChartRecord().getFormRecord(
                        loggedInInfo, demographicNo, formId);
                return templateRenderer.render(request, growthChartRequests(
                        demographicNo, formId, values));
            }
            if ("formGrowth0_36".equals(form.getTable())) {
                Properties values = new FrmGrowth0_36Record().getFormRecord(
                        loggedInInfo, demographicNo, formId);
                return templateRenderer.render(request, infantGrowthRequests(
                        demographicNo, formId, values));
            }
        } catch (SQLException e) {
            throw new PDFGenerationException("The saved growth chart could not be loaded", e);
        }
        throw new PDFGenerationException("Unsupported growth chart form");
    }

    private List<TemplatePdfRequest> growthChartRequests(
            int demographicNo, int formId, Properties values) {
        boolean girl = "F".equals(values.getProperty("patientSex", ""));
        String sex = girl ? "Girl" : "Boy";
        Map<String, String> window = GrowthChartWindowMapper.mostRecentWindow(
                values, List.of("date", "age", "stature", "weight", "comment", "bmi"), 42, 7);

        List<TemplatePdfRequest> requests = new ArrayList<>();
        requests.add(new TemplatePdfRequest(
                "GrowthChart", demographicNo, formId, "Growth Charts",
                "growthChart" + sex + "StatureWeight",
                List.of("growthChart" + sex + "Print"),
                List.of("growthChart" + sex + "Graphic", "growthChart" + sex + "Graphic2"),
                window));
        requests.add(new TemplatePdfRequest(
                "GrowthChart", demographicNo, formId, "Growth Charts",
                "growthChart" + sex + "BMI",
                List.of("growthChart" + sex + "BMIPrint"),
                List.of("growthChart" + sex + "GraphicBMI"),
                window));
        return requests;
    }

    private List<TemplatePdfRequest> infantGrowthRequests(
            int demographicNo, int formId, Properties values) {
        boolean girl = "F".equals(values.getProperty("patientSex", ""));
        String sex = girl ? "Girl" : "Boy";
        Map<String, String> lengthWindow = GrowthChartWindowMapper.mostRecentWindow(
                values, List.of("date", "age", "length", "weight", "comment", "headCirc"), 20, 10);
        Map<String, String> headWindow = GrowthChartWindowMapper.mostRecentWindow(
                values, List.of("date", "age", "length", "weight", "comment", "headCirc"), 20, 5);

        List<TemplatePdfRequest> requests = new ArrayList<>();
        requests.add(new TemplatePdfRequest(
                "Growth0_36", demographicNo, formId, "Growth Charts",
                "growth" + sex + "Length0_36",
                List.of("growth" + sex + "Length0_36Print"),
                List.of("growth" + sex + "Length0_36Graphic", "growth" + sex + "Length0_36Graphic2"),
                lengthWindow));
        requests.add(new TemplatePdfRequest(
                "Growth0_36", demographicNo, formId, "Growth Charts",
                "growth" + sex + "Head0_36",
                List.of("growth" + sex + "Head0_36Print"),
                List.of("growth" + sex + "Head0_36Graphic", "growth" + sex + "Head0_36Graphic2"),
                headWindow));
        return requests;
    }
}
