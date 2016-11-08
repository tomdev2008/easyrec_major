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
package org.easyrec.service.core;

import java.util.List;
import java.util.Set;
import org.easyrec.model.core.ItemVO;
import org.easyrec.model.core.web.Item;

/**
 * The easyrec Profile system uses a sql columns in the database to save the profile as XML
 * This Service class loads them from the database using XPath to access the XML profiles.
 *
 * @author szavrel
 */
public interface ProfileService {

    /**
     * Used easyrec
     * This function loads a profile XML string from the database
     *
     * @param item The Item Object of the profile (holds itemId, itemTypeId and tenantId)
     * @return a string with the XML profile for the given item object
     */
    public String getProfile(Item item);

    /**
     * Used duke matcher
     * This function loads a profile XML string from the database
     *
     * @param item The Item Object of the profile (holds itemId, itemTypeId and tenantId)
     * @return a string with the XML profile for the given item object
     */
    public String getProfile(ItemVO<Integer, Integer> item);

    /**
     * Used
     * This function loads a profile XML string from the database
     *
     * @param tenantId   the tenantId of the profile
     * @param itemId     the itemId of the profile
     * @param itemTypeId the itemTypeId of the profile
     * @return a string with the XML profile for the given tenantId , itemId, itemTypeId combination
     */
    public String getProfile(Integer tenantId, String itemId, String itemTypeId);

    /**
     * used
     * This function writes a profile as an XML string to the database
     *
     * @param tenantId   the tenantId of the profile
     * @param itemId     the string itemId of the profile
     * @param itemType   the itemType of the profile
     * @param profileXML the profile as an XML string
     * @return <code>true</code> if the operation succeeds <code>false</code> otherwise
     */
    public boolean storeProfile(Integer tenantId, String itemId, String itemType, String profileXML);

    /**
     * Used
     * This function deletes a profile of an item
     *
     * @param tenantId the tenantId of the profile's item
     * @param itemId   the string itemId of the profile's item
     * @param itemType the itemType of the profile's item
     * @return <code>true</code> if the operation succeeds <code>false</code> otherwise
     */
    public boolean deleteProfile(Integer tenantId, String itemId, String itemType);

    /**
     * Used
     * This function loads all results as List of string values from
     * the profile based on the provided XPath. In contrast to
     * <code>getMultiDimensionValue</code> it also throws the XPath
     * and DOM relevant Exceptions.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemType       the itemTypeId of the profile
     * @param dimensionXPath the XPath of the value you want to load
     * @return the values of the given XPath
     * @throws java.lang.Exception
     */
    public Set<String> loadProfileField(Integer tenantId, String itemId, String itemType,
                                        String dimensionXPath)
            throws Exception;

    /**
     * used
     * This function inserts a value into an item's ( based on tenantId, itemId, itemTypeId)
     * XML Profile at the specified XPath.
     *
     * @param tenantId       the tenantId of the profile
     * @param itemId         the itemId of the profile
     * @param itemTypeId     the itemTypeId of the profile
     * @param dimensionXPath the XPath of the value you want to update or insert
     * @param value          the value you want to insert or update into the profile
     * @return <code>true</code> if the operation succeeds <code>false</code> otherwise
     * @throws java.lang.Exception
     */
    public boolean storeProfileField(Integer tenantId, String itemId, String itemTypeId,
                                     String dimensionXPath, String value)
            throws Exception;

    /**
     * Used
     * This function deletes the nodes defined by <code>deleteXPath</code>.
     *
     * @param tenantId    the tenantId of the profile
     * @param itemId      the itemId of the profile
     * @param itemType    the itemType of the profile
     * @param deleteXPath the XPath to the nodes which will be deleted
     * @return returns <code>true</code> if the operation succeeded and
     *         <code>false</code> otherwise
     * @throws java.lang.Exception
     */
    public boolean deleteProfileField(Integer tenantId, String itemId, String itemType, String deleteXPath)
            throws Exception;


    /**
     * Used by duke matcher
     * 
     * This function will load all Item's with a specific itemType
     *
     * @param tenantId the tenantId of the profile
     * @param itemType the itemType name of the profile
     * @param count    the maximum result set size
     * @return A list of ItemVo Objects of the requested itemType
     */
    public List<ItemVO<Integer, Integer>> getItemsByItemType(Integer tenantId, String itemType, int count);

}
