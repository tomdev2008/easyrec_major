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

import org.apache.solr.client.solrj.beans.Field;

/**
 *
 * @author Stephan
 */
public class SolrItem {
    
    @Field("tenant_i")
    private Integer tenant;
    
    @Field("type_i")
    private Integer itemType;
    
    @Field("id_i")
    private Integer id;
    
    @Field
    private Float score;

    public SolrItem() {
    }

    public Integer getTenant() {
        return tenant;
    }

    public void setTenant(Integer tenant) {
        this.tenant = tenant;
    }

    public Integer getItemType() {
        return itemType;
    }

    public void setItemType(Integer itemType) {
        this.itemType = itemType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }
    
}
