/*
 * Copyright 2015 Research Studios Austria Forschungsgesellschaft mBH
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
package org.easyrec.plugin.aggregator.impl;

import com.google.common.base.Strings;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.plugin.aggregator.model.AggregatorConfiguration;
import org.easyrec.service.core.TenantService;
import org.easyrec.service.domain.TypeMappingService;

import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.easyrec.model.core.ActionTypeVO;
import org.easyrec.model.core.ActionVO;
import org.easyrec.model.core.ItemVO;
import org.easyrec.model.core.web.Item;
import org.easyrec.plugin.aggregator.AggregatorGenerator;
import org.easyrec.plugin.aggregator.AggregatorService;
import org.easyrec.plugin.aggregator.model.AggregatorConfigurationInt;
import org.easyrec.plugin.aggregator.model.AggregatorStatistics;
import org.easyrec.plugin.aggregator.model.FieldConfiguration;
import org.easyrec.plugin.aggregator.store.dao.AggregatorActionDAO;
import org.easyrec.plugin.aggregator.store.dao.AggregatorLogEntryDAO;
import org.easyrec.service.core.impl.JSONProfileServiceImpl;
import org.easyrec.store.dao.IDMappingDAO;
import org.easyrec.store.dao.core.ItemDAO;
import org.easyrec.store.dao.core.types.ActionTypeDAO;
import org.easyrec.store.dao.core.types.ItemTypeDAO;

/**
 * <DESCRIPTION>
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2015</p>
 * <p/>
 *
 * @author Stephan Zavrel
 */
public class AggregatorServiceImpl implements AggregatorService {
    private TypeMappingService typeMappingService;
    private TenantService tenantService;
    private AggregatorActionDAO aggregatorActionDAO;
    private JSONProfileServiceImpl jsonProfileService;
    private ItemDAO itemDAO;
    private IDMappingDAO idMappingDAO;
    private ItemTypeDAO itemTypeDAO;
    private AggregatorLogEntryDAO logEntryDAO;
    private ActionTypeDAO actionTypeDAO;

    // logging
    private final Log logger = LogFactory.getLog(this.getClass());

    public AggregatorServiceImpl(TypeMappingService typeMappingService, TenantService tenantService, AggregatorActionDAO aggregatorActionDAO, JSONProfileServiceImpl jsonProfileService, ItemDAO itemDAO, IDMappingDAO idMappingDAO, ItemTypeDAO itemTypeDAO, AggregatorLogEntryDAO logEntryDAO, ActionTypeDAO actionTypeDAO) {
        this.typeMappingService = typeMappingService;
        this.tenantService = tenantService;
        this.aggregatorActionDAO = aggregatorActionDAO;
        this.jsonProfileService = jsonProfileService;
        this.itemDAO = itemDAO;   
        this.idMappingDAO = idMappingDAO;
        this.itemTypeDAO = itemTypeDAO;
        this.logEntryDAO = logEntryDAO;
        this.actionTypeDAO = actionTypeDAO;
    }

    @Override
    public List<Integer> getUsersWithActions(AggregatorConfigurationInt configurationInt) {
        
        return aggregatorActionDAO.getUsersWithActions(configurationInt);
    }

    @Override
    public List<ActionVO<Integer,Integer>> getActionsForUser(Integer userId, AggregatorConfigurationInt configurationInt) {
        
        return aggregatorActionDAO.getActionsForUsers(userId, configurationInt);
    }
    
    @Override
    public void aggregateUserProfile(Integer userId, List<ActionVO<Integer,Integer>> actions, AggregatorConfigurationInt configurationInt) {
        
        //if user profile does not exist, create it
        String userIdStr = idMappingDAO.lookup(userId);
        Item userItem = itemDAO.get(configurationInt.getTenantId(), userIdStr, AggregatorGenerator.ITEMTYPE_USER);
        Object userProfile = null;
        if (userItem == null) {
            userItem = itemDAO.add(configurationInt.getTenantId(), userIdStr, AggregatorGenerator.ITEMTYPE_USER, userIdStr, "", "");
            jsonProfileService.storeProfile(userItem.getTenantId(), userItem.getItemId(), userItem.getItemType(), "{}");
        } 

        HashMap<String, HashMap<String, Integer>> tmpProfile = new HashMap<>();

        ItemVO<Integer,Integer> prevItem = null;
        for (ActionVO<Integer, Integer> action : actions) {
            // only if config for actionInfo, evaluate actions, otherwise continue until next itemID,typeID
            if(!configurationInt.getProfileFields().isEmpty()) { // item profile content is interesting
                if (!action.getItem().equals(prevItem)) { // in case the item is new -> load profile and get the fields
                    prevItem = action.getItem();
                    Object profile = jsonProfileService.getProfileParsed(action.getItem());
                    if (profile != null) { //TODO: Move out of prevItem if-clause; now multiple actions on item only count as 1!!!
                    //get fields stuff here
                        for (FieldConfiguration fc : configurationInt.getProfileFields().values()) {
                            if ((fc.getItemType()==null) || (action.getItem().getType().equals(fc.getItemType()))) {
                                addFieldToTmpProfile(fc, profile, tmpProfile, configurationInt.getWeights().get(action.getActionType()));
                            }
                        }
                    }
                }
                    
            }
            
            if(!configurationInt.getActionFields().isEmpty()) { //in case actionInfo content is interesting
                String actionInfo = action.getActionInfo();
                if (!Strings.isNullOrEmpty(actionInfo)) {
                    Object actionProfile = configurationInt.getConfiguration().jsonProvider().parse(actionInfo);
                    for (FieldConfiguration fc : configurationInt.getActionFields().values()) {
                        addFieldToTmpProfile(fc, actionProfile, tmpProfile, configurationInt.getWeights().get(action.getActionType()));
                    }
                }
            }
        }

        userProfile = convertAndOrderProfile(tmpProfile, configurationInt);
        if (configurationInt.getActionType() != null) {
            if (!((LinkedHashMap<String, Object>) userProfile).isEmpty()) {
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                String actionType = actionTypeDAO.getTypeById(configurationInt.getTenantId(), configurationInt.getActionType());
                map.put(actionType.toLowerCase(), userProfile);
                userProfile = map;
            }
        }
        try {
            jsonProfileService.storeProfileFieldParsed(userItem.getTenantId(), userItem.getItemId(), userItem.getItemType(), "$", "upa", userProfile);
        } catch (Exception ex) {
            logger.error("An error occured storing the user profile", ex);
        }
        
    }

    private void addFieldToTmpProfile(FieldConfiguration fc, Object sourceProfile, HashMap<String,HashMap<String,Integer>> userProfile, Integer weight) {
        try {
            ArrayList<String> fields = new ArrayList<>();
            Object field = fc.getJsonPath().read(sourceProfile);
            if (!(field instanceof List)) {
                if (!(field instanceof String)) {
                    field = field.toString();
                }
                fields.add((String)field);
            } else {
                for (Object field1 : (List)field) {
                    if (!(field1 instanceof String) && (field1 != null)) {
                    field1 = field1.toString();
                }
                fields.add((String)field1);
                }
            }

            HashMap<String, Integer> outputField = userProfile.get(fc.getOutputField());
            if (outputField == null) {
                outputField = new HashMap<>();
                userProfile.put(fc.getOutputField(), outputField);
            }
            for (String field1 : fields) {
                Integer counter = outputField.get(field1);
                if (counter == null) { 
                    counter = weight;
                } else {
                    counter += weight;
                }
                outputField.put(field1, counter);  
            }
            
        } catch (PathNotFoundException pnfe) {
            logger.debug("Path not found in profile " + pnfe.getMessage());
        }
        
    }
    
    private LinkedHashMap<String, Object> convertAndOrderProfile(HashMap<String, HashMap<String, Integer>> tmpProfile, AggregatorConfigurationInt configuration) {
        
        LinkedHashMap<String, Object> profile = new LinkedHashMap<>();
        for (String field : tmpProfile.keySet()) {
            Integer threshold = null;
            if (configuration.getActionFields().get(field) != null) {
                threshold = configuration.getActionFields().get(field).getThreshold();
            } else if (configuration.getProfileFields().get(field) != null) {
                threshold = configuration.getProfileFields().get(field).getThreshold();
            }
            if (threshold == null) threshold = Integer.MAX_VALUE;
            profile.put(field, convertAndOrderField(tmpProfile.get(field), threshold));
        }
           
        return profile;
    }
    
    private List<KeyValue> convertAndOrderField(HashMap<String, Integer> tmpField, Integer threshold) {

        List<Entry<String, Integer>> list = new ArrayList<>(tmpField.entrySet());
        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            @Override
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue()); //Note: exchanged o2 and o1 for descending order!
            }
        });

        List<KeyValue> result = new ArrayList<>();
        int i = 0;
        for (Iterator<Entry<String, Integer>> it = list.iterator(); it.hasNext(); i++) {
            if (i >= threshold) break;
            Entry<String, Integer> entry = it.next();
            result.add(new DefaultKeyValue(entry.getKey(), entry.getValue()));
        }
        return result;
        
    }
        
    @Override
    public Integer getNumberOfActions(AggregatorConfigurationInt intConfiguration, Date lastRun) {
        return aggregatorActionDAO.getNumberOfActions(intConfiguration.getTenantId(), intConfiguration.getActionType(), lastRun);
    }
    
    
    @Override
    public AggregatorConfigurationInt mapTypesToConfiguration(AggregatorConfiguration configuration) throws Exception {

        AggregatorConfigurationInt ret;

        ret = new AggregatorConfigurationInt(configuration.getTenantId(), null ,new ArrayList<Integer>(), null, configuration.getMaxRulesPerItem(), configuration.getDoDeltaUpdate(), configuration.getLastRun());

        Integer actionId;
        try {
            actionId = typeMappingService.getIdOfActionType(configuration.getTenantId(), configuration.getActionType());
        } catch (IllegalArgumentException iae) {
            actionId = null;
            String sb = "Action '" + configuration.getActionType() + "' not valid for Tenant '" + configuration.getTenantId() + "'! Action will not be considered for Aggragation!";
            logger.debug(sb);
        }
        ret.setActionType(actionId);
        Integer typeUser;
        try {
            typeUser = itemTypeDAO.getIdOfType(configuration.getTenantId(), AggregatorGenerator.ITEMTYPE_USER);
        } catch (IllegalArgumentException iae) {
            logger.info("itemType USER not found for tenant " + configuration.getTenantId() + "! Adding now.");
            typeUser = itemTypeDAO.insertOrUpdate(configuration.getTenantId(), AggregatorGenerator.ITEMTYPE_USER, Boolean.TRUE);
        }

        ret.setItemTypeUser(typeUser);

        for (String type : configuration.getItemTypes()) {
            Integer itemTypeId = typeMappingService.getIdOfItemType(configuration.getTenantId(), type);
            if (itemTypeId != null) {
                ret.getItemTypes().add(itemTypeId);
            } else {
                logger.info("ItemType '" + type + "' not valid for Tenant '" + configuration.getTenantId() +
                        "'! ItemType will not be considered in rulemining!");
            }
        }
        if (ret.getItemTypes() == null) {
            String sb = "No valid ItemTypes defined for Tenant '" + configuration.getTenantId() + "'! Skipping this rulemining configuration!";
            logger.info(sb);
            throw new Exception(sb);
        }

        Integer assocTypeId = typeMappingService.getIdOfAssocType(configuration.getTenantId(), configuration.getAssociationType());
        if (assocTypeId == null) {
            String sb = "AssocType '" + configuration.getAssociationType() + "' not valid for tenant '" + configuration.getTenantId() + "'! Skipping analysis!";
            logger.info(sb);
            throw new Exception(sb);
        }
        ret.setAssocType(assocTypeId);
        
        Set<ActionTypeVO> actionTypes = actionTypeDAO.getTypeVOs(configuration.getTenantId());
        Map<Integer,Integer> weights = ret.getWeights();
        for (ActionTypeVO actionType : actionTypes) {
            weights.put(actionType.getId(), actionType.getWeight());
        }
        
        if (!Strings.isNullOrEmpty(configuration.getActionInfoFields())) {
            String[] fields = configuration.getActionInfoFields().replaceAll("\n", "").split(";");
            for (String field : fields) {
                String[] fieldInfo = field.split(",");
                JsonPath jp = JsonPath.compile(fieldInfo[1]);
                FieldConfiguration fc = new FieldConfiguration(fieldInfo[0], jp);
                if (fieldInfo.length > 2) {
                    Integer it = null;
                    try {
                        it = itemTypeDAO.getIdOfType(configuration.getTenantId(), fieldInfo[2]);
                    } catch (IllegalArgumentException iae) {
                        logger.debug("ItemType " + fieldInfo[2] + " not found for tenant " + configuration.getTenantId() + "! Will aggregate all itemTypes.");
                        it = null;
                    }
                    fc.setItemType(it);   
                }
                if (fieldInfo.length > 3) { //percentage Threshold not feasible because of deltaUpdate
                    Integer thres = null;
                    try {
                        thres = Integer.parseInt(fieldInfo[3]);
                    } catch (NumberFormatException iae) {
                        logger.debug("Threshold is not an integer: " + fieldInfo[3] + " !" + configuration.getTenantId() + "! Ignoring threshold.");
                        thres = null;
                    }
                    fc.setThreshold(thres);
                    ret.setHasActionFieldThreshold(true);
                }
                ret.getActionFields().put(fc.getOutputField(),fc);
            }
        }
        
        if (!Strings.isNullOrEmpty(configuration.getItemProfileFields())) {
            String[] fields = configuration.getItemProfileFields().replaceAll("\n", "").split(";");
            for (String field : fields) {
                String[] fieldInfo = field.split(",");
                JsonPath jp = JsonPath.compile(fieldInfo[1]);
                FieldConfiguration fc = new FieldConfiguration(fieldInfo[0], jp);
                if (fieldInfo.length > 2) {
                    Integer it = null;
                    try {
                        it = itemTypeDAO.getIdOfType(configuration.getTenantId(), fieldInfo[2]);
                    } catch (IllegalArgumentException iae) {
                        logger.debug("ItemType " + fieldInfo[2] + " not found for tenant " + configuration.getTenantId() + "! Will aggregate all itemTypes.");
                        it = null;
                    }
                    fc.setItemType(it);   
                }
                if (fieldInfo.length > 3) {
                    Integer thres = null;
                    try {
                        thres = Integer.parseInt(fieldInfo[3]);
                    } catch (NumberFormatException iae) {
                        logger.debug("Threshold is not an integer: " + fieldInfo[3] + " !" + configuration.getTenantId() + "! Ignoring threshold.");
                        thres = null;
                    }
                    fc.setThreshold(thres);
                    ret.setHasProfileFieldThreshold(true);
                }
                ret.getProfileFields().put(fc.getOutputField(), fc);
            }
        }

        if (configuration.getDoDeltaUpdate()) {
            ret.setLastRun(logEntryDAO.getLastLogEntryForTenant(configuration.getTenantId(), assocTypeId, AggregatorGenerator.ID, AggregatorGenerator.VERSION));
        }
        
        return ret;

    }

    private void pruneProfile(LinkedHashMap<String, Object> profileField, Integer threshold) {
        for (Iterator<Entry<String, Object>> it = profileField.entrySet().iterator(); it.hasNext();) {
            Entry<String,Object> entry = it.next();
            Integer val = (Integer) entry.getValue();
            if (val < threshold) {
                it.remove(); // using iterator to avoid ConcurrentModificationExceptions!!
            }
        }
    }
    
    @Override
    public void removeOldRules(AggregatorConfigurationInt configuration,
                               AggregatorStatistics stats) {

//        itemAssocDAO.removeItemAssocByTenant(configuration.getTenantId(), configuration.getAssocType(),
//                typeMappingService.getIdOfSourceType(configuration.getTenantId(), AggregatorGenerator.ID.toString() + "/" + AggregatorGenerator.VERSION),
//                stats.getStartDate());
    }

    
    // getters and setters
    public TypeMappingService getTypeMappingService() {
        return typeMappingService;
    }

    public void setTypeMappingService(TypeMappingService typeMappingService) {
        this.typeMappingService = typeMappingService;
    }

    public TenantService getTenantService() {
        return tenantService;
    }

    public void setTenantService(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    public AggregatorActionDAO getAggregatorActionDAO() {
        return aggregatorActionDAO;
    }

    public void setAggregatorActionDAO(AggregatorActionDAO aggregatorActionDAO) {
        this.aggregatorActionDAO = aggregatorActionDAO;
    }

    public JSONProfileServiceImpl getJsonProfileService() {
        return jsonProfileService;
    }

    public void setJsonProfileService(JSONProfileServiceImpl jsonProfileService) {
        this.jsonProfileService = jsonProfileService;
    }

    public ItemDAO getItemDAO() {
        return itemDAO;
    }

    public void setItemDAO(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

    public IDMappingDAO getIdMappingDAO() {
        return idMappingDAO;
    }

    public void setIdMappingDAO(IDMappingDAO idMappingDAO) {
        this.idMappingDAO = idMappingDAO;
    }

    public ItemTypeDAO getItemTypeDAO() {
        return itemTypeDAO;
    }

    public void setItemTypeDAO(ItemTypeDAO itemTypeDAO) {
        this.itemTypeDAO = itemTypeDAO;
    }

    public AggregatorLogEntryDAO getLogEntryDAO() {
        return logEntryDAO;
    }

    public void setLogEntryDAO(AggregatorLogEntryDAO logEntryDAO) {
        this.logEntryDAO = logEntryDAO;
    }

}
