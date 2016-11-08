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

package org.easyrec.plugin.profilesolr.model;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author szavrel
 */
public class SolrSimilarityConfigurationInt {

    private Integer tenantId;
    private Integer assocType;
    private Integer itemTypeUser;
    private Integer maxRulesPerItem;
//    private Boolean doDeltaUpdate;
    private Date lastRun;
    private List<JsonPath> indexFields = new ArrayList<>();
    private List<QueryFieldConfiguration> queryFields = new ArrayList<>();
    private Configuration configuration;

    public SolrSimilarityConfigurationInt() {

    }

    public SolrSimilarityConfigurationInt(Integer tenantId, Integer assocType, List<Integer> itemTypes, Integer actionType, Integer maxRulesPerItem, Date lastRun) {
        this.tenantId = tenantId;
        this.assocType = assocType;
        this.maxRulesPerItem = maxRulesPerItem;
//        this.doDeltaUpdate = doDeltaUpdate;
        this.lastRun = lastRun;
        this.configuration = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.ALWAYS_RETURN_LIST);
    }

//    public Boolean getDoDeltaUpdate() {
//        return doDeltaUpdate;
//    }
//
//    public void setDoDeltaUpdate(Boolean doDeltaUpdate) {
//        this.doDeltaUpdate = doDeltaUpdate;
//    }

    public Integer getMaxRulesPerItem() {
        return maxRulesPerItem;
    }

    public void setMaxRulesPerItem(Integer maxRulesPerItem) {
        this.maxRulesPerItem = maxRulesPerItem;
    }

    public Integer getAssocType() {
        return assocType;
    }

    public void setAssocType(Integer assocType) {
        this.assocType = assocType;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public Date getLastRun() {
        return lastRun;
    }

    public void setLastRun(Date lastRun) {
        this.lastRun = lastRun;
    }

    public List<JsonPath> getIndexFields() {
        return indexFields;
    }

    public void setIndexFields(List<JsonPath> indexFields) {
        this.indexFields = indexFields;
    }

    public List<QueryFieldConfiguration> getQueryFields() {
        return queryFields;
    }

    public void setQueryFields(List<QueryFieldConfiguration> queryFields) {
        this.queryFields = queryFields;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Integer getItemTypeUser() {
        return itemTypeUser;
    }

    public void setItemTypeUser(Integer itemTypeUser) {
        this.itemTypeUser = itemTypeUser;
    }  
    
}
