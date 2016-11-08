/**Copyright 2016 Research Studios Austria Forschungsgesellschaft mBH
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
package org.easyrec.model.core;

import java.io.Serializable;

/**
 *
 * @author stephan
 */
public class ActionTypeVO implements Serializable, Comparable<ActionTypeVO> {

    private static final long serialVersionUID = 3968779878408631393L;
    
    private Integer id;
    private String name;
    private Integer tenantId;
    private Boolean hasValue;
    private Integer weight;

    public ActionTypeVO() {
    }

    public ActionTypeVO(Integer id, String name, Integer tenantId) {
        this.id = id;
        this.name = name;
        this.tenantId = tenantId;
        this.hasValue = false;
        this.weight = 1;
    }

    public ActionTypeVO(Integer id, String name, Integer tenantId, Boolean hasValue) {
        this.id = id;
        this.name = name;
        this.tenantId = tenantId;
        this.hasValue = hasValue;
        this.weight = 1;
    }

    public ActionTypeVO(Integer id, String name, Integer tenantId, Boolean hasValue, Integer weight) {
        this.id = id;
        this.name = name;
        this.tenantId = tenantId;
        this.hasValue = hasValue;
        this.weight = weight;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public Boolean getHasValue() {
        return hasValue;
    }

    public void setHasValue(Boolean hasValue) {
        this.hasValue = hasValue;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(ActionTypeVO o) {
        return this.name.compareTo(o.getName());
    }
    
    
}
