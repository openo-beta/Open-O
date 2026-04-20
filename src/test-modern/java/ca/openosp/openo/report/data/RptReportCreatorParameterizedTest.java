/**
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
 */
package ca.openosp.openo.report.data;

import org.junit.jupiter.api.*;

import java.util.Vector;

import ca.openosp.openo.util.ParameterizedClause;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link RptReportCreator#getWhereValueClauseParameterized(String, Vector)}.
 *
 * @since 2026-04-17
 */
@DisplayName("RptReportCreator.getWhereValueClauseParameterized")
@Tag("unit")
@Tag("fast")
class RptReportCreatorParameterizedTest {

    private static Vector<String> values(String... xs) {
        Vector<String> v = new Vector<>();
        for (String x : xs) v.add(x);
        return v;
    }

    @Test
    void shouldReturnEmptyForNullTemplate() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(null, new Vector<>());
        assertThat(c.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnEmptyForEmptyTemplate() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized("", new Vector<>());
        assertThat(c.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnLiteralWhenTemplateHasNoTokens() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "demographic.last_name = 'smith'", new Vector<>());
        assertThat(c.sql()).isEqualTo("demographic.last_name = 'smith'");
        assertThat(c.params()).isEmpty();
    }

    @Test
    void shouldReplaceUnquotedToken() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "demographic.id = ${id}", values("42"));
        assertThat(c.sql()).isEqualTo("demographic.id = ?");
        assertThat(c.params()).containsExactly("42");
    }

    @Test
    void shouldStripSurroundingSingleQuotes() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "demographic.last_name = '${name}'", values("O'Brien"));
        assertThat(c.sql()).isEqualTo("demographic.last_name = ?");
        assertThat(c.params()).containsExactly("O'Brien");
    }

    @Test
    void shouldStripSurroundingDoubleQuotes() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "demographic.last_name = \"${name}\"", values("Sánchez"));
        assertThat(c.sql()).isEqualTo("demographic.last_name = ?");
        assertThat(c.params()).containsExactly("Sánchez");
    }

    @Test
    void shouldNotStripMismatchedQuotes() {
        // Leading ' but trailing " — leave both as-is
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "name = '${x}\"", values("v"));
        assertThat(c.sql()).isEqualTo("name = '?\"");
        assertThat(c.params()).containsExactly("v");
    }

    @Test
    void shouldHandleMultipleTokensInPositionalOrder() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "a = '${a}' and b = '${b}'", values("v1", "v2"));
        assertThat(c.sql()).isEqualTo("a = ? and b = ?");
        assertThat(c.params()).containsExactly("v1", "v2");
    }

    @Test
    void shouldConsumeOneValuePerOccurrenceEvenWhenTokenNamesRepeat() {
        // The legacy code walks positionally, so "${x}...${x}" consumes two vec slots.
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "a = '${x}' or b = '${x}'", values("first", "second"));
        assertThat(c.sql()).isEqualTo("a = ? or b = ?");
        assertThat(c.params()).containsExactly("first", "second");
    }

    @Test
    void shouldBindEmptyStringWhenValueIsNull() {
        // Preserves the legacy substitution behaviour: null became "" in the output.
        Vector<String> vs = new Vector<>();
        vs.add(null);
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "name = '${x}'", vs);
        assertThat(c.sql()).isEqualTo("name = ?");
        assertThat(c.params()).containsExactly("");
    }

    @Test
    void shouldBindEmptyStringWhenVectorHasFewerValuesThanTokens() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "a = '${a}' and b = '${b}'", values("only_a"));
        assertThat(c.sql()).isEqualTo("a = ? and b = ?");
        assertThat(c.params()).containsExactly("only_a", "");
    }

    @Test
    void shouldHandleTokenAtStartOfTemplate() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "${x} = 42", values("col"));
        assertThat(c.sql()).isEqualTo("? = 42");
        assertThat(c.params()).containsExactly("col");
    }

    @Test
    void shouldHandleTokenAtEndOfTemplateWithQuotes() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "name = '${x}'", values("val"));
        assertThat(c.sql()).isEqualTo("name = ?");
        assertThat(c.params()).containsExactly("val");
    }

    @Test
    void shouldLeaveMalformedOpeningTokenAlone() {
        // "${" without a closing "}" — copy remainder verbatim, don't consume a value.
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "name = ${unclosed", values("ignored"));
        assertThat(c.sql()).isEqualTo("name = ${unclosed");
        assertThat(c.params()).isEmpty();
    }

    @Test
    void shouldFoldTrailingLikeWildcardIntoValue() {
        // "name like '${prefix}%'"  →  "name like ?"  bound "John%"
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "name like '${prefix}%'", values("John"));
        assertThat(c.sql()).isEqualTo("name like ?");
        assertThat(c.params()).containsExactly("John%");
    }

    @Test
    void shouldFoldLeadingLikeWildcardIntoValue() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "name like '%${suffix}'", values("son"));
        assertThat(c.sql()).isEqualTo("name like ?");
        assertThat(c.params()).containsExactly("%son");
    }

    @Test
    void shouldFoldBothSideLikeWildcardsIntoValue() {
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "name like '%${needle}%'", values("ohn"));
        assertThat(c.sql()).isEqualTo("name like ?");
        assertThat(c.params()).containsExactly("%ohn%");
    }

    @Test
    void shouldFoldMultipleLikeWildcardsIntoValue() {
        // SQL LIKE treats %% the same as %, but make sure the algorithm doesn't drop chars.
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "name like '%%${x}%%'", values("v"));
        assertThat(c.sql()).isEqualTo("name like ?");
        assertThat(c.params()).containsExactly("%%v%%");
    }

    @Test
    void shouldHandleValueWithExistingPercent() {
        // Caller already baked % into the value; template is a plain '${x}' exact-match.
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "name like '${prefix}'", values("John%"));
        assertThat(c.sql()).isEqualTo("name like ?");
        assertThat(c.params()).containsExactly("John%");
    }

    @Test
    void shouldHandleAccentedAndApostropheValues() {
        // Real patient names; PreparedStatement binding handles them.
        ParameterizedClause c = RptReportCreator.getWhereValueClauseParameterized(
            "name in ('${a}','${b}','${c}')", values("O'Brien", "François", "Sánchez"));
        assertThat(c.sql()).isEqualTo("name in (?,?,?)");
        assertThat(c.params()).containsExactly("O'Brien", "François", "Sánchez");
    }
}
