package ca.openosp.openo.olis.model;

import ca.openosp.openo.commn.dao.Hl7TextInfoDao;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the OLIS display-sequence sort comparators
 * ({@link OlisLabResultSortable}, {@link OlisLabRequestSortable}) used to order
 * results/requests per the OLIS Sorting Quick Reference (CV04/05/06/15).
 *
 * <p>Each test asserts one rule from the OLIS ordering spec. The keys mirror the
 * "Sort N" columns of the reference sheet: for results — ancillary (OBX.11=Z) first,
 * then ZBX.2 sort key, then catalog nomenclature sort key, then alternate name 1,
 * then sub-ID (OBX.4), then release date (ZBX.1); for requests — collection date
 * (newest first), then group placer, then ZBR.11 sort key, then nomenclature sort
 * key, then alternate name 1, then OBR set ID. Empty values sort last at each
 * level.</p>
 *
 * <p>Extends {@link OpenOUnitTestBase} so {@code OLISUtils}'s SpringUtils static
 * initializer is mocked at class-load (the comparators call
 * {@code OLISUtils.compareStringEmptyIsMore}).</p>
 *
 * @since 2026-06-17
 */
@DisplayName("OLIS result/request sort comparators")
@Tag("unit")
@Tag("fast")
public class OlisLabSortableUnitTest extends OpenOUnitTestBase {

    /**
     * The comparators call {@code OLISUtils.compareStringEmptyIsMore}, and
     * {@code OLISUtils}'s static initializer resolves a {@code Hl7TextInfoDao} via
     * the mocked SpringUtils. Register it before any comparator runs so the
     * one-time class init succeeds.
     */
    @BeforeEach
    void registerOlisUtilsStaticDeps() {
        registerMock(Hl7TextInfoDao.class, Mockito.mock(Hl7TextInfoDao.class));
    }

    /** Build a result sortable; setId carries the original position (1-based). */
    private static OlisLabResultSortable result(int setId, String subId, String nomenSortKey,
                                                String altName, boolean ancillary,
                                                Date release, String zbxSortKey) {
        return new OlisLabResultSortable(setId, subId, nomenSortKey, altName, ancillary, release, zbxSortKey);
    }

    private static List<Integer> sortedSetIds(List<OlisLabResultSortable> in) {
        List<OlisLabResultSortable> copy = new ArrayList<>(in);
        Collections.sort(copy, OlisLabResultSortable.OLIS_RESULT_COMPARATOR);
        List<Integer> ids = new ArrayList<>();
        for (OlisLabResultSortable r : copy) {
            ids.add(r.getSetId());
        }
        return ids;
    }

    @Nested
    @DisplayName("result ordering (OBX)")
    class ResultOrdering {

        @Test
        @DisplayName("should place ancillary results (OBX.11=Z) before non-ancillary")
        void shouldPlaceAncillaryFirst() {
            OlisLabResultSortable nonAncillary = result(1, "", "", "Leukocytes", false, new Date(0), "0400.0015");
            OlisLabResultSortable ancillary = result(2, "", "", "Body Height", true, new Date(0), "8000.0.0");

            assertThat(sortedSetIds(Arrays.asList(nonAncillary, ancillary)))
                    .containsExactly(2, 1);
        }

        @Test
        @DisplayName("should order non-ancillary results by ZBX.2 sort key, empty last")
        void shouldOrderByZbxSortKeyEmptyLast() {
            OlisLabResultSortable a = result(1, "", "", "A", false, new Date(0), "0400.0240");
            OlisLabResultSortable b = result(2, "", "", "B", false, new Date(0), "0400.0015");
            OlisLabResultSortable noKey = result(3, "", "9999", "C", false, new Date(0), "");

            assertThat(sortedSetIds(Arrays.asList(a, b, noKey)))
                    .containsExactly(2, 1, 3);
        }

        @Test
        @DisplayName("should fall back to nomenclature sort key when ZBX.2 keys are equal")
        void shouldFallBackToNomenclatureSortKey() {
            OlisLabResultSortable a = result(1, "", "200000.064.200", "A", false, new Date(0), "3000.01");
            OlisLabResultSortable b = result(2, "", "200000.064.000", "B", false, new Date(0), "3000.01");

            assertThat(sortedSetIds(Arrays.asList(a, b)))
                    .containsExactly(2, 1);
        }

        @Test
        @DisplayName("should fall back to alternate name when ZBX.2 and nomenclature keys are equal/empty")
        void shouldFallBackToAlternateName() {
            OlisLabResultSortable monocytes = result(1, "", "", "Monocytes", false, new Date(0), "");
            OlisLabResultSortable basophils = result(2, "", "", "Basophils", false, new Date(0), "");

            assertThat(sortedSetIds(Arrays.asList(monocytes, basophils)))
                    .containsExactly(2, 1);
        }

        @Test
        @DisplayName("should fall back to sub-ID then release date for otherwise-equal results")
        void shouldFallBackToSubIdThenReleaseDate() {
            // equal on ancillary/zbx/nomen/altName -> sub-ID decides
            OlisLabResultSortable sub2 = result(1, "2", "", "Anti-H", false, new Date(1000), "");
            OlisLabResultSortable sub1 = result(2, "1", "", "Anti-H", false, new Date(2000), "");
            assertThat(sortedSetIds(Arrays.asList(sub2, sub1))).containsExactly(2, 1);

            // equal on everything incl. sub-ID -> earlier release date first
            OlisLabResultSortable later = result(3, "1", "", "Anti-H", false, new Date(5000), "");
            OlisLabResultSortable earlier = result(4, "1", "", "Anti-H", false, new Date(1000), "");
            assertThat(sortedSetIds(Arrays.asList(later, earlier))).containsExactly(4, 3);
        }
    }

    @Nested
    @DisplayName("request ordering (OBR)")
    class RequestOrdering {

        private OlisLabRequestSortable request(String name, int obrIndex, Date collDate,
                                               String groupPlacer, String zbr11SortKey,
                                               String nomenSortKey, String setId) {
            OLISRequestNomenclature nomen = new OLISRequestNomenclature();
            nomen.setName(name);
            nomen.setSortKey(nomenSortKey);
            return new OlisLabRequestSortable(name, obrIndex, collDate, groupPlacer, zbr11SortKey, nomen, setId);
        }

        private List<Integer> sortedObrIndexes(List<OlisLabRequestSortable> in) {
            List<OlisLabRequestSortable> copy = new ArrayList<>(in);
            Collections.sort(copy, OlisLabRequestSortable.OLIS_REQUEST_COMPARATOR);
            List<Integer> idx = new ArrayList<>();
            for (OlisLabRequestSortable r : copy) {
                idx.add(r.getObrIndex());
            }
            return idx;
        }

        @Test
        @DisplayName("should order requests by collection date, newest first")
        void shouldOrderByCollectionDateNewestFirst() {
            OlisLabRequestSortable older = request("A", 1, new Date(1_000_000), "", "", "", "1");
            OlisLabRequestSortable newer = request("B", 2, new Date(9_000_000), "", "", "", "1");

            assertThat(sortedObrIndexes(Arrays.asList(older, newer)))
                    .containsExactly(2, 1);
        }

        @Test
        @DisplayName("should sort null collection dates last")
        void shouldSortNullCollectionDateLast() {
            OlisLabRequestSortable dated = request("A", 1, new Date(1_000_000), "", "", "", "1");
            OlisLabRequestSortable undated = request("B", 2, null, "", "", "", "1");

            assertThat(sortedObrIndexes(Arrays.asList(undated, dated)))
                    .containsExactly(1, 2);
        }

        @Test
        @DisplayName("should fall back to group placer, then ZBR.11, then nomenclature sort key for equal dates")
        void shouldFallBackThroughRequestKeys() {
            Date sameDate = new Date(5_000_000);
            // differ only by group placer
            OlisLabRequestSortable gpA = request("A", 1, sameDate, "CV05_002", "", "", "1");
            OlisLabRequestSortable gpB = request("B", 2, sameDate, "CV05_001", "", "", "1");
            assertThat(sortedObrIndexes(Arrays.asList(gpA, gpB))).containsExactly(2, 1);

            // equal group placer -> ZBR.11 sort key
            OlisLabRequestSortable z1 = request("A", 1, sameDate, "CV05_001", "3004", "", "1");
            OlisLabRequestSortable z2 = request("B", 2, sameDate, "CV05_001", "3001", "", "1");
            assertThat(sortedObrIndexes(Arrays.asList(z1, z2))).containsExactly(2, 1);

            // equal group placer + ZBR.11 -> nomenclature sort key
            OlisLabRequestSortable n1 = request("A", 1, sameDate, "CV05_001", "", "200000.151.000", "1");
            OlisLabRequestSortable n2 = request("B", 2, sameDate, "CV05_001", "", "200000.109.000", "1");
            assertThat(sortedObrIndexes(Arrays.asList(n1, n2))).containsExactly(2, 1);
        }

        @Test
        @DisplayName("should not NPE when nomenclature sort key/name are null (pre-reseed rows)")
        void shouldTolerateNullNomenclatureFields() {
            OLISRequestNomenclature blank = new OLISRequestNomenclature(); // null sortKey + null name
            Date sameDate = new Date(5_000_000);
            OlisLabRequestSortable a = new OlisLabRequestSortable("A", 1, sameDate, "GP", "", blank, "1");
            OlisLabRequestSortable b = new OlisLabRequestSortable("B", 2, sameDate, "GP", "", blank, "2");

            List<OlisLabRequestSortable> list = new ArrayList<>(Arrays.asList(a, b));
            // must not throw
            Collections.sort(list, OlisLabRequestSortable.OLIS_REQUEST_COMPARATOR);
            assertThat(list).hasSize(2);
        }
    }
}
