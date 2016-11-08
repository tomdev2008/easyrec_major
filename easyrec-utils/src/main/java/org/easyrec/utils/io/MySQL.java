/**Copyright 2010 Research Studios Austria Forschungsgesellschaft mBH
 *
 * This file is part of easyrec.
 *
 * easyrec is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * easyrec is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with easyrec.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.easyrec.utils.io;

import com.google.common.base.Strings;
import java.util.Calendar;
import java.util.Date;

/**
 * This class manipulates mysql query strings.
 *
 * @author phlavac
 */
public class MySQL {

    /**
     * This function adds a Like clause to an SQL statement.
     * The Like clause starts with the keyword "AND" meaning that given sql string
     * has to end with the keyword
     * "WHERE" and a clause (e.g. 1=1) before the like clause can be appended.
     * If the attributeValue has less than 3 charactars a equal comparison is
     * performed.
     *
     * @param sql
     * @param attribute
     * @param attributeValue
     *
     */
    public static StringBuilder addLikeClause(StringBuilder sql, String attribute, String attributeValue) {
        if (Strings.isNullOrEmpty(attributeValue)) return sql;

        if (attributeValue.length() > 2) {
            return sql.append(" AND ").append(attribute).append(" LIKE '%").append(attributeValue).append("%' ");
        } else {
            return sql.append(" AND ").append(attribute).append(" = '").append(attributeValue).append("' ");
        }
    }

    /**
     * This function adds a limit clause to a given mysql statement (e.g. LIMIT 200,50)
     * show the rows from 200 to 250.
     *
     * @param sql
     * @param offset
     * @param number
     *
     */
    public static StringBuilder addLimitClause(StringBuilder sql, int offset, int number) {
        if (offset < 0 || number <= 0) return sql;

        return sql.append(" LIMIT ").append(offset).append(", ").append(number);
    }
    
    /**
     * Method for compensating for MySQL rounding behaviour for date types since version 5.6.4 and above.
     * Unfortunately MySQL behaves differently when inserting date types and when using them for comparison in
     * queries. On insert, date types are rounded to the nearest second when the field is not specified as a fractional
     * second field (e.g. datetime(6) vs. just datetime). However, when passing date types as a parameter for a query
     * comparison it uses the exact value without rounding, thus leading to undesired results. This method rounds to 
     * nearest second. Use when passing date types to queries for comparisons.
     * 
     * @param date the date to round
     * @return the rounded date
     */
    public static Date sanitzeForMysql56(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MILLISECOND, 500);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
