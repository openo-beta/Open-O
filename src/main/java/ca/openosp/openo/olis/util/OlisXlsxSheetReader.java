package ca.openosp.openo.olis.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import ca.openosp.openo.util.UtilXML;

/**
 * Streaming reader for the XLSX (OOXML) workbooks consumed by the OLIS admin
 * importers — the eHealth Ontario Lab/SCC roster ({@code OLISFacilityImport2Action})
 * and the OLIS nomenclature extract ({@code OLISNomenclatureImport2Action}).
 *
 * <p>An XLSX file is a ZIP whose worksheets are XML. Rather than load Apache POI
 * (large, DOM-based), this reads the parts directly: shared strings, the
 * workbook-to-worksheet relationship map, and a row-by-row SAX pass over a
 * worksheet. All parsing uses {@link UtilXML#newSecureSAXParser()} so external
 * entities are disabled (XXE-safe).</p>
 *
 * <p>Rows are delivered to a {@link RowConsumer} keyed by header name: the first
 * worksheet row is treated as the header and every later non-empty row is handed
 * over as a {@code header -> cell value} map. Cells of shared-string type are
 * resolved against the {@code sharedStrings} list supplied by the caller.</p>
 *
 * @since 2026-06-01
 */
public final class OlisXlsxSheetReader {

    private OlisXlsxSheetReader() {
        // utility class — no instances
    }

    /**
     * Callback invoked once per data row (header row excluded).
     *
     * @since 2026-06-01
     */
    public interface RowConsumer {
        /**
         * @param row Map&lt;String, String&gt; the row as a {@code header -> cell value}
         *        map; only columns whose header cell was non-empty are present.
         *        <strong>The map is reused across rows</strong> — it is cleared and
         *        repopulated for the next row, so read what you need during this call
         *        and copy it ({@code new HashMap<>(row)}) if you need to retain it.
         */
        void accept(Map<String, String> row);
    }

    /**
     * Reads {@code xl/sharedStrings.xml} into an indexed list. XLSX cells of type
     * {@code "s"} store an index into this table rather than the literal text.
     *
     * @param zip ZipFile the open XLSX workbook
     * @return List&lt;String&gt; the shared strings in index order; empty if the
     *         workbook has no shared-strings part
     * @throws Exception if the part cannot be read or parsed
     * @since 2026-06-01
     */
    public static List<String> readSharedStrings(ZipFile zip) throws Exception {
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) {
            return new ArrayList<String>();
        }
        final List<String> strings = new ArrayList<String>();
        SAXParser parser = UtilXML.newSecureSAXParser();
        try (InputStream in = zip.getInputStream(entry)) {
            parser.parse(new InputSource(in), new DefaultHandler() {
                private StringBuilder buf = new StringBuilder();
                private boolean inT = false;
                private boolean inSi = false;
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    if ("si".equals(localName) || qName.endsWith(":si") || "si".equals(qName)) {
                        buf.setLength(0);
                        inSi = true;
                    } else if ("t".equals(localName) || qName.endsWith(":t") || "t".equals(qName)) {
                        inT = inSi;
                    }
                }
                @Override
                public void characters(char[] ch, int start, int length) {
                    if (inT) {
                        buf.append(ch, start, length);
                    }
                }
                @Override
                public void endElement(String uri, String localName, String qName) {
                    if ("si".equals(localName) || qName.endsWith(":si") || "si".equals(qName)) {
                        strings.add(buf.toString());
                        inSi = false;
                    } else if ("t".equals(localName) || qName.endsWith(":t") || "t".equals(qName)) {
                        inT = false;
                    }
                }
            });
        }
        return strings;
    }

    /**
     * Resolves the worksheet path of the first sheet listed in the workbook —
     * for single-sheet extracts like the Lab/SCC roster.
     *
     * @param zip ZipFile the open XLSX workbook
     * @return String the {@code xl/worksheets/...} path of the first sheet, or
     *         {@code null} if the workbook or its relationships part is missing
     * @throws Exception if a part cannot be read or parsed
     * @since 2026-06-01
     */
    public static String firstSheetPath(ZipFile zip) throws Exception {
        ZipEntry wbEntry = zip.getEntry("xl/workbook.xml");
        ZipEntry relsEntry = zip.getEntry("xl/_rels/workbook.xml.rels");
        if (wbEntry == null || relsEntry == null) {
            return null;
        }
        final Map<String, String> rIdToTarget = readRelIdToTarget(zip, relsEntry);
        final String[] firstTarget = new String[1];
        SAXParser p2 = UtilXML.newSecureSAXParser();
        try (InputStream in = zip.getInputStream(wbEntry)) {
            p2.parse(new InputSource(in), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    if (firstTarget[0] != null) return;
                    if ("sheet".equals(localName) || "sheet".equals(qName)) {
                        String rid = attrs.getValue("r:id");
                        if (rid == null) rid = attrs.getValue("id");
                        if (rid != null) {
                            firstTarget[0] = rIdToTarget.get(rid);
                        }
                    }
                }
            });
        }
        return firstTarget[0];
    }

    /**
     * Maps each worksheet name to its part path — for multi-sheet extracts like
     * the nomenclature workbook, which the caller addresses by sheet name.
     *
     * @param zip ZipFile the open XLSX workbook
     * @return Map&lt;String, String&gt; a {@code sheet name -> worksheet path} map;
     *         empty if the workbook or its relationships part is missing
     * @throws Exception if a part cannot be read or parsed
     * @since 2026-06-01
     */
    public static Map<String, String> workbookSheetMap(ZipFile zip) throws Exception {
        ZipEntry wbEntry = zip.getEntry("xl/workbook.xml");
        ZipEntry relsEntry = zip.getEntry("xl/_rels/workbook.xml.rels");
        if (wbEntry == null || relsEntry == null) {
            return new HashMap<String, String>();
        }
        final Map<String, String> rIdToTarget = readRelIdToTarget(zip, relsEntry);
        final Map<String, String> sheetNameToPath = new HashMap<String, String>();
        SAXParser p2 = UtilXML.newSecureSAXParser();
        try (InputStream in = zip.getInputStream(wbEntry)) {
            p2.parse(new InputSource(in), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    if ("sheet".equals(localName) || "sheet".equals(qName)) {
                        String name = attrs.getValue("name");
                        String rid = attrs.getValue("r:id");
                        if (rid == null) rid = attrs.getValue("id");
                        if (name != null && rid != null) {
                            String target = rIdToTarget.get(rid);
                            if (target != null) {
                                sheetNameToPath.put(name, target);
                            }
                        }
                    }
                }
            });
        }
        return sheetNameToPath;
    }

    /**
     * SAX-streams one worksheet, delivering each data row to {@code consumer}. The
     * first row is taken as the header; subsequent non-empty rows are mapped
     * {@code header -> value} (shared-string cells resolved via {@code sharedStrings}).
     *
     * @param zip ZipFile the open XLSX workbook
     * @param sheetPath String the worksheet part path (from {@link #firstSheetPath}
     *        or {@link #workbookSheetMap})
     * @param sharedStrings List&lt;String&gt; the shared-strings table from
     *        {@link #readSharedStrings}
     * @param consumer RowConsumer invoked once per data row
     * @throws IOException if the worksheet part is not present in the workbook
     * @throws Exception if the worksheet cannot be parsed
     * @since 2026-06-01
     */
    public static void streamRows(ZipFile zip, String sheetPath, final List<String> sharedStrings,
                                  final RowConsumer consumer) throws Exception {
        ZipEntry entry = zip.getEntry(sheetPath);
        if (entry == null) {
            throw new IOException("Worksheet entry not found: " + sheetPath);
        }
        SAXParser parser = UtilXML.newSecureSAXParser();
        try (InputStream in = zip.getInputStream(entry)) {
            parser.parse(new InputSource(in), new DefaultHandler() {
                private final Map<String, String> currentRow = new HashMap<String, String>();
                private final Map<String, String> columnLetterToHeader = new HashMap<String, String>();
                private String currentCellRef;
                private String currentCellType;
                private StringBuilder cellBuf = new StringBuilder();
                private boolean inValue = false;
                private boolean inInlineStr = false;
                private int rowIndex = 0;

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    String name = localName.isEmpty() ? qName : localName;
                    if ("row".equals(name)) {
                        currentRow.clear();
                        rowIndex++;
                    } else if ("c".equals(name)) {
                        currentCellRef = attrs.getValue("r");
                        currentCellType = attrs.getValue("t");
                        cellBuf.setLength(0);
                    } else if ("v".equals(name)) {
                        inValue = true;
                    } else if ("is".equals(name)) {
                        inInlineStr = true;
                    } else if ("t".equals(name) && inInlineStr) {
                        inValue = true;
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) {
                    if (inValue) {
                        cellBuf.append(ch, start, length);
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) {
                    String name = localName.isEmpty() ? qName : localName;
                    if ("v".equals(name)) {
                        inValue = false;
                    } else if ("t".equals(name) && inInlineStr) {
                        inValue = false;
                    } else if ("is".equals(name)) {
                        inInlineStr = false;
                    } else if ("c".equals(name)) {
                        String raw = cellBuf.toString();
                        String value;
                        if ("s".equals(currentCellType)) {
                            try {
                                value = sharedStrings.get(Integer.parseInt(raw.trim()));
                            } catch (Exception e) {
                                value = raw;
                            }
                        } else {
                            value = raw;
                        }
                        String column = columnLetterOnly(currentCellRef);
                        if (rowIndex == 1) {
                            columnLetterToHeader.put(column, value);
                        } else {
                            String header = columnLetterToHeader.get(column);
                            if (header != null) {
                                currentRow.put(header, value);
                            }
                        }
                    } else if ("row".equals(name)) {
                        if (rowIndex > 1 && !currentRow.isEmpty()) {
                            consumer.accept(currentRow);
                        }
                    }
                }
            });
        }
    }

    /**
     * Returns the leading column letters of an A1-style cell reference (e.g.
     * {@code "BC12" -> "BC"}).
     *
     * @param cellRef String the cell reference, may be {@code null}
     * @return String the column-letter prefix, or {@code ""} if {@code cellRef} is
     *         {@code null}
     * @since 2026-06-01
     */
    static String columnLetterOnly(String cellRef) {
        if (cellRef == null) return "";
        int i = 0;
        while (i < cellRef.length() && Character.isLetter(cellRef.charAt(i))) i++;
        return cellRef.substring(0, i);
    }

    /**
     * Parses {@code xl/_rels/workbook.xml.rels} into a {@code relationship id ->
     * worksheet target path} map, normalising each target to a workbook-root path.
     *
     * @param zip ZipFile the open XLSX workbook
     * @param relsEntry ZipEntry the already-resolved relationships part
     * @return Map&lt;String, String&gt; the {@code rId -> target} map
     * @throws Exception if the part cannot be parsed
     * @since 2026-06-01
     */
    private static Map<String, String> readRelIdToTarget(ZipFile zip, ZipEntry relsEntry) throws Exception {
        final Map<String, String> rIdToTarget = new HashMap<String, String>();
        SAXParser p1 = UtilXML.newSecureSAXParser();
        try (InputStream in = zip.getInputStream(relsEntry)) {
            p1.parse(new InputSource(in), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attrs) {
                    if ("Relationship".equals(localName) || "Relationship".equals(qName)) {
                        String id = attrs.getValue("Id");
                        String target = attrs.getValue("Target");
                        if (id != null && target != null) {
                            rIdToTarget.put(id, target.startsWith("/") ? target.substring(1) : "xl/" + target);
                        }
                    }
                }
            });
        }
        return rIdToTarget;
    }
}
