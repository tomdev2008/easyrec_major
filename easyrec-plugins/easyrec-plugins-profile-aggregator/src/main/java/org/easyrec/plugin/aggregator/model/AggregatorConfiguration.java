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
package org.easyrec.plugin.aggregator.model;

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
public class AggregatorConfiguration extends GeneratorConfiguration {

    public static String DEFAULT_ACTIONTYPE = "";

    public AggregatorConfiguration() {
        this(DEFAULT_ACTIONTYPE);
    }

    public AggregatorConfiguration(String actionType) {
        this.actionType = actionType;
    }

    private Date lastRun;

    @XmlElementWrapper(name = "itemTypes")
    @XmlElement(name = "itemType")
    private List<String> itemTypes = Lists.newArrayList();

    @PluginParameter(
        displayName = "action type",
        shortDescription = "The type of action to be considered for profile aggregation.",
        description = "Defines the type of actions to be considered for aggeregation of a user profile.",
        optional = false)
    private String actionType = "VIEW";
       
    @PluginParameter(
        displayName = "action info fields",
        shortDescription = "JSON fields in the actionInfo to be analysed.",
        description = "The JSON fields in the actionInfo to be analysed. Use the following format: targetField,sourceJSONPath,(itemType),(threshold);itemType and threshold are optional:"
                + "if not provided items of all types are considered. Use semicolon to define multiple fields for analysis. Example: genres,$.genre,MOVIE,3; tries to read the field "
                + "$.genre from the actionInfo where the item type is MOVIE and writes the result to the field genres in the result profile. At least 3 (threshold) counts of the specific genre are needed"
                + "to be stored in the profile",
        optional = true,
        asTextArea = true)
    private String actionInfoFields;
    
    @PluginParameter(
        displayName = "item profile fields",
        shortDescription = "JSON fields in the item profile to be analysed.",
        description = "The JSON fields in the item profile to be analysed. Use the following format: targetField,sourceJSONPath,(itemType), (threshold);itemType and threshold are optional:"
                + "if not provided items of all types are considered. Use semicolon to define multiple fields for analysis. Example: genres,$.genre,MOVIE,3; tries to read the field "
                + "$.genre from the item profile where the item type is MOVIE and writes the result to the field genres in the result profile. At least 3 (threshold) counts of the specific genre are needed"
                + "to be stored in the profile",
        optional = true,
        asTextArea = true)
    private String itemProfileFields;
    
    private Integer maxRulesPerItem;

    @PluginParameter(
        displayName = "do delta update",
        shortDescription = "If true, only users with actions since the last plugin run are considered.",
        description = "If true, only users with actions since the last plugin run are considered and the profiles updated accordingly.",
        optional = false)
    private Boolean doDeltaUpdate = true;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Boolean getDoDeltaUpdate() {
        return doDeltaUpdate;
    }

    public void setDoDeltaUpdate(Boolean doDeltaUpdate) {
        this.doDeltaUpdate = doDeltaUpdate;
    }


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

    public String getActionInfoFields() {
        return actionInfoFields;
    }

    public void setActionInfoFields(String actionInfoFields) {
        this.actionInfoFields = actionInfoFields;
    }

    public String getItemProfileFields() {
        return itemProfileFields;
    }

    public void setItemProfileFields(String itemProfileFields) {
        this.itemProfileFields = itemProfileFields;
    }

}
