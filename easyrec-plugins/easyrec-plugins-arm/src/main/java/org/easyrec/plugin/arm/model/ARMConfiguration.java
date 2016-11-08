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
package org.easyrec.plugin.arm.model;

import com.google.common.collect.Lists;
import org.easyrec.plugin.arm.model.enums.MetricTypes;
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
public class ARMConfiguration extends GeneratorConfiguration {

    public static String DEFAULT_ACTIONTYPE = "VIEW";

    public ARMConfiguration() {
        this(DEFAULT_ACTIONTYPE);
    }

    public ARMConfiguration(String actionType) {
        this.actionType = actionType;
    }

    private Integer support;
    //private TupleCounter tupleCounter;
    @XmlElementWrapper(name = "itemTypes")
    @XmlElement(name = "itemType")
    private List<String> itemTypes = Lists.newArrayList();

    @PluginParameter(
        displayName = "action type",
        shortDescription = "The type of action to be considered in rule generation.",
        description = "Defines the type of actions to be considered when rules are generated.",
        optional = false)
    private String actionType = "VIEW";

    @PluginParameter(
        displayName = "support percentage",
        shortDescription = "# of shopping baskets that must contain an item combination to be considered.",
        description = "Defines which percentage of all shopping baskets must contain a certain item combination so that this combination will be considered as significant.",
        optional = false)
    private Double supportPrcnt = 0.0;

    @PluginParameter(
        displayName = "minimum absolute support",
        shortDescription = "Minimum absolute # of shopping baskets that must contain an item combination to be considered.",
        description = "Defines the absolute minimum of shopping baskets that must contain a certain item combination for this combination to be considered as significant.",
        optional = false)
    private Integer supportMinAbs = 2;

    @PluginParameter(
        displayName = "confidence percentage",
        shortDescription = "The relation of total actions on an item to actions on this and another item. ",
        description = "Defines the confidence in an item combination (A,B) by putting it into perspective to how often A occurs without B.",
        optional = false)
    private Double confidencePrcnt = 0.0;

    @PluginParameter(
        displayName = "maximum rules per item",
        shortDescription = "Maximum number of rules generated for an item.",
        description = "When generating recommendation rules only the best N are considered. Relations exceeding this value are disregarded.",
        optional = false)
    private Integer maxRulesPerItem = 50;

    @PluginParameter(
        displayName = "exclude single-item baskets",
        shortDescription = "Baskets (users) with only a single item (action) are disregarded in rule generation. ",
        description = "Single action users (e.g. just one purchase) do not contribute to rule generation but can have a negative influence on the percentage values like confidence & support."
                + "In scenarios with a lot of single action users enabling this value can lead to more recommendations.",
        optional = false)
    private Boolean excludeSingleItemBaskests = false;


    @PluginParameter(
        displayName = "neutral rating",
        shortDescription = "The value of a rating to be considered neutral.",
        description = "Defines the threshold below which ratings are considered 'bad' and above which they are considered as 'good'.",
        optional = true)
    private Double ratingNeutral = 5.5;

    @PluginParameter(
        displayName = "metric type",
        shortDescription = "The metric to be used to calculate item relations.",
        description = "Allows to set the metric that determines the strength of a relation between items. Valid values are CONFIDENCE, CONVICTION, LIFT, LONGTAIL.",
        optional = true)
    private MetricTypes metricType = MetricTypes.CONFIDENCE;

    @PluginParameter(
            displayName = "popular items threshold",
            shortDescription = "The number of most popular items considered for rule mining.",
            description = "Defines the number of items considered for rule minig. Usually only the x most popular items are considered. CAUTION!!! This setting heavily influences the amount of memory needed by easyrec. The default value of 5000 requires a java heap size of 640MB to be on the safe side!",
            optional = false)
    private Integer maxSizeL1 = 5000;
    
    @PluginParameter(
                    displayName = "item batch size",
                    shortDescription = "The number of items loaded to memory at a time for rule mining.",
                    description = "Defines the number of items loaded to memory at a time for rule mining. Helps to keep memory usage under control for " +
                                    "large sets of items",
                    optional = false)
    private Integer itemBatchSize = 4000;

    @PluginParameter(
                    displayName = "keep top items",
                    shortDescription = "The number of most popular items always kept for analysis when batch processing.",
                    description = "When ruleminer uses batch processing to save memory it only loads a subset of the most popular items into memory at a " +
                                    "time. This amount specifies how many of the most popular items are kept in memory for analysis at all times. Keep this  value " +
                                    "significantly lower than popular items threshold. Furthermore it is always added to item batch size so be cautious with " +
                                    "memory usage.",
                    optional = false)
    private Integer L1KeepItemCount = 1000;
    
    @PluginParameter(
                    displayName = "time range (d)",
                    shortDescription = "The number of last days from which actions are considered.",
                    description = "Sets the number of days for how long back actions are considered. Use -1 for no time limit.",
                    optional = false)
    private Integer timeRange = 365;

    @PluginParameter(
                    displayName = "max basket size",
                    shortDescription = "The maximum number of items in a basket considered for rule mining.",
                    description = "The maximum number of items in a basket considered for rule mining.",
                    optional = false)
    private Integer maxBasketSize = 300;
    
    @PluginParameter(
                    displayName = "store  alternative metrics",
                    shortDescription = "Calculates and stores the values for the not chosen metric types for comparison purposes.",
                    description = "Calculates and stores the values for the not chosen metric types for comparison purposes in a database field. "
                            + "Enable for tuning and tweaking purposes to compare different metric types.",
                    optional = false)
    private boolean storeAlternativeMetrics = false;
    
    private Boolean doDeltaUpdate = false;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Double getConfidencePrcnt() {
        return confidencePrcnt;
    }

    public void setConfidencePrcnt(Double confidencePrcnt) {
        this.confidencePrcnt = confidencePrcnt;
    }

    public Boolean getDoDeltaUpdate() {
        return doDeltaUpdate;
    }

    public void setDoDeltaUpdate(Boolean doDeltaUpdate) {
        this.doDeltaUpdate = doDeltaUpdate;
    }

    public Boolean getExcludeSingleItemBaskests() {
        return excludeSingleItemBaskests;
    }

    public void setExcludeSingleItemBaskests(Boolean excludeSingleItemBaskests) {
        this.excludeSingleItemBaskests = excludeSingleItemBaskests;
    }

    public List<String> getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(List<String> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public Integer getMaxRulesPerItem() {
        return maxRulesPerItem;
    }

    public void setMaxRulesPerItem(Integer maxRulesPerItem) {
        this.maxRulesPerItem = maxRulesPerItem;
    }

    public Integer getMaxSizeL1() {
        return maxSizeL1;
    }

    public void setMaxSizeL1(Integer maxSizeL1) {
        this.maxSizeL1 = maxSizeL1;
    }

    public Integer getSupport() {
        return support;
    }

    public void setSupport(Integer support) {
        this.support = support;
    }

    public Integer getSupportMinAbs() {
        return supportMinAbs;
    }

    public void setSupportMinAbs(Integer supportMinAbs) {
        this.supportMinAbs = supportMinAbs;
    }

    public Double getSupportPrcnt() {
        return supportPrcnt;
    }

    public void setSupportPrcnt(Double supportPrcnt) {
        this.supportPrcnt = supportPrcnt;
    }

    public Double getRatingNeutral() {
        return ratingNeutral;
    }

    public void setRatingNeutral(Double ratingNeutral) {
        this.ratingNeutral = ratingNeutral;
    }

    public MetricTypes getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricTypes metricType) {
        this.metricType = metricType;
    }

    public Integer getItemBatchSize() {
        return itemBatchSize;
    }

    public void setItemBatchSize(Integer itemBatchSize) {
        this.itemBatchSize = itemBatchSize;
    }

    public Integer getL1KeepItemCount() {
        return L1KeepItemCount;
    }

    public void setL1KeepItemCount(Integer L1KeepItemCount) {
        this.L1KeepItemCount = L1KeepItemCount;
    }

    public Integer getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(Integer timeRange) {
        this.timeRange = timeRange;
    }

    public Integer getMaxBasketSize() {
        return maxBasketSize;
    }

    public void setMaxBasketSize(Integer maxBasketSize) {
        this.maxBasketSize = maxBasketSize;
    }

    public boolean getStoreAlternativeMetrics() {
        return storeAlternativeMetrics;
    }

    public void setStoreAlternativeMetrics(boolean storeAlternativeMetrics) {
        this.storeAlternativeMetrics = storeAlternativeMetrics;
    }
    
}
