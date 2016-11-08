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
package org.easyrec.service.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.model.core.TenantConfigVO;
import org.easyrec.model.core.TenantVO;
import org.easyrec.service.core.TenantService;
import org.easyrec.store.dao.core.AuthenticationDAO;
import org.easyrec.store.dao.core.TenantDAO;
import org.easyrec.store.dao.core.types.*;

import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.util.List;
import java.util.Properties;
import org.easyrec.service.core.ClusterService;
import org.easyrec.store.dao.core.ActionDAO;
import org.easyrec.store.dao.core.ItemAssocDAO;
import org.easyrec.store.dao.core.ItemDAO;

/**
 * Implementation of the {@link org.easyrec.service.core.TenantService} interface.
 * <p/>
 * Note: For now this is a simple wrapper for the {link org.easyrec.store.dao.core.TenantDAO} interface.
 * But soon, some caching will be introduced in order to prevent massive unnecessary database queries.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: szavrel $<br/>
 * $Date: 2011-11-11 18:30:05 +0100 (Fr, 11 Nov 2011) $<br/>
 * $Revision: 18636 $</p>
 *
 * @author Roman Cerny
 */
public class TenantServiceImpl implements TenantService {
    // HINT: introduce caching of tenants (Mantis Issue: #701)

    //////////////////////////////////////////////////////////////////////////////
    // members
    private final Log logger = LogFactory.getLog(this.getClass());

    private TenantConfigVO defaultTenantConfig;

    private TenantDAO tenantDAO;
    private ActionTypeDAO actionTypeDAO;
    private AggregateTypeDAO aggregateTypeDAO;
    private AssocTypeDAO assocTypeDAO;
    private ItemTypeDAO itemTypeDAO;
    private SourceTypeDAO sourceTypeDAO;
    private ViewTypeDAO viewTypeDAO;
    private AuthenticationDAO authenticationDAO;
    private ClusterService clusterService;
    private ItemAssocDAO itemAssocDAO;
    private ActionDAO actionDAO;
    private ItemDAO itemDAO;

    public TenantServiceImpl(TenantDAO tenantDAO, TenantConfigVO tenantConfig, ActionTypeDAO actionTypeDAO,
                             AggregateTypeDAO aggregateTypeDAO, AssocTypeDAO assocTypeDAO, ItemTypeDAO itemTypeDAO,
                             SourceTypeDAO sourceTypeDAO, ViewTypeDAO viewTypeDAO,
                             AuthenticationDAO authenticationDAO, ItemAssocDAO itemAssocDAO,
                             ActionDAO actionDAO, ItemDAO itemDAO) {
        this.tenantDAO = tenantDAO;
        this.defaultTenantConfig = tenantConfig;
        this.actionTypeDAO = actionTypeDAO;
        this.aggregateTypeDAO = aggregateTypeDAO;
        this.assocTypeDAO = assocTypeDAO;
        this.itemTypeDAO = itemTypeDAO;
        this.sourceTypeDAO = sourceTypeDAO;
        this.viewTypeDAO = viewTypeDAO;
        this.authenticationDAO = authenticationDAO;
        this.itemAssocDAO  = itemAssocDAO;
        this.actionDAO = actionDAO;
        this.itemDAO = itemDAO;
    }

    // interface 'TenantService' implementation
    @Override
    public TenantVO getTenantById(Integer tenantId) {
        return tenantDAO.getTenantById(tenantId);
    }

    @Override
    public TenantVO getTenantByStringId(String stringId) {
        return tenantDAO.getTenantByStringId(stringId);
    }

    @Override
    public List<TenantVO> getAllTenants() {
        return tenantDAO.getAllTenants();
    }

    @Override
    public int deactivateTenant(TenantVO tenant) {

        return tenantDAO.setTenantActive(tenant, false);
    }

    @Override
    public int insertTenantWithTypes(TenantVO tenant, TenantConfigVO tenantConfig) {

        tenant.setId(tenantDAO.insertTenant(tenant));

        if (tenantConfig != null) {
            int i = 1;
            List<String> actionTypes = (tenantConfig.getActionTypes() != null) ? tenantConfig.getActionTypes() :
                    defaultTenantConfig.getActionTypes();
            for (String actionType : actionTypes) {
                actionTypeDAO.insertOrUpdate(tenant.getId(), actionType, i++);
                if ("RATE".equals(actionType)) {
                    actionTypeDAO.insertOrUpdate(tenant.getId(), actionType, i, true);
                }
            }
            i = 1;
            List<String> itemTypes = (tenantConfig.getItemTypes() != null) ? tenantConfig.getItemTypes() :
                    defaultTenantConfig.getItemTypes();
            for (String itemType : itemTypes) {
                itemTypeDAO.insertOrUpdate(tenant.getId(), itemType, i++);
            }
            i = 1;
            List<String> assocTypes = (tenantConfig.getAssocTypes() != null) ? tenantConfig.getAssocTypes() :
                    defaultTenantConfig.getAssocTypes();
            for (String assocType : assocTypes) {
                assocTypeDAO.insertOrUpdate(tenant.getId(), assocType, i++);
            }
            i = 1;
            List<String> aggregateTypes =
                    (tenantConfig.getAggregateTypes() != null) ? tenantConfig.getAggregateTypes() :
                            defaultTenantConfig.getAggregateTypes();
            for (String aggregateType : aggregateTypes) {
                aggregateTypeDAO.insertOrUpdate(tenant.getId(), aggregateType, i++);
            }
            i = 1;
            List<String> sourceTypes = (tenantConfig.getSourceTypes() != null) ? tenantConfig.getSourceTypes() :
                    defaultTenantConfig.getSourceTypes();
            for (String sourceType : sourceTypes) {
                sourceTypeDAO.insertOrUpdate(tenant.getId(), sourceType, i++);
            }
            i = 1;
            List<String> viewTypes = (tenantConfig.getViewTypes() != null) ? tenantConfig.getViewTypes() :
                    defaultTenantConfig.getViewTypes();
            for (String viewType : viewTypes) {
                viewTypeDAO.insertOrUpdate(tenant.getId(), viewType, i++);
            }
            i = 1;
            List<String> authDomains =
                    (tenantConfig.getAuthenticationDomains() != null) ? tenantConfig.getAuthenticationDomains() :
                            defaultTenantConfig.getAuthenticationDomains();
            for (String authDomain : authDomains) {
                authenticationDAO.insertDomainURLForTenant(tenant.getId(), authDomain);
            }
        } else {
            int i = 1;
            for (String actionType : defaultTenantConfig.getActionTypes()) {
                actionTypeDAO.insertOrUpdate(tenant.getId(), actionType, i++);
            }
            i = 1;
            for (String itemType : defaultTenantConfig.getItemTypes()) {
                itemTypeDAO.insertOrUpdate(tenant.getId(), itemType, i++);
            }
            i = 1;
            for (String assocType : defaultTenantConfig.getAssocTypes()) {
                assocTypeDAO.insertOrUpdate(tenant.getId(), assocType, i++);
            }
            i = 1;
            for (String aggregateType : defaultTenantConfig.getAggregateTypes()) {
                aggregateTypeDAO.insertOrUpdate(tenant.getId(), aggregateType, i++);
            }
            i = 1;
            for (String sourceType : defaultTenantConfig.getSourceTypes()) {
                sourceTypeDAO.insertOrUpdate(tenant.getId(), sourceType, i++);
            }
            i = 1;
            for (String viewType : defaultTenantConfig.getViewTypes()) {
                viewTypeDAO.insertOrUpdate(tenant.getId(), viewType, i++);
            }
            i = 1;
            for (String authDomain : defaultTenantConfig.getAuthenticationDomains()) {
                authenticationDAO.insertDomainURLForTenant(tenant.getId(), authDomain);
            }
        }

        clusterService.initTenantForClusters(tenant);

        return tenant.getId();
    }

    @Override
    public void insertAssocTypeForTenant(Integer tenantId, String assocType) {
        assocTypeDAO.insertOrUpdate(tenantId, assocType);
    }

    @Override
    public int insertAssocTypeForTenant(Integer tenantId, String assocType, boolean visible) {
        return assocTypeDAO.insertOrUpdate(tenantId, assocType, visible);
    }

    @Override
    public void insertSourceTypeForTenant(Integer tenantId, String sourceType) {
        sourceTypeDAO.insertOrUpdate(tenantId, sourceType);
    }

    @Override
    public int insertItemTypeForTenant(Integer tenantId, String itemType, boolean visible) {
        return itemTypeDAO.insertOrUpdate(tenantId, itemType, visible);
    }

    @Override
    public String isValidItemTypeName(String itemTypeName) {

        if (itemTypeName.contains(" ")) {
            return "The item type name cannot contain spaces.";
        }

        if ("CLUSTER".equals(itemTypeName)) {
            return "CLUSTER is a reserved item type name used for the easyrec clusters.";
        }

        if ("ITEM".equals(itemTypeName)) {
            return "ITEM is a reserved item type name used by easyrec.";
        }
        
        if (!itemTypeName.equals(itemTypeName.replaceAll("[^A-Z_0-9]+", ""))) {
            return "Only use machine readable UPPERCASE names containing 0-9, A-Z and _ ";
        }

        return "";
    }

    @Override
    public void deleteItemTypeForTenant(Integer tenantId, Integer itemType) throws Exception {
        // delete all Rules with itemtype
        itemAssocDAO.removeItemAssocByTenantAndItemType(tenantId, itemType);
        // delete all actions with itemtype
        actionDAO.removeActionsByTenantAndItemType(tenantId, itemType);
        // TODO: check for itemTypes in plugin configurations
        // delete all items with itemtype
        String it = itemTypeDAO.getTypeById(tenantId, itemType);
        itemDAO.remove(tenantId, it);
        // delete itemType
        itemTypeDAO.deleteTypeById(tenantId, itemType);
        logger.info("Successfully removed itemType " + it + " and all references!");
    }
    
    @Override
    public int insertActionTypeForTenant(Integer tenantId, String actionType, boolean hasvalue) {
        return actionTypeDAO.insertOrUpdate(tenantId, actionType, tenantId, hasvalue);
    }

    /**
     * Lookup if an actionType has an value.
     * @param tenantId
     * @param actionType
     * @return returns true - if an action value is required, false - if no action value is required, 
     * null - if the actionType could not be found
     */
    @Override
    public Boolean hasActionValue(Integer tenantId, String actionType) {
        return actionTypeDAO.hasValue(tenantId, actionType);
    }
 
    @Override
    public Properties getTenantConfig(Integer tenantId) {
        Blob config = tenantDAO.getTenantConfig(tenantId);
        Properties tenantConfig = new Properties();
        try {
            tenantConfig.load(config.getBinaryStream());
            //tenantConfig.load(new StringReader(config));
        } catch (Exception e) {
            logger.debug("Could not load tenantConfig!", e);
            return null;
        }
        return tenantConfig;
    }

    @Override
    public int storeTenantConfig(Integer tenantId, Properties tenantConfig) {
        //StringWriter configWriter = new StringWriter();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            tenantConfig.store(baos, "");
            // tenantConfig.store(configWriter, "Last change: " + new Date(System.currentTimeMillis()).toString());
            return tenantDAO.storeTenantConfig(tenantId, baos.toString()/*configWriter.toString()*/);
        } catch (Exception e) {
            logger.error("An error occured trying to persist tenantConfig! Making no changes!", e);
        }
        return -1;
    }

    @Override
    public void updateConfigProperty(Integer tenantId, String key, String value) {

        Properties props = getTenantConfig(tenantId);
        if (props == null) {
            props = new Properties();
        }
        props.setProperty(key, value);
        storeTenantConfig(tenantId, props);
    }


    @Override
    public Properties getTenantStatistic(Integer tenantId) {
        Blob config = tenantDAO.getTenantStatistic(tenantId);
        Properties tenantStatistic = new Properties();
        try {
            tenantStatistic.load(config.getBinaryStream());
        } catch (Exception e) {
            logger.warn("An error occurred!", e);
            return null;
        }
        return tenantStatistic;
    }

    @Override
    public int storeTenantStatistic(Integer tenantId, Properties tenantStatistic) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            tenantStatistic.store(baos, "");
            return tenantDAO.storeTenantStatistic(tenantId, baos.toString()/*configWriter.toString()*/);
        } catch (Exception e) {
            logger.error("An error occured trying to persist tenantStatistic! Making no changes!", e);
        }
        return -1;
    }

    @Override
    public void updateTenantStatistic(Integer tenantId, String key, String value) {
        Properties props = getTenantStatistic(tenantId);
        if (props == null) {
            props = new Properties();
        }
        props.setProperty(key, value);
        storeTenantStatistic(tenantId, props);
    }


    @Override
    public boolean removeTenantWithTypes(TenantVO tenant) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TenantDAO getTenantDAO() {
        return tenantDAO;
    }

    public void setTenantDAO(TenantDAO tenantDAO) {
        this.tenantDAO = tenantDAO;
    }

    public ActionTypeDAO getActionTypeDAO() {
        return actionTypeDAO;
    }

    public void setActionTypeDAO(ActionTypeDAO actionTypeDAO) {
        this.actionTypeDAO = actionTypeDAO;
    }

    public AggregateTypeDAO getAggregateTypeDAO() {
        return aggregateTypeDAO;
    }

    public void setAggregateTypeDAO(AggregateTypeDAO aggregateTypeDAO) {
        this.aggregateTypeDAO = aggregateTypeDAO;
    }

    public AssocTypeDAO getAssocTypeDAO() {
        return assocTypeDAO;
    }

    public void setAssocTypeDAO(AssocTypeDAO assocTypeDAO) {
        this.assocTypeDAO = assocTypeDAO;
    }

    public ItemTypeDAO getItemTypeDAO() {
        return itemTypeDAO;
    }

    public void setItemTypeDAO(ItemTypeDAO itemTypeDAO) {
        this.itemTypeDAO = itemTypeDAO;
    }

    public SourceTypeDAO getSourceTypeDAO() {
        return sourceTypeDAO;
    }

    public void setSourceTypeDAO(SourceTypeDAO sourceTypeDAO) {
        this.sourceTypeDAO = sourceTypeDAO;
    }

    public ViewTypeDAO getViewTypeDAO() {
        return viewTypeDAO;
    }

    public void setViewTypeDAO(ViewTypeDAO viewTypeDAO) {
        this.viewTypeDAO = viewTypeDAO;
    }

    public AuthenticationDAO getAuthenticationDAO() {
        return authenticationDAO;
    }

    public void setAuthenticationDAO(AuthenticationDAO authenticationDAO) {
        this.authenticationDAO = authenticationDAO;
    }

    @Override
    public TenantConfigVO getDefaultTenantConfig() {
        return defaultTenantConfig;
    }

    public ClusterService getClusterService() {
        return clusterService;
    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    public ItemAssocDAO getItemAssocDAO() {
        return itemAssocDAO;
    }

    public void setItemAssocDAO(ItemAssocDAO itemAssocDAO) {
        this.itemAssocDAO = itemAssocDAO;
    }

    public ActionDAO getActionDAO() {
        return actionDAO;
    }

    public void setActionDAO(ActionDAO actionDAO) {
        this.actionDAO = actionDAO;
    }

    public ItemDAO getItemDAO() {
        return itemDAO;
    }

    public void setItemDAO(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

}
