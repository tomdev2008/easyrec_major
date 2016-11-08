/**Copyright 2010 Research Studios Austria Forschungsgesellschaft mBH
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
package org.easyrec.model.core.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.google.common.base.Strings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.util.core.Web;
import org.easyrec.utils.io.Text;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * This class is the basic model of an Item.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: szavrel $<br/>
 * $Date: 2012-02-02 18:49:38 +0100 (Do, 02 Feb 2012) $<br/>
 * $Revision: 18703 $</p>
 *
 * @author phlavac
 * @version <CURRENT PROJECT VERSION>
 * @since <PROJECT VERSION ON FILE CREATION>
 */
@XmlRootElement
public class Item implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 4214924728903902312L;

    @XmlTransient
    private static final Log logger = LogFactory.getLog(Item.class);

    private String id;
    private Integer tenantId;
    private String itemId;
    private String itemType;
    private String description;
    private String url;
    private String imageUrl;
    private String relativeUrl;
    private String relativeImageUrl;
    private String absoluteUrl;
    private String absoluteImageUrl;
    private Double value;
    private boolean active;
    private String creationDate;

    //added by FK on 2012-12-18 to enable adding profile data to recommended items
    private String profileData;

    // TODO: move to vocabulary?
    public static final String DEFAULT_STRING_ITEM_TYPE = "ITEM";

    public Item() {}

    public Item(String id, Integer tenantId, String itemId, String itemType, String description, String url,
                String imageUrl, Double value, boolean active, String creationDate) {
        super();
        this.id = id;
        this.tenantId = tenantId;
        this.itemId = itemId;
        this.itemType = itemType;
        this.description = description;
        this.url = url;
        this.imageUrl = imageUrl;
        this.value = value;
        this.active = active;
        this.creationDate = creationDate;
    }

    /**
     * Returns the internal id of the item table
     *
     *
     */
    @XmlTransient
    @JsonIgnore
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlTransient
    @JsonIgnore
    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * returns the itemid that was set by the user through the REST-API
     *
     *
     */
    @XmlElement(name = "id")
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    /**
     * return a trimmed decoded item description, if decoding fails the
     * trimmed description is returned.
     *
     *
     */
    @XmlElement(nillable = true)
    public String getDescription() {
        try {
            if (description == null)
                return "n/a";
            return URLDecoder.decode(description.trim(), "UTF-8");
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Could not decode description of item with id '" + id + "'", e);
            return description.trim();
        }

    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(nillable = true)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement(nillable = true)
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @XmlTransient
    @JsonIgnore
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }


    /**
     * If tenant url is part of item url,
     * return only the relative item image url else return the full item image url.
     *
     *
     */
    @JsonIgnore
    public String getRelativeImageUrl() {
        return relativeImageUrl;
    }

    /**
     * Sets the relative item image url concerning the given tenant url.
     * If tenant url is part of item image url,
     * only the relative item image url is set else the full item image url is set.
     * Also the absolute item image url is set.
     *
     * @param tenantUrl
     */
    public void setRelativeImageUrl(String tenantUrl) {

        if ((imageUrl != null) &&(imageUrl.indexOf(tenantUrl) >= 0)) {
            this.relativeImageUrl = imageUrl
                    .substring(imageUrl.indexOf(tenantUrl) + tenantUrl.length(), imageUrl.length());
            this.absoluteImageUrl = imageUrl;
        } else {
            this.relativeImageUrl = imageUrl;
            this.absoluteImageUrl = Text.matchMax(tenantUrl, imageUrl);
        }

    }

    /**
     * If tenant url is part of item url,
     * return only the relative item url else return the full item url.
     *
     *
     */
    @JsonIgnore
    public String getRelativeUrl() {
        return relativeUrl;
    }

    /**
     * Sets the relative item url concerning the given tenant url.
     * If tenant url is part of item url,
     * only the relative item url is set else the full item url is set.
     * Also the absolute item url is set.
     *
     * @param tenantUrl
     */
    public void setRelativeUrl(String tenantUrl) {
        if ((url != null) && (url.indexOf(tenantUrl) >= 0)) {
            this.relativeUrl = url.substring(url.indexOf(tenantUrl) + tenantUrl.length(), url.length());
            this.absoluteUrl = url;
        } else {
            this.relativeUrl = url;
            this.absoluteUrl = Text.matchMax(tenantUrl, url);
        }
    }

    /**
     * Returns the absolut Image Url of an Item (if was set with setRelativeImageUrl
     *
     *
     */
    @JsonIgnore
    public String getAbsoluteImageUrl() {
        return absoluteImageUrl;
    }

    /**
     * Returns the absolut Url of an Item (if was set with setRelativeUrl
     *
     *
     */
    @JsonIgnore
    public String getAbsoluteUrl() {
        return absoluteUrl;
    }

    @XmlElement(nillable = true)
    @JsonRawValue
    public String getProfileData() {
        return profileData;
    }

    public void setProfileData(String profileData) {
        this.profileData = profileData;
    }

    /**
     * sets the url for backtracking an item.
     *
     * @param session
     * @param userId
     * @param tenant
     * @param itemFrom
     * @param itemFromType
     * @param itemTo
     * @param itemToType
     * @param assocType
     * @param url
     */
    public static String getTrackingUrl(Session session, Integer userId, RemoteTenant tenant, Integer itemFrom, Integer itemFromType,
                                        Integer itemTo, Integer itemToType, Integer assocType, String url) {

        try {
            if (!Strings.isNullOrEmpty(tenant.getBacktrackingURL())) {
                return tenant.getBacktrackingURL() + "/t?" + "r=" + userId + "&t=" + tenant.getId() + "&f=" +
                    itemFrom + "&ft=" + itemFromType + "&i=" + itemTo + "&it=" + itemToType + "&a=" + assocType + "&u=" + URLEncoder.encode(url, "UTF-8");                
            } else {
                return Web.getExtendedWebAppPathFromRequestURI(session.getRequest()) + "/t?" + "r=" + userId + "&t=" + tenant.getId() + "&f=" +
                    itemFrom + "&ft=" + itemFromType + "&i=" + itemTo + "&it=" + itemToType + "&a=" + assocType + "&u=" + URLEncoder.encode(url, "UTF-8");
            }
        } catch (Exception e) {
            logger.warn("An error occurred!", e);
            return null;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (active ? 1231 : 1237);
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((imageUrl == null) ? 0 : imageUrl.hashCode());
        result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
        result = prime * result + ((itemType == null) ? 0 : itemType.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Item other = (Item) obj;
        if (active != other.active) return false;
        if (description == null) {
            if (other.description != null) return false;
        } else if (!description.equals(other.description)) return false;
        if (id == null) {
            if (other.id != null) return false;
        } else if (!id.equals(other.id)) return false;
        if (imageUrl == null) {
            if (other.imageUrl != null) return false;
        } else if (!imageUrl.equals(other.imageUrl)) return false;
        if (itemId == null) {
            if (other.itemId != null) return false;
        } else if (!itemId.equals(other.itemId)) return false;
        if (itemType == null) {
            if (other.itemType != null) return false;
        } else if (!itemType.equals(other.itemType)) return false;
        if (tenantId == null) {
            if (other.tenantId != null) return false;
        } else if (!tenantId.equals(other.tenantId)) return false;
        if (url == null) {
            if (other.url != null) return false;
        } else if (!url.equals(other.url)) return false;
        return true;
    }

}
