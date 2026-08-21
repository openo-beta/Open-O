package ca.openosp.openo.form.pdfservlet;

import ca.openosp.openo.managers.NioFileManager;
import ca.openosp.openo.util.ConcatPDF;
import ca.openosp.openo.utility.PDFGenerationException;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Callable adapter around the legacy template-and-plot PDF engine.
 */
@Service
public class FormTemplatePdfRenderer {
    @Autowired
    private NioFileManager nioFileManager;

    /**
     * Renders one or more template pages into one temporary PDF.
     *
     * @param baseRequest authenticated request supplying the user's session
     * @param requests server-defined render requests
     * @return path to the generated temporary PDF
     * @throws PDFGenerationException when rendering fails
     */
    public Path render(HttpServletRequest baseRequest, List<TemplatePdfRequest> requests)
            throws PDFGenerationException {
        if (requests == null || requests.isEmpty()) {
            throw new PDFGenerationException("No form PDF pages were requested");
        }

        ArrayList<Object> renderedPages = new ArrayList<>();
        try {
            for (TemplatePdfRequest request : requests) {
                renderedPages.add(new ByteArrayInputStream(renderPage(baseRequest, request)));
            }
            try (ByteArrayOutputStream combined = new ByteArrayOutputStream()) {
                ConcatPDF.concat(renderedPages, combined);
                return nioFileManager.saveTempFile("growth_chart_" + System.currentTimeMillis(), combined);
            }
        } catch (IOException | DocumentException e) {
            throw new PDFGenerationException("The encounter form could not be rendered as a PDF", e);
        }
    }

    private byte[] renderPage(HttpServletRequest baseRequest, TemplatePdfRequest request)
            throws IOException, DocumentException {
        Map<String, String[]> parameters = new LinkedHashMap<>();
        parameters.put("form_class", new String[]{request.getFormClass()});
        parameters.put("demographic_no", new String[]{String.valueOf(request.getDemographicNo())});
        parameters.put("formId", new String[]{String.valueOf(request.getFormId())});
        parameters.put("__title", new String[]{request.getTitle()});
        parameters.put("__template", new String[]{request.getTemplate()});
        parameters.put("__cfgfile", request.getPrintConfigs().toArray(new String[0]));
        parameters.put("__cfgGraphicFile", request.getGraphicConfigs().toArray(new String[0]));
        for (Map.Entry<String, String> override : request.getValueOverrides().entrySet()) {
            parameters.put(override.getKey(), new String[]{override.getValue()});
        }

        HttpServletRequest isolatedRequest = new IsolatedParameterRequest(baseRequest, parameters);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            new AttachmentPdfEngine().render(isolatedRequest, baseRequest.getServletContext(), output);
            return output.toByteArray();
        }
    }

    private static final class AttachmentPdfEngine extends FrmPDFServlet {
        private void render(HttpServletRequest request, ServletContext context, ByteArrayOutputStream output)
                throws IOException, DocumentException {
            generatePDFDocumentBytes(request, context, output, 0);
        }
    }

    /**
     * Prevents unrelated consultation parameters and attributes from entering form rendering.
     */
    private static final class IsolatedParameterRequest extends HttpServletRequestWrapper {
        private final Map<String, String[]> parameters;

        private IsolatedParameterRequest(HttpServletRequest request, Map<String, String[]> parameters) {
            super(request);
            this.parameters = parameters;
        }

        @Override
        public String getParameter(String name) {
            String[] values = parameters.get(name);
            return values == null || values.length == 0 ? null : values[0];
        }

        @Override
        public String[] getParameterValues(String name) {
            return parameters.get(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Collections.unmodifiableMap(parameters);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(parameters.keySet());
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return Collections.emptyEnumeration();
        }
    }
}
