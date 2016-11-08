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
package org.easyrec.service.core.impl;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.model.core.ItemVO;
import org.easyrec.model.core.web.Item;
import org.easyrec.service.domain.TypeMappingService;
import org.easyrec.store.dao.IDMappingDAO;
import org.easyrec.store.dao.core.ProfileDAO;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @author Stephan
 */
public class JSONProfileServiceImpl implements InitializingBean {

    // logging
    private final Log logger = LogFactory.getLog(this.getClass());
    
    private final ProfileDAO profileDAO;
    private final IDMappingDAO idMappingDAO;
    private final TypeMappingService typeMappingService;

    public JSONProfileServiceImpl(ProfileDAO profileDAO, IDMappingDAO idMappingDAO, TypeMappingService typeMappingService) {
        this.profileDAO = profileDAO;
        this.idMappingDAO = idMappingDAO;
        this.typeMappingService = typeMappingService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            
            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }

            @Override
            public MappingProvider mappingProvider() {
                return new JacksonMappingProvider();
            }
        });
        
    }
    
    
    public String getProfile(Item item) {
        return getProfile(item.getTenantId(), item.getItemId(), item.getItemType());
    }

    public String getProfile(ItemVO<Integer, Integer> item) {
        return profileDAO.getProfile(item.getTenant(), item.getItem(), item.getType());
    }

    public String getProfile(Integer tenantId, String itemId, String itemTypeId) {
        return profileDAO.getProfile(tenantId, idMappingDAO.lookupOnly(itemId), typeMappingService.getIdOfItemType(tenantId, itemTypeId));
    }
    
    public Object getProfileParsed(Integer tenantId, String itemId, String itemTypeId) {
        String profile = getProfile(tenantId, itemId, itemTypeId);
        if (profile != null) {
            return Configuration.defaultConfiguration().jsonProvider().parse(profile);
        } else {
            return profile;
        }
    }

    public Object getProfileParsed(ItemVO<Integer, Integer> item) {
        String profile = getProfile(item);
        if (profile != null) {
            return Configuration.defaultConfiguration().jsonProvider().parse(profile);
        } else {
            return profile;
        }
    }
    
    public boolean storeProfile(Integer tenantId, String itemId, String itemType, String profile) {
        return profileDAO.storeProfile(tenantId, idMappingDAO.lookup(itemId), typeMappingService.getIdOfItemType(tenantId, itemType), profile) != 0;
    }

    public boolean deleteProfile(Integer tenantId, String itemId, String itemType) {
        return profileDAO.deleteProfile(tenantId, idMappingDAO.lookupOnly(itemId), typeMappingService.getIdOfItemType(tenantId, itemType));
    }

    public Object loadProfileField(Integer tenantId, String itemId, String itemType, String dimensionPath) throws Exception {
        String profile = getProfile(tenantId,itemId,itemType);
        JsonPath jp = JsonPath.compile(dimensionPath);
        Object result = jp.read(profile);
        return result;
    }
    
    public String loadProfileFieldJSON(Integer tenantId, String itemId, String itemType, String dimensionPath) throws Exception {
        Object result = loadProfileField(tenantId, itemId, itemType, dimensionPath);
        String res = Configuration.defaultConfiguration().jsonProvider().toJson(result);
        return res;
    }

    public boolean storeProfileField(Integer tenantId, String itemId, String itemTypeId, String Path, String key, String value) throws Exception {
        
        String profile = getProfile(tenantId,itemId,itemTypeId);
        if (profile != null) {
            JsonPath jp = JsonPath.compile(Path);
            Object updated = jp.put(Configuration.defaultConfiguration().jsonProvider().parse(profile), key, Configuration.defaultConfiguration().jsonProvider().parse(value), Configuration.defaultConfiguration());
            return storeProfile(tenantId, itemId, itemTypeId, Configuration.defaultConfiguration().jsonProvider().toJson(updated));
        }
        return false;
    }
    
    public boolean storeProfileFieldParsed(Integer tenantId, String itemId, String itemTypeId, String Path, String key, Object value) throws Exception {
        
        String profile = getProfile(tenantId,itemId,itemTypeId);
        if (profile != null) {
            JsonPath jp = JsonPath.compile(Path);
            Object updated = jp.put(Configuration.defaultConfiguration().jsonProvider().parse(profile), key, value, Configuration.defaultConfiguration());
            return storeProfile(tenantId, itemId, itemTypeId, Configuration.defaultConfiguration().jsonProvider().toJson(updated));
        }
        return false;
    }
    
    public boolean pushToArrayField(Integer tenantId, String itemId, String itemTypeId, String path, String value) throws Exception {
        
        String profile = getProfile(tenantId,itemId,itemTypeId);
        JsonPath jp = JsonPath.compile(path);
        Object array = jp.read(profile, Configuration.defaultConfiguration());
        if (!Configuration.defaultConfiguration().jsonProvider().isArray(array)) throw new IllegalArgumentException("The given field is not an array!"); 
        Object updated = jp.add(Configuration.defaultConfiguration().jsonProvider().parse(profile), Configuration.defaultConfiguration().jsonProvider().parse(value), Configuration.defaultConfiguration());
        return storeProfile(tenantId, itemId, itemTypeId, Configuration.defaultConfiguration().jsonProvider().toJson(updated));    
    }

    public boolean deleteProfileField(Integer tenantId, String itemId, String itemType, String deletePath) throws Exception {
        String profile = getProfile(tenantId,itemId,itemType);
        JsonPath jp = JsonPath.compile(deletePath);
        Object updated = jp.delete(Configuration.defaultConfiguration().jsonProvider().parse(profile), Configuration.defaultConfiguration());
        return storeProfile(tenantId, itemId, itemType, Configuration.defaultConfiguration().jsonProvider().toJson(updated));
    }

    public List<ItemVO<Integer, Integer>> getItemsByItemType(Integer tenantId, String itemType, int count) {
        return profileDAO.getItemsByItemType(tenantId, typeMappingService.getIdOfItemType(tenantId, itemType), count);
    }
    
    
    
}
