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

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import org.easyrec.plugin.stats.GeneratorStatistics;

/**
 *
 * @author szavrel
 */
@XmlRootElement
public class AggregatorStatistics extends GeneratorStatistics implements Serializable {
    private static final long serialVersionUID = -3202254935026334488L;

    // ------------------------- FIELDS --------------------
    private String sessionName;
    private Integer nrUsers;
    private Integer nrProducts;
    private Integer sizeRules;
    private long duration;
    private String exception = null;

    public AggregatorStatistics() {

    }

    // ------------------------- GETTER / SETTER METHODS --------------------

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Integer getNrUsers() {
        return nrUsers;
    }

    public void setNrUsers(Integer nrUsers) {
        this.nrUsers = nrUsers;
    }

    public Integer getNrProducts() {
        return nrProducts;
    }

    public void setNrProducts(Integer nrProducts) {
        this.nrProducts = nrProducts;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public Integer getSizeRules() {
        return sizeRules;
    }

    public void setSizeRules(Integer sizeRules) {
        this.sizeRules = sizeRules;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

}
