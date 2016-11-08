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

import org.easyrec.model.core.ItemVO;
import org.easyrec.utils.spring.store.dao.TableCreatingDAO;

import java.util.List;
import java.util.Set;

/**
 * This interface provides methods to store data into and read <code>Profile</code> entries from an easyrec database.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: fsalcher $<br/>
 * $Date: 2012-03-23 15:35:07 +0100 (Fr, 23 Mär 2012) $<br/>
 * $Revision: 18791 $</p>
 *
 * @author Stephan Zavrel
 */
public interface BaseProfileDAO<T, I, IT> extends TableCreatingDAO {

    ///////////////////////////////////////////////////////////////////////////
    // constants
    public final static String DEFAULT_TABLE_NAME = "item";

    public final static String DEFAULT_TENANT_ID_COLUMN_NAME = "tenantId";
    public final static String DEFAULT_ITEM_ID_COLUMN_NAME = "itemid";
    public final static String DEFAULT_ITEM_TYPE_ID_COLUMN_NAME = "itemtype";
    public final static String DEFAULT_PROFILE_DATA_COLUMN_NAME = "profileData";
    public final static String DEFAULT_ACTIVE_COLUMN_NAME = "active";


    // non abstract
    public int storeProfile(T tenant, I item, IT itemType, String profileXML);

    public String getProfile(T tenant, I item, IT itemType);

    public String getProfile(T tenantId, I itemId, IT itemTypeId, Boolean active);

    public Set<String> getMultiDimensionValue(T tenantId, I itemId, IT itemTypeId, String dimensionXPath);

    public String getSimpleDimensionValue(T tenantId, I itemId, IT itemTypeId, String dimensionXPath);

    /**
     * This method replaces some parts of the <code>Profile XML</code>
     * defined by a XPath with another string.
     *
     * @param tenantId    the tenantId of the item with the profile
     * @param itemId      the itemId of the item with the profile
     * @param itemTypeId  the itemTypeId of the item with the profile
     * @param updateXPath an XPath which points to the part of the
     *                    <code>Profile</code> which will be replaced
     * @param newXML      the string which will be placed at the given
     *                    XPath location
     * @return <code>true</code> if the operation succeeds and
     *         <code>false</code> otherwise
     */
    public boolean updateXML(T tenantId, I itemId, IT itemTypeId,
                             String updateXPath, String newXML);

    public boolean deleteProfile(T tenantId, I itemId, IT itemTypeId);

    public List<ItemVO<Integer, Integer>> getItemsByDimensionValue(T tenantId, IT itemType,
                                                                   String dimensionXPath, String value);

    public List<ItemVO<Integer, Integer>> getItemsByItemType(T Tenant, IT itemType, int count);

    public void activateProfile(T tenant, I item, IT itemType);

    public void deactivateProfile(T tenant, I item, IT itemType);

}
