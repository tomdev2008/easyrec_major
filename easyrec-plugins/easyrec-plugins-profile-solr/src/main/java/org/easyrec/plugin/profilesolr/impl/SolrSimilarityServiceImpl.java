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
package org.easyrec.plugin.profilesolr.impl;

import com.google.common.base.Strings;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.easyrec.model.core.ItemAssocVO;
import org.easyrec.model.core.ItemVO;
import org.easyrec.model.core.web.Item;
import org.easyrec.plugin.profilesolr.SolrSimilarityGenerator;
import org.easyrec.plugin.profilesolr.SolrSimilarityService;
import org.easyrec.plugin.profilesolr.model.QueryFieldConfiguration;
import org.easyrec.plugin.profilesolr.model.SolrItem;
import org.easyrec.plugin.profilesolr.model.SolrSimilarityConfiguration;
import org.easyrec.plugin.profilesolr.model.SolrSimilarityConfigurationInt;
import org.easyrec.plugin.profilesolr.model.SolrSimilarityStatistics;
import org.easyrec.plugin.profilesolr.store.dao.RuleminingItemAssocDAO;
import org.easyrec.plugin.profilesolr.store.dao.SolrItemDAO;
import org.easyrec.service.domain.TypeMappingService;
import org.easyrec.store.dao.IDMappingDAO;
import org.easyrec.store.dao.core.types.ItemTypeDAO;

/**
 *
 * @author Stephan
 */
public class SolrSimilarityServiceImpl implements SolrSimilarityService {
   
    private final ItemTypeDAO itemTypeDAO;
    private final TypeMappingService typeMappingService;
    private final SolrItemDAO solrItemDAO;
    private final IDMappingDAO idMappingDAO;
    private final RuleminingItemAssocDAO itemAssocDAO;
    
    private SolrClient solrClient;

        
    // logging
    private final Log logger = LogFactory.getLog(this.getClass());

    public SolrSimilarityServiceImpl(ItemTypeDAO itemTypeDAO, TypeMappingService typeMappingService, SolrItemDAO solrItemDAO, IDMappingDAO idMappingDAO, RuleminingItemAssocDAO itemAssocDAO) {
        this.itemTypeDAO = itemTypeDAO;
        this.typeMappingService = typeMappingService;
        this.solrItemDAO = solrItemDAO;
        this.idMappingDAO = idMappingDAO;
        this.itemAssocDAO = itemAssocDAO;
    }

    
    @Override
    public SolrSimilarityConfigurationInt mapTypesToConfiguration(SolrSimilarityConfiguration configuration) throws Exception {

        SolrSimilarityConfigurationInt ret;

        ret = new SolrSimilarityConfigurationInt(configuration.getTenantId(), null ,new ArrayList<Integer>(), null, configuration.getMaxRulesPerItem(), configuration.getLastRun());

        Integer typeUser;
        try {
            typeUser = itemTypeDAO.getIdOfType(configuration.getTenantId(), SolrSimilarityGenerator.ITEMTYPE_USER);
        } catch (IllegalArgumentException iae) {
            logger.info("itemType USER not found for tenant " + configuration.getTenantId() + "! Adding now.");
            typeUser = itemTypeDAO.insertOrUpdate(configuration.getTenantId(), SolrSimilarityGenerator.ITEMTYPE_USER, Boolean.TRUE);
        }

        ret.setItemTypeUser(typeUser);

        Integer assocTypeId = typeMappingService.getIdOfAssocType(configuration.getTenantId(), configuration.getAssociationType());
        if (assocTypeId == null) {
            String sb = "AssocType '" + configuration.getAssociationType() + "' not valid for tenant '" + configuration.getTenantId() + "'! Skipping analysis!";
            logger.info(sb);
            throw new Exception(sb);
        }
        ret.setAssocType(assocTypeId);
        
        if (!Strings.isNullOrEmpty(configuration.getIndexProfileFields())) {
            String[] fields = configuration.getIndexProfileFields().replaceAll("\n", "").split(";");
            for (String field : fields) {
                JsonPath jp = JsonPath.compile(field);
                ret.getIndexFields().add(jp);
            }
        }
        
        if (!Strings.isNullOrEmpty(configuration.getQueryProfileFields())) {
            String[] fields = configuration.getQueryProfileFields().replaceAll("\n", "").split(";");
            for (String field : fields) {
                String[] fieldInfo = field.split(",");
                JsonPath jp = JsonPath.compile(fieldInfo[0]);
                Integer boost = null;
                if (fieldInfo.length > 1) {
                    boost = Integer.parseInt(fieldInfo[1]);
                }
                ret.getQueryFields().add(new QueryFieldConfiguration(jp, boost));
            }
        }
        

//        if (configuration.getDoDeltaUpdate()) {
//            ret.setLastRun(logEntryDAO.getLastLogEntryForTenant(configuration.getTenantId(), assocTypeId, AggregatorGenerator.ID, AggregatorGenerator.VERSION));
//        }
        
        return ret;

    }  
    
    
    @Override
    public void addItemsToIndex(SolrSimilarityConfigurationInt configuration) throws Exception {
        List<Item> items = solrItemDAO.getNonUserItems(configuration);
        // TODO: implement changeDate on items correctly! index from lastRun!!
        for (Item item : items) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", item.getTenantId() + item.getItemType() + item.getItemId());
            doc.addField("tenant_i", item.getTenantId());
            doc.addField("type_i", itemTypeDAO.getIdOfType(item.getTenantId(),item.getItemType()));
            doc.addField("id_i", idMappingDAO.lookupOnly(item.getItemId()));
            String all = "";
            Object parsedProfile = configuration.getConfiguration().jsonProvider().parse(item.getProfileData());
            for ( JsonPath jp : configuration.getIndexFields()) {
                all += addFieldToSolrString(jp, parsedProfile);
            }
            doc.addField("all", all);

            UpdateResponse response = solrClient.add(doc);
        }
        
//        logger.info("Response status:" + response.getStatus());
        
        solrClient.commit();
    }

    private String addFieldToSolrString(JsonPath jp, Object sourceProfile) {
        String fields = "";
        try {    
            Object field = jp.read(sourceProfile);
            if (!(field instanceof List)) {
                if (!(field instanceof String)) {
                    field = field.toString();
                }
                fields += (String) field + " ";
            } else {
                for (Object field1 : (List)field) {
                    if (!(field1 instanceof String)) {
                    field1 = field1.toString();
                }
                fields += (String)field1 + " ";
                }
            }
        } catch (PathNotFoundException pnfe) {
            logger.debug("Path not found in profile " + pnfe.getMessage());
        }
        return fields;
    }
    
    @Override
    public void matchProfiles(SolrSimilarityConfigurationInt configuration, SolrSimilarityStatistics stats) {
        
        List<Item> users = solrItemDAO.getUserItems(configuration);
        int count = 0;
        for (Item user : users) {
            Object parsedProfile = configuration.getConfiguration().jsonProvider().parse(user.getProfileData());
            String query = "";
            for (QueryFieldConfiguration fc : configuration.getQueryFields()) {
                List<Object> fields = fc.getJsonPath().read(parsedProfile, configuration.getConfiguration());//Configuration.builder().options(Option.ALWAYS_RETURN_LIST,Option.DEFAULT_PATH_LEAF_TO_NULL).build());
                for (Object field : fields) {
                    if (field instanceof String) {
                        //field = (String) field;
                        if (fc.getBoost() != null) field = (String) field + '^' + fc.getBoost();
                        //field = field.substring(field.lastIndexOf("[") + 2, field.length() - 2);
                        query += field + " ";
                    } else {
                        stats.setException("The following path does not return simple String values an is ignored! Check your settings: " + fc.getJsonPath().getPath());
                    }
                }

            }
            //logger.info("Query:" + query);
            SolrQuery sQuery = new SolrQuery(query);
            sQuery.addFilterQuery("tenant_i:" + configuration.getTenantId());
            sQuery.add("fl","tenant_i,type_i,id_i,score");
            try {
                QueryResponse response = solrClient.query(sQuery);
                
                List<SolrItem> list = response.getBeans(SolrItem.class);
                if (!list.isEmpty()) {
                    List<ItemAssocVO<Integer,Integer>> itemAssocs = new ArrayList<>();
                    Integer userId = idMappingDAO.lookupOnly(user.getItemId());

                    for (SolrItem item : list) {
                        ItemAssocVO<Integer, Integer> rule = new ItemAssocVO<>(
                                configuration.getTenantId(), 
                                new ItemVO<>(user.getTenantId(), userId, configuration.getItemTypeUser()),
                                configuration.getAssocType(),
                                (double) item.getScore(),
                                new ItemVO<>(item.getTenant(), item.getId(), item.getItemType()),
                                typeMappingService.getIdOfSourceType(configuration.getTenantId(), SolrSimilarityGenerator.ID.toString() + "/" + SolrSimilarityGenerator.VERSION),
                                "",
                                typeMappingService.getIdOfViewType(configuration.getTenantId(), TypeMappingService.VIEW_TYPE_COMMUNITY),
                                true,
                                stats.getStartDate());
                        
                        itemAssocs.add(rule);
                    }
                    count += itemAssocs.size();
                    for (ItemAssocVO<Integer, Integer> itemAssoc : itemAssocs) {
                        itemAssocDAO.insertOrUpdateItemAssoc(itemAssoc);
                    }
                    
                }

            } catch (SolrServerException ex) {
                logger.debug("An Excption occured quering solr.", ex);
            } catch (IOException ex) {
                logger.debug("An Excption occured quering solr.", ex);
            }
        }
        stats.setSizeRules(count);
        stats.setNumberOfRulesCreated(count);
        
    }
    
    @Override
    public void removeOldRules(SolrSimilarityConfigurationInt configuration,
                               SolrSimilarityStatistics stats) {

        itemAssocDAO.removeItemAssocByTenant(configuration.getTenantId(), configuration.getAssocType(),
                typeMappingService.getIdOfSourceType(configuration.getTenantId(), SolrSimilarityGenerator.ID.toString() + "/" + SolrSimilarityGenerator.VERSION),
                stats.getStartDate());
    }
        
    @Override
    public SolrClient getSolrClient() {
        return solrClient;
    }

    @Override
    public void setSolrClient(SolrClient solrClient) {
        this.solrClient = solrClient;
    }
    
   
}
