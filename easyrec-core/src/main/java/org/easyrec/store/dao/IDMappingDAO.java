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
package org.easyrec.store.dao;

import org.easyrec.service.core.exception.ItemNotFoundException;

/**
 * This interface provides methods to map a String IDs to Integers.
 * For an example of a spring bean definition config file see tests.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: pmarschik $<br/>
 * $Date: 2011-02-11 11:04:49 +0100 (Fr, 11 Feb 2011) $<br/>
 * $Revision: 17656 $</p>
 *
 * @author Stephan Zavrel
 */

public interface IDMappingDAO {
    // constants
    public final static String DEFAULT_TABLE_NAME = "idmapping";

    public final static String DEFAULT_INT_ID_COLUMN_NAME = "intId";
    public final static String DEFAULT_STRING_ID_COLUMN_NAME = "stringId";

    /**
     * Returns the Integer ID mapped to the given String ID. In case no mapping exists, a new one is created and
     * the resulting Integer is returned.
     *
     * @param id the String to be looked up in the mapping
     * @return the Integer mapped to the given String
     */
    public Integer lookup(String id);

    /**
     * Returns the Integer ID mapped to the given String ID. In case no mapping exists,
     * it returns <code>null</code>.
     *
     * @param id the String to be looked up in the mapping
     * @return the Integer mapped to the given String
     */
    public Integer lookupOnly(String id)
            throws ItemNotFoundException;

    /**
     * Returns the String ID mapped to the given Integer ID. In case the no mapping exists, null is returned.
     *
     * @param id the Integer to be looked up in the mapping
     * @return the String mapped to the given Integer; null if no mapping exists.
     */
    public String lookup(Integer id);

}
