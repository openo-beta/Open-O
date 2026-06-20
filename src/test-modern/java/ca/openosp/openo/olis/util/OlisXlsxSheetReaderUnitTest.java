package ca.openosp.openo.olis.util;

import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OlisXlsxSheetReader}.
 *
 * <p>XLSX is a ZIP of XML parts. These tests assemble minimal-but-valid workbook
 * fixtures directly (no Apache POI dependency) and assert the reader recovers the
 * rows, shared strings, and sheet structure exactly as the OLIS Lab/SCC and
 * nomenclature importers rely on after the shared reader was extracted from
 * them.</p>
 *
 * @since 2026-06-01
 * @see OlisXlsxSheetReader
 */
@DisplayName("OlisXlsxSheetReader unit tests")
@Tag("unit")
@Tag("fast")
public class OlisXlsxSheetReaderUnitTest extends OpenOUnitTestBase {

    @TempDir
    Path tempDir;

    /**
     * Builds a minimal XLSX. Every cell is emitted as a shared string (type "s")
     * so the test also exercises shared-string index resolution. The first row of
     * each sheet is the header row, matching what {@link OlisXlsxSheetReader} expects.
     */
    private File writeXlsx(String[] sheetNames, String[][][] sheetRows) throws Exception {
        File file = tempDir.resolve("fixture-" + sheetNames.length + "-" + System.identityHashCode(sheetRows) + ".xlsx").toFile();
        List<String> shared = new ArrayList<>();
        StringBuilder workbookSheets = new StringBuilder();
        StringBuilder rels = new StringBuilder();
        Map<String, String> sheetParts = new LinkedHashMap<>();

        for (int s = 0; s < sheetNames.length; s++) {
            String rid = "rId" + (s + 1);
            String target = "worksheets/sheet" + (s + 1) + ".xml";
            workbookSheets.append("<sheet name=\"").append(xmlEsc(sheetNames[s]))
                    .append("\" r:id=\"").append(rid).append("\"/>");
            rels.append("<Relationship Id=\"").append(rid)
                    .append("\" Target=\"").append(target).append("\"/>");

            StringBuilder data = new StringBuilder("<worksheet><sheetData>");
            String[][] rows = sheetRows[s];
            for (int r = 0; r < rows.length; r++) {
                data.append("<row>");
                for (int c = 0; c < rows[r].length; c++) {
                    int idx = shared.size();
                    shared.add(rows[r][c]);
                    data.append("<c r=\"").append(colLetter(c)).append(r + 1)
                            .append("\" t=\"s\"><v>").append(idx).append("</v></c>");
                }
                data.append("</row>");
            }
            data.append("</sheetData></worksheet>");
            sheetParts.put("xl/" + target, data.toString());
        }

        StringBuilder sst = new StringBuilder("<sst>");
        for (String v : shared) {
            sst.append("<si><t>").append(xmlEsc(v)).append("</t></si>");
        }
        sst.append("</sst>");

        String workbookXml = "<workbook xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets>" + workbookSheets + "</sheets></workbook>";
        String relsXml = "<Relationships>" + rels + "</Relationships>";

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            put(zos, "xl/sharedStrings.xml", sst.toString());
            put(zos, "xl/workbook.xml", workbookXml);
            put(zos, "xl/_rels/workbook.xml.rels", relsXml);
            for (Map.Entry<String, String> e : sheetParts.entrySet()) {
                put(zos, e.getKey(), e.getValue());
            }
        }
        return file;
    }

    private static void put(ZipOutputStream zos, String name, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String colLetter(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private static String xmlEsc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    @Test
    @DisplayName("should map each data row by header name")
    void shouldMapRowsByHeader() throws Exception {
        File xlsx = writeXlsx(
                new String[]{"Sheet1"},
                new String[][][]{{
                        {"LOINC Code", "Result Alternate Name 1"},
                        {"14749-6", "Glucose"},
                        {"718-7", "Hemoglobin"}
                }});

        List<Map<String, String>> rows = new ArrayList<>();
        try (ZipFile zip = new ZipFile(xlsx)) {
            List<String> shared = OlisXlsxSheetReader.readSharedStrings(zip);
            String sheetPath = OlisXlsxSheetReader.firstSheetPath(zip);
            OlisXlsxSheetReader.streamRows(zip, sheetPath, shared, row -> rows.add(new HashMap<>(row)));
        }

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0))
                .containsEntry("LOINC Code", "14749-6")
                .containsEntry("Result Alternate Name 1", "Glucose");
        assertThat(rows.get(1))
                .containsEntry("LOINC Code", "718-7")
                .containsEntry("Result Alternate Name 1", "Hemoglobin");
    }

    @Test
    @DisplayName("should resolve worksheet path by sheet name for multi-sheet workbooks")
    void shouldResolveSheetByName() throws Exception {
        File xlsx = writeXlsx(
                new String[]{"Test Request Nomenclature", "Test Result Nomenclatures"},
                new String[][][]{
                        {{"Code", "Name"}, {"REQ-1", "Request One"}},
                        {{"Code", "Name"}, {"RES-1", "Result One"}}
                });

        try (ZipFile zip = new ZipFile(xlsx)) {
            Map<String, String> sheetMap = OlisXlsxSheetReader.workbookSheetMap(zip);
            assertThat(sheetMap).containsKeys("Test Request Nomenclature", "Test Result Nomenclatures");

            List<String> shared = OlisXlsxSheetReader.readSharedStrings(zip);
            List<Map<String, String>> resultRows = new ArrayList<>();
            OlisXlsxSheetReader.streamRows(zip, sheetMap.get("Test Result Nomenclatures"), shared, row -> resultRows.add(new HashMap<>(row)));

            assertThat(resultRows).hasSize(1);
            assertThat(resultRows.get(0)).containsEntry("Code", "RES-1").containsEntry("Name", "Result One");
        }
    }

    @Test
    @DisplayName("should populate the shared-strings table")
    void shouldReadSharedStrings() throws Exception {
        File xlsx = writeXlsx(
                new String[]{"Sheet1"},
                new String[][][]{{{"Header"}, {"Value A"}}});

        try (ZipFile zip = new ZipFile(xlsx)) {
            List<String> shared = OlisXlsxSheetReader.readSharedStrings(zip);
            assertThat(shared).contains("Header", "Value A");
        }
    }

    @Test
    @DisplayName("should ignore cells whose header column was blank")
    void shouldSkipUnmappedColumns() throws Exception {
        // Header row has only column A; the data row also fills column B, which
        // must be dropped because there is no header to key it on.
        File xlsx = writeXlsx(
                new String[]{"Sheet1"},
                new String[][][]{{
                        {"Kept"},
                        {"keepme", "dropme"}
                }});

        List<Map<String, String>> rows = new ArrayList<>();
        try (ZipFile zip = new ZipFile(xlsx)) {
            List<String> shared = OlisXlsxSheetReader.readSharedStrings(zip);
            OlisXlsxSheetReader.streamRows(zip, OlisXlsxSheetReader.firstSheetPath(zip), shared, row -> rows.add(new HashMap<>(row)));
        }

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)).containsEntry("Kept", "keepme");
        assertThat(rows.get(0)).doesNotContainValue("dropme");
    }

    @Test
    @DisplayName("columnLetterOnly should strip the row number from a cell reference")
    void columnLetterOnlyStripsRowNumber() {
        assertThat(OlisXlsxSheetReader.columnLetterOnly("BC12")).isEqualTo("BC");
        assertThat(OlisXlsxSheetReader.columnLetterOnly("A1")).isEqualTo("A");
        assertThat(OlisXlsxSheetReader.columnLetterOnly(null)).isEmpty();
    }
}
