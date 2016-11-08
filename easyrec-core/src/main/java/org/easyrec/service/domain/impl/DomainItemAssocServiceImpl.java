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
package org.easyrec.service.domain.impl;

import org.easyrec.model.core.AssociatedItemVO;
import org.easyrec.model.core.ItemAssocVO;
import org.easyrec.model.core.ItemVO;
import org.easyrec.model.core.transfer.IAConstraintVO;
import org.easyrec.service.core.ItemAssocService;
import org.easyrec.service.domain.DomainItemAssocService;
import org.easyrec.service.domain.TypeMappingService;
import org.easyrec.store.dao.domain.TypedItemAssocDAO;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the {@link org.easyrec.service.domain.DomainItemAssocService} interface.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: dmann $<br/>
 * $Date: 2011-12-20 15:22:22 +0100 (Di, 20 Dez 2011) $<br/>
 * $Revision: 18685 $</p>
 *
 * @author Roman Cerny
 */
public class DomainItemAssocServiceImpl implements DomainItemAssocService {
    //////////////////////////////////////////////////////////////////////////////
    // constants

    // csv import
    //private static final int CSV_NUMBER_OF_COLUMNS = 10;
    //private static final int DEFAULT__REPORT__BLOCK_SIZE = 1000;

    // autoimport
    //private static final Boolean DEFAULT_AUTO_IMPORT_OVERWRITE_DUPLICATES = new Boolean(true);

    //////////////////////////////////////////////////////////////////////////////
    // members
    private TypedItemAssocDAO typedItemAssocDAO;
    private ItemAssocService itemAssocService;
    private TypeMappingService typeMappingService;

    // csv import
    //private Boolean importOverwriteDuplicates = null;
    //private Integer reportBlockSize = null;

    public DomainItemAssocServiceImpl(TypedItemAssocDAO typedItemAssocDAO, ItemAssocService itemAssocService,
                                      TypeMappingService typeMappingService) {
        this.typedItemAssocDAO = typedItemAssocDAO;
        this.itemAssocService = itemAssocService;
        this.typeMappingService = typeMappingService;
    }

    //////////////////////////////////////////////////////////////////////////////
    // interface "ItemAssocService" implementation
    @Override
    public List<ItemAssocVO<Integer, String>> getItemAssocs(
            ItemVO<Integer, String> itemFrom, String assocType, ItemVO<Integer, String> itemTo,
            IAConstraintVO<Integer, String> constraints) {
        return typedItemAssocDAO.getItemAssocs(itemFrom, assocType, itemTo, constraints);
    }

    @Override
    public List<ItemAssocVO<Integer, String>> getItemAssocsFromTenant(Integer tenant,
                                                                                                       Integer numberOfResults) {
        return typedItemAssocDAO.getItemAssocsQBE(null, null, null,
                new IAConstraintVO<Integer, String>(numberOfResults, tenant));
    }

    @Override
    public List<ItemAssocVO<Integer, String>> getItemAssocsForItem(Integer tenant,
                                                                                                    ItemVO<Integer, String> itemFrom,
                                                                                                    Integer numberOfResults) {
        return typedItemAssocDAO.getItemAssocsQBE(itemFrom, null, null,
                new IAConstraintVO<Integer, String>(numberOfResults, null, null, null, tenant, null, false));
    }

    @Override
    public List<AssociatedItemVO<Integer, String>> getItemsFrom(String itemFromType, String assocType,
                                                                                 ItemVO<Integer, String> itemTo,
                                                                                 IAConstraintVO<Integer, String> constraints) {
        return typedItemAssocDAO.getItemsFrom(itemFromType, assocType, itemTo, constraints);
    }

    @Override
    public List<AssociatedItemVO<Integer, String>> getItemsTo(
            ItemVO<Integer, String> itemFrom, String assocType, String itemToType,
            IAConstraintVO<Integer, String> constraints) {
        return typedItemAssocDAO.getItemsTo(itemFrom, assocType, itemToType, constraints);
    }

    @Override
    public void importItemAssocsFromCSV(String fileName) {
        importItemAssocsFromCSV(fileName, null);
    }

    @Override
    public void importItemAssocsFromCSV(String fileName,
                                        ItemAssocVO<Integer, String> defaults) {
        itemAssocService.importItemAssocsFromCSV(fileName,
                typeMappingService.convertTypedItemAssocVO(defaults.getTenant(), defaults));
    }

    @Override
    public Iterator<ItemAssocVO<Integer, String>> getItemAssocIterator(int bulkSize) {
        return typedItemAssocDAO.getItemAssocIterator(bulkSize);
    }

    @Override
    public ItemAssocVO<Integer, String> loadItemAssoc(Long itemAssocId) {
        return typedItemAssocDAO.loadItemAssocByPrimaryKey(itemAssocId);
    }

    @Override
    public int removeAllItemAssocs() {
        return typedItemAssocDAO.removeAllItemAssocs();
    }

    @Override
    public int removeAllItemAssocsFromSource(String sourceType) {
        return typedItemAssocDAO.removeItemAssocsQBE(
                new ItemAssocVO<Integer, String>(null, null, null, null, null,
                        sourceType, null, null, null));
    }

    @Override
    public int removeAllItemAssocsFromSource(String sourceType, String sourceInfo) {
        return typedItemAssocDAO.removeItemAssocsQBE(
                new ItemAssocVO<Integer, String>(null, null, null, null, null,
                        sourceType, sourceInfo, null, null));
    }

    /**
     * Removes an ItemAssoc-Entry with the given ID from the DB.
     *
     * @param itemAssocId Integer
     */
    @Override
    public int removeItemAssoc(Long itemAssocId) {
        // validate input parameters
        if (itemAssocId == null) {
            throw new IllegalArgumentException("missing 'itemAssocId'");
        }

        return typedItemAssocDAO.removeItemAssocsQBE(
                new ItemAssocVO<Integer, String>(itemAssocId, (Integer) null, null,
                        null, null, null, null, null, null, null));
    }

    /**
     * This is a QBE Implementation for removing ItemAssocs-Entries. Removes several ItemAssoc-Entries that match the given
     * Example. Attributes that are left out (set to NULL) will act like a wildcard. Meaning if you for example just
     * pass a sourceInfo="16" and an ItemToType="track", all Tracks with sourceInfo="16" will be removed from the DB.
     *
     * @param itemAssoc ItemAssocVO
     */
    @Override
    public int removeItemAssocQBE(ItemAssocVO<Integer, String> itemAssoc) {
        return typedItemAssocDAO.removeItemAssocsQBE(itemAssoc);
    }

    @Override
    public int insertItemAssoc(ItemAssocVO<Integer, String> itemAssoc) {
        // validate input parameters
        if (itemAssoc == null) {
            throw new IllegalArgumentException("missing 'itemAssoc'");
        }

        int rowsAffected = 0;
        if (itemAssoc.getId() == null) {
            rowsAffected = typedItemAssocDAO.insertItemAssoc(itemAssoc);
        } else {
            rowsAffected = typedItemAssocDAO.updateItemAssocUsingPrimaryKey(itemAssoc);
        }

        return rowsAffected;
    }

    /**
     * This Method inserts or updates a given ItemAssoc. An Update should occur,
     * only if the assocValue has changed.
     *
     * @param itemAssoc ItemAssocVO
     */
    @Override
    public int insertOrUpdateItemAssoc(ItemAssocVO<Integer, String> itemAssoc) {
        // validate input parameters
        if (itemAssoc == null) {
            throw new IllegalArgumentException("missing 'itemAssoc'");
        }

        int rowsAffected = 0;
        if (itemAssoc.getId() == null) {
            // check if itemAssoc already exists
            ItemAssocVO<Integer, String> itemAssocResult = typedItemAssocDAO
                    .loadItemAssocByUniqueKey(itemAssoc);
            if (itemAssocResult == null) {
                // insert a new itemAssoc entry
                rowsAffected = typedItemAssocDAO.insertItemAssoc(itemAssoc);
            } else {
                // update existing itemAssoc entry (without knowing the id),
                // only if value the has changed
                if (!itemAssoc.getAssocValue().equals(itemAssocResult.getAssocValue())) {
                    rowsAffected = typedItemAssocDAO.updateItemAssocUsingUniqueKey(itemAssoc);
                }
            }
        } else {
            // update existing itemAssoc entry (using the id)
            rowsAffected = typedItemAssocDAO.updateItemAssocUsingPrimaryKey(itemAssoc);
        }
        return rowsAffected;
    }

    @Override
    public int insertOrUpdateItemAssocs(
            final List<ItemAssocVO<Integer, String>> itemAssocs) {
        return typedItemAssocDAO.insertOrUpdateItemAssocs(itemAssocs);
    }

    @Override
    public int removeAllItemAssocsFromTenant(Integer tenantId) {
        return typedItemAssocDAO.removeItemAssocsQBE(
                new ItemAssocVO<Integer, String>(null, tenantId, null, null, null,
                        null, null, null, null, null));
    }

    @Override
    public boolean isActiveItemAssoc(Long itemAssocId) {
        return itemAssocService.isActiveItemAssoc(itemAssocId);
    }

    @Override
    public int activateItemAssoc(Long itemAssocId) {
        return itemAssocService.activateItemAssoc(itemAssocId);
    }

    @Override
    public int deactivateItemAssoc(Long itemAssocId) {
        return itemAssocService.deactivateItemAssoc(itemAssocId);
    }
}
