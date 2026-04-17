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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable carrier for a SQL fragment that uses {@code ?} placeholders together with
 * the ordered list of values to bind at those placeholders.
 *
 * <p>Use this type anywhere a WHERE-clause fragment is assembled from user input and then
 * concatenated into a larger SQL string. Instead of substituting values into the string
 * (which forces a regex allowlist as the only injection defense), carry the fragment and
 * its params together, then pass {@link #params()} as bind parameters to
 * {@code DBHandler.GetPreSQL(sql, params)}.</p>
 *
 * <p>Two clauses can be merged with {@link #combine(String, ParameterizedClause)}. The
 * left clause's SQL comes first, then the joiner string, then the right clause's SQL.
 * Params concatenate in the same order so bind positions line up.</p>
 *
 * @since 2026-04-17
 */
public final class ParameterizedClause {

    private static final ParameterizedClause EMPTY =
        new ParameterizedClause("", Collections.emptyList());

    private final String sql;
    private final List<Object> params;

    public ParameterizedClause(String sql, List<Object> params) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null");
        }
        if (params == null) {
            throw new IllegalArgumentException("params must not be null");
        }
        this.sql = sql;
        this.params = Collections.unmodifiableList(new ArrayList<>(params));
    }

    public static ParameterizedClause empty() {
        return EMPTY;
    }

    public String sql() {
        return sql;
    }

    public List<Object> params() {
        return params;
    }

    public boolean isEmpty() {
        return sql.isEmpty();
    }

    /**
     * Concatenates this clause with {@code other}, separated by {@code joiner}.
     * If either side is empty, the other is returned unchanged (no joiner emitted).
     * Params of {@code this} come first, followed by params of {@code other}.
     */
    public ParameterizedClause combine(String joiner, ParameterizedClause other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return other;
        }
        List<Object> merged = new ArrayList<>(this.params.size() + other.params.size());
        merged.addAll(this.params);
        merged.addAll(other.params);
        return new ParameterizedClause(this.sql + joiner + other.sql, merged);
    }
}
