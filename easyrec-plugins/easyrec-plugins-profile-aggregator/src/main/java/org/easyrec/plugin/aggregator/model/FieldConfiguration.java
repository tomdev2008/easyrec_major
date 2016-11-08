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
package org.easyrec.plugin.aggregator.model;

import com.jayway.jsonpath.JsonPath;

/**
 *
 * @author Stephan
 */
public class FieldConfiguration {
    
    private String outputField;
    private JsonPath jsonPath;
    private Integer itemType;
    private Integer threshold;

    public FieldConfiguration(String outputField, JsonPath jsonPath) {
        this.outputField = outputField;
        this.jsonPath = jsonPath;
    }
    
    public String getOutputField() {
        return outputField;
    }

    public void setOutputField(String outputField) {
        this.outputField = outputField;
    }

    public JsonPath getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(JsonPath jsonPath) {
        this.jsonPath = jsonPath;
    }

    public Integer getItemType() {
        return itemType;
    }

    public void setItemType(Integer itemType) {
        this.itemType = itemType;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }
    
}
