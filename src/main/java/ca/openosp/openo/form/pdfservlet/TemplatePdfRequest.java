package ca.openosp.openo.form.pdfservlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Typed, server-controlled request for the legacy form template PDF engine.
 */
public final class TemplatePdfRequest {
    private static final Pattern SAFE_RESOURCE = Pattern.compile("[A-Za-z0-9_-]+");
    private static final Pattern SAFE_FORM_CLASS = Pattern.compile("[A-Za-z0-9_]+");

    private final String formClass;
    private final int demographicNo;
    private final int formId;
    private final String title;
    private final String template;
    private final List<String> printConfigs;
    private final List<String> graphicConfigs;
    private final Map<String, String> valueOverrides;

    /**
     * Creates a validated template-render request.
     */
    public TemplatePdfRequest(String formClass, int demographicNo, int formId, String title,
                              String template, List<String> printConfigs,
                              List<String> graphicConfigs, Map<String, String> valueOverrides) {
        if (formClass == null || !SAFE_FORM_CLASS.matcher(formClass).matches()) {
            throw new IllegalArgumentException("Invalid form class");
        }
        if (demographicNo <= 0 || formId <= 0) {
            throw new IllegalArgumentException("Form and demographic identifiers must be positive");
        }
        validateResource(template);
        for (String config : printConfigs) {
            validateResource(config);
        }
        for (String config : graphicConfigs) {
            validateResource(config);
        }
        this.formClass = formClass;
        this.demographicNo = demographicNo;
        this.formId = formId;
        this.title = title == null ? "" : title;
        this.template = template;
        this.printConfigs = Collections.unmodifiableList(new ArrayList<>(printConfigs));
        this.graphicConfigs = Collections.unmodifiableList(new ArrayList<>(graphicConfigs));
        this.valueOverrides = Collections.unmodifiableMap(new LinkedHashMap<>(valueOverrides));
    }

    private static void validateResource(String resource) {
        if (resource == null || !SAFE_RESOURCE.matcher(resource).matches()) {
            throw new IllegalArgumentException("Invalid PDF resource name");
        }
    }

    public String getFormClass() {
        return formClass;
    }

    public int getDemographicNo() {
        return demographicNo;
    }

    public int getFormId() {
        return formId;
    }

    public String getTitle() {
        return title;
    }

    public String getTemplate() {
        return template;
    }

    public List<String> getPrintConfigs() {
        return printConfigs;
    }

    public List<String> getGraphicConfigs() {
        return graphicConfigs;
    }

    public Map<String, String> getValueOverrides() {
        return valueOverrides;
    }
}
