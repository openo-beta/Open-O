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
package ca.openosp.openo.util;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link ParameterizedClause}.
 *
 * @since 2026-04-17
 */
@DisplayName("ParameterizedClause")
@Tag("unit")
@Tag("fast")
class ParameterizedClauseTest {

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        void shouldStoreSqlAndParams() {
            ParameterizedClause c = new ParameterizedClause("x = ?", Arrays.asList("v"));
            assertThat(c.sql()).isEqualTo("x = ?");
            assertThat(c.params()).containsExactly("v");
        }

        @Test
        void shouldRejectNullSql() {
            assertThatThrownBy(() -> new ParameterizedClause(null, Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldRejectNullParams() {
            assertThatThrownBy(() -> new ParameterizedClause("x = ?", null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldDefensivelyCopyParams() {
            List<Object> mutable = new ArrayList<>();
            mutable.add("a");
            ParameterizedClause c = new ParameterizedClause("x = ?", mutable);
            mutable.add("b");
            assertThat(c.params()).containsExactly("a");
        }

        @Test
        void shouldReturnUnmodifiableParams() {
            ParameterizedClause c = new ParameterizedClause("x = ?", Arrays.asList("v"));
            assertThatThrownBy(() -> c.params().add("extra"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("empty()")
    class Empty {

        @Test
        void shouldHaveEmptySqlAndNoParams() {
            ParameterizedClause c = ParameterizedClause.empty();
            assertThat(c.sql()).isEmpty();
            assertThat(c.params()).isEmpty();
            assertThat(c.isEmpty()).isTrue();
        }

        @Test
        void shouldReturnSameInstance() {
            assertThat(ParameterizedClause.empty()).isSameAs(ParameterizedClause.empty());
        }
    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        void shouldBeTrueWhenSqlIsEmpty() {
            assertThat(new ParameterizedClause("", Collections.emptyList()).isEmpty()).isTrue();
        }

        @Test
        void shouldBeFalseWhenSqlIsPresent() {
            assertThat(new ParameterizedClause("x = ?", Arrays.asList("v")).isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("combine")
    class Combine {

        @Test
        void shouldConcatenateSqlWithJoinerAndMergeParamsInOrder() {
            ParameterizedClause a = new ParameterizedClause("x = ?", Arrays.asList("vx"));
            ParameterizedClause b = new ParameterizedClause("y = ?", Arrays.asList("vy"));
            ParameterizedClause merged = a.combine(" and ", b);
            assertThat(merged.sql()).isEqualTo("x = ? and y = ?");
            assertThat(merged.params()).containsExactly("vx", "vy");
        }

        @Test
        void shouldReturnLeftWhenRightIsEmpty() {
            ParameterizedClause a = new ParameterizedClause("x = ?", Arrays.asList("vx"));
            ParameterizedClause merged = a.combine(" and ", ParameterizedClause.empty());
            assertThat(merged.sql()).isEqualTo("x = ?");
            assertThat(merged.params()).containsExactly("vx");
        }

        @Test
        void shouldReturnRightWhenLeftIsEmpty() {
            ParameterizedClause b = new ParameterizedClause("y = ?", Arrays.asList("vy"));
            ParameterizedClause merged = ParameterizedClause.empty().combine(" and ", b);
            assertThat(merged.sql()).isEqualTo("y = ?");
            assertThat(merged.params()).containsExactly("vy");
        }

        @Test
        void shouldReturnLeftWhenRightIsNull() {
            ParameterizedClause a = new ParameterizedClause("x = ?", Arrays.asList("vx"));
            assertThat(a.combine(" and ", null)).isSameAs(a);
        }

        @Test
        void shouldPreserveMultipleParamsFromEachSide() {
            ParameterizedClause a = new ParameterizedClause("x in (?,?)", Arrays.asList(1, 2));
            ParameterizedClause b = new ParameterizedClause("y in (?,?,?)", Arrays.asList("a", "b", "c"));
            ParameterizedClause merged = a.combine(" and ", b);
            assertThat(merged.sql()).isEqualTo("x in (?,?) and y in (?,?,?)");
            assertThat(merged.params()).containsExactly(1, 2, "a", "b", "c");
        }

        @Test
        void shouldProduceEquivalentSqlRegardlessOfJoinerWhitespace() {
            ParameterizedClause a = new ParameterizedClause("x = ?", Arrays.asList(1));
            ParameterizedClause b = new ParameterizedClause("y = ?", Arrays.asList(2));
            assertThat(a.combine("and", b).sql()).isEqualTo("x = ? and y = ?");
            assertThat(a.combine(" and ", b).sql()).isEqualTo("x = ? and y = ?");
            assertThat(a.combine("  AND  ", b).sql()).isEqualTo("x = ? AND y = ?");
        }

        @Test
        void shouldConcatenateDirectlyWhenJoinerIsNullOrEmpty() {
            ParameterizedClause a = new ParameterizedClause("x=?", Arrays.asList(1));
            ParameterizedClause b = new ParameterizedClause("y=?", Arrays.asList(2));
            assertThat(a.combine("", b).sql()).isEqualTo("x=?y=?");
            assertThat(a.combine(null, b).sql()).isEqualTo("x=?y=?");
        }
    }

    @Nested
    @DisplayName("and(...) convenience method")
    class AndConvenience {

        @Test
        void shouldJoinWithSpacedAndAndConcatParams() {
            ParameterizedClause a = new ParameterizedClause("x = ?", Arrays.asList("vx"));
            ParameterizedClause b = new ParameterizedClause("y = ?", Arrays.asList("vy"));
            ParameterizedClause merged = a.and(b);
            assertThat(merged.sql()).isEqualTo("x = ? and y = ?");
            assertThat(merged.params()).containsExactly("vx", "vy");
        }

        @Test
        void shouldReturnLeftWhenRightIsEmpty() {
            ParameterizedClause a = new ParameterizedClause("x = ?", Arrays.asList("vx"));
            assertThat(a.and(ParameterizedClause.empty())).isSameAs(a);
        }

        @Test
        void shouldReturnRightWhenLeftIsEmpty() {
            ParameterizedClause b = new ParameterizedClause("y = ?", Arrays.asList("vy"));
            ParameterizedClause merged = ParameterizedClause.empty().and(b);
            assertThat(merged.sql()).isEqualTo("y = ?");
            assertThat(merged.params()).containsExactly("vy");
        }
    }

    @Nested
    @DisplayName("joinAnd(...) static factory")
    class JoinAnd {

        @Test
        void shouldReturnEmptyForNullOrEmptyInput() {
            assertThat(ParameterizedClause.joinAnd(null).isEmpty()).isTrue();
            assertThat(ParameterizedClause.joinAnd(Collections.emptyList()).isEmpty()).isTrue();
        }

        @Test
        void shouldFoldMultipleClausesWithSpacedAnd() {
            ParameterizedClause a = new ParameterizedClause("x = ?", Arrays.asList(1));
            ParameterizedClause b = new ParameterizedClause("y = ?", Arrays.asList(2));
            ParameterizedClause c = new ParameterizedClause("z = ?", Arrays.asList(3));
            ParameterizedClause merged = ParameterizedClause.joinAnd(Arrays.asList(a, b, c));
            assertThat(merged.sql()).isEqualTo("x = ? and y = ? and z = ?");
            assertThat(merged.params()).containsExactly(1, 2, 3);
        }

        @Test
        void shouldSkipEmptyClauses() {
            ParameterizedClause a = new ParameterizedClause("x = ?", Arrays.asList(1));
            ParameterizedClause b = new ParameterizedClause("y = ?", Arrays.asList(2));
            ParameterizedClause merged = ParameterizedClause.joinAnd(
                Arrays.asList(ParameterizedClause.empty(), a, ParameterizedClause.empty(), b));
            assertThat(merged.sql()).isEqualTo("x = ? and y = ?");
            assertThat(merged.params()).containsExactly(1, 2);
        }
    }
}
