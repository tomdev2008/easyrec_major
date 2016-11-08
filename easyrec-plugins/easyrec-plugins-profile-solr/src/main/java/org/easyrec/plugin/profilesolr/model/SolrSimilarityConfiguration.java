/**Copyright 2015 Research Studios Austria Forschungsgesellschaft mBH
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

import com.google.common.collect.Lists;
import java.util.Date;
import org.easyrec.plugin.configuration.PluginParameter;
import org.easyrec.plugin.generator.GeneratorConfiguration;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 *
 * @author szavrel
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SolrSimilarityConfiguration extends GeneratorConfiguration {

    public SolrSimilarityConfiguration() {
    }

    private Date lastRun;

    @XmlElementWrapper(name = "itemTypes")
    @XmlElement(name = "itemType")
    private List<String> itemTypes = Lists.newArrayList();
       
    @PluginParameter(
        displayName = "index profile fields",
        shortDescription = "JSON fields in the item profiles to be indexed for matching.",
        description = "The JSON fields in the item profiles to be analysed. A semicolon seperated list of profile fields.",
        optional = true,
        asTextArea = true)
    private String indexProfileFields;
    
    @PluginParameter(
        displayName = "query profile fields",
        shortDescription = "JSON fields in the profile to be matched.",
        description = "The JSON fields in the profile that are used to build the query for matching.",
        optional = true,
        asTextArea = true)
    private String queryProfileFields;
    
    private Integer maxRulesPerItem;

//    @PluginParameter(
//        displayName = "do delta update",
//        shortDescription = "If true, only the actions since the last plugin run are considered.",
//        description = "If true, only the actions since the last plugin run are considered and the profiles updated accordingly.",
//        optional = false)
//    private Boolean doDeltaUpdate = true;
//
//
//    public Boolean getDoDeltaUpdate() {
//        return doDeltaUpdate;
//    }
//
//    public void setDoDeltaUpdate(Boolean doDeltaUpdate) {
//        this.doDeltaUpdate = doDeltaUpdate;
//    }


    public List<String> getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(List<String> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public Date getLastRun() {
        return lastRun;
    }

    public void setLastRun(Date lastRun) {
        this.lastRun = lastRun;
    }

    public Integer getMaxRulesPerItem() {
        return maxRulesPerItem;
    }

    public void setMaxRulesPerItem(Integer maxRulesPerItem) {
        this.maxRulesPerItem = maxRulesPerItem;
    }

    public String getIndexProfileFields() {
        return indexProfileFields;
    }

    public void setIndexProfileFields(String indexProfileFields) {
        this.indexProfileFields = indexProfileFields;
    }

    public String getQueryProfileFields() {
        return queryProfileFields;
    }

    public void setQueryProfileFields(String queryProfileFields) {
        this.queryProfileFields = queryProfileFields;
    }

}
