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
package org.easyrec.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.sun.jersey.spi.resource.Singleton;
import java.io.IOException;
import org.easyrec.service.core.exception.ItemNotFoundException;
import org.easyrec.model.core.web.Message;
import org.easyrec.model.core.web.SuccessMessage;
import org.easyrec.store.dao.web.OperatorDAO;
import org.easyrec.vocabulary.MSG;
import org.easyrec.vocabulary.WS;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.easyrec.model.core.web.Item;
import org.easyrec.model.web.JSONParams;
import org.easyrec.service.core.impl.JSONProfileServiceImpl;
import org.easyrec.service.domain.TypeMappingService;
import org.easyrec.store.dao.core.ItemDAO;

/**
 * This class is a REST webservice facade for the ProfileService
 *
 * @author Stephan Zavrel
 */

/*
* The path of the URI must begin with the version number of the API.
* Followed by an optional "json" part. If this part is missing the result
* will be returned as XML.
*/
@Path("1.1/json/profile")
@Produces({"application/json"})
@Singleton

public class JSONProfileWebService {

    @Context
    public HttpServletRequest request;

    private final JSONProfileServiceImpl profileService;
    private final OperatorDAO operatorDAO;
    private final ObjectMapper objectMapper;
    private final TypeMappingService typeMappingService;
    private final ItemDAO itemDAO;

    private final static String JAMON_PROFILE_LOAD = "rest.profile.json.load";
    private final static String JAMON_PROFILE_STORE = "rest.profile.json.store";
    private final static String JAMON_PROFILE_DELETE = "rest.profile.json.delete";
    private final static String JAMON_PROFILE_FIELD_STORE = "rest.profile.json.field.store";
    private final static String JAMON_PROFILE_FIELD_LOAD = "rest.profile.json.field.load";
    private final static String JAMON_PROFILE_FIELD_DELETE = "rest.profile.json.field.delete";

    public JSONProfileWebService(JSONProfileServiceImpl profileService, OperatorDAO operatorDAO, ObjectMapper objectMapper, TypeMappingService typeMappingService, ItemDAO itemDAO) {
        this.profileService = profileService;
        this.operatorDAO = operatorDAO;
        this.objectMapper = objectMapper;
        this.typeMappingService = typeMappingService;
        this.itemDAO = itemDAO;
    }


    /**
     * This method stores the given profile to the Item defined by the tenantID,
     * itemID and the itemTypeID. If there is already a Profile it will be overwritten.
     * If the Item does not exist it will be created.
     *
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the item where the profile will be stored
     * @param itemID       the itemID if the item where the profile will be stored
     * @param itemType     the itemType of the item where the profile will be stored
     * @param profile      the XML profile which will be stored
     * @return a response object containing information about the success of the operation
     */
    @POST
    @Path("/store")
    @Consumes("application/x-www-form-urlencoded")
    public Response storeProfile(@FormParam("apikey") String apiKey,
                                 @FormParam("tenantid") String tenantID,
                                 @FormParam("itemid") String itemID,
                                 @DefaultValue("ITEM") @FormParam("itemtype") String itemType,
                                 @FormParam("profile") String profile) {

        return storeProfileInternal(apiKey, tenantID, itemID, itemType, profile);        

    }
    
        /**
     * This method stores the given profile to the Item defined by the tenantID,
     * itemID and the itemTypeID. If there is already a Profile it will be overwritten.
     * If the Item does not exist it will be created.
     *
     * @param params
     * @return a response object containing information about the success of the operation
     */
    @POST
    @Path("/store")
    public Response storeProfile(JSONParams params) {

        return storeProfileInternal(params.getApikey(), params.getTenantid(), params.getItemid(), params.getItemtype(), params.getProfile());
        
    }
    
    /**
     * This method stores the given profile to the Item defined by the tenantID,
     * itemID and the itemTypeID. If there is already a Profile it will be overwritten.
     * If the Item does not exist it will be created.
     *
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the item where the profile will be stored
     * @param itemID       the itemID if the item where the profile will be stored
     * @param itemType     the itemType of the item where the profile will be stored
     * @param itemDescription
     * @param itemUrl
     * @param itemImageUrl
     * @param profile      the XML profile which will be stored
     * @return a response object containing information about the success of the operation
     */
    @POST
    @Path("/storeitemwithprofile")
    @Consumes("application/x-www-form-urlencoded")
    public Response storeItemWithProfile(
                                 @FormParam("apikey") String apiKey,
                                 @FormParam("tenantid") String tenantID,
                                 @FormParam("itemid") String itemID,
                                 @DefaultValue("ITEM") @FormParam("itemtype") String itemType,
                                 @FormParam("itemdescription") String itemDescription,
                                 @FormParam("itemurl") String itemUrl,
                                 @FormParam("itemimageurl") String itemImageUrl,
                                 @FormParam("profile") String profile) {

        return storeItemWithProfileInternal(apiKey, tenantID, itemID, itemType, itemDescription, itemUrl, itemImageUrl, profile);
    }

        /**
     * This method stores the given profile to the Item defined by the tenantID,
     * itemID and the itemTypeID. If there is already a Profile it will be overwritten.
     * If the Item does not exist it will be created.
     *
     * @param params
     * @return a response object containing information about the success of the operation
     */
    @POST
    @Path("/storeitemwithprofile")
    public Response storeItemWithProfileJSON(JSONParams params) {

        return storeItemWithProfileInternal(params.getApikey(), params.getTenantid(), params.getItemid(), params.getItemtype(), params.getItemdescription(), params.getItemurl(), params.getItemimageurl(), params.getProfile());
    }
    
    /**
     * This method deletes the profile of the item defined by the tenantID,
     * itemID and the itemTypeID
     *
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the item whose profile will be deleted
     * @param itemID       the itemID if the item whose profile will be deleted
     * @param itemType     the itemType of the item whose profile will be deleted
     * @return a response object containing information about the success of the operation
     */
    @DELETE
    @Path("/delete")
    public Response deleteProfile(@QueryParam("apikey") String apiKey,
                                  @QueryParam("tenantid") String tenantID,
                                  @QueryParam("itemid") String itemID,
                                  @DefaultValue("ITEM") @QueryParam("itemtype") String itemType) {

        Monitor mon = MonitorFactory.start(JAMON_PROFILE_DELETE);

        List<Message> errorMessages = new ArrayList<>();
        List<Message> responseObject = new ArrayList<>();

        try {
            if (checkParameters(apiKey, tenantID, itemID, itemType, errorMessages)) {
                Integer coreTenantID = operatorDAO.getTenantId(apiKey, tenantID);
                if (coreTenantID == null)
                    errorMessages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
                else {
                    if (profileService.deleteProfile(coreTenantID, itemID, itemType))
                        responseObject.add(MSG.PROFILE_DELETED);
                    else
                        errorMessages.add(MSG.PROFILE_NOT_DELETED);
                }
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            if (illegalArgumentException.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
            } else
                errorMessages.add(MSG.PROFILE_NOT_DELETED.append(illegalArgumentException.getMessage()));
        } catch (ItemNotFoundException itemNotFoundException) {
            errorMessages.add(MSG.ITEM_NOT_EXISTS);
        } catch (RuntimeException runtimeException) {
            errorMessages.add(MSG.PROFILE_NOT_DELETED);
        }

        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_DELETE, null);
        mon.stop();
        return response;
    }

    /**
     * This method returns the profile of the item defined by the tenantID,
     * itemID and the itemTypeID
     *
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the item whose profile will be returned
     * @param itemID       the itemID if the item whose profile will be returned
     * @param itemType     the itemType of the item whose profile will be returned
     * @param callback
     * @return a response object containing the wanted profile
     */
    @GET
    @Path("/load")
    public Response loadProfile(@QueryParam("apikey") String apiKey,
                                @QueryParam("tenantid") String tenantID,
                                @QueryParam("itemid") String itemID,
                                @DefaultValue("ITEM") @QueryParam("itemtype") String itemType,
                                @QueryParam("callback") String callback) {

        Monitor mon = MonitorFactory.start(JAMON_PROFILE_LOAD);

        List<Message> errorMessages = new ArrayList<>();
        Object responseObject = null;

        try {
            if (checkParameters(apiKey, tenantID, itemID, itemType, errorMessages)) {
                Integer coreTenantID = operatorDAO.getTenantId(apiKey, tenantID);
                if (coreTenantID == null)
                    errorMessages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
                else {
                    String profile = profileService.getProfile(coreTenantID, itemID, itemType);
                    if (profile != null)
                        responseObject = new ResponseProfileField("profile/load", tenantID, itemID, itemType, "profile", profile);
                    else
                        errorMessages.add(MSG.PROFILE_NOT_LOADED);
                }
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            if (illegalArgumentException.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
            } else
                errorMessages.add(MSG.PROFILE_NOT_LOADED.append(illegalArgumentException.getMessage()));
        } catch (ItemNotFoundException itemNotFoundException) {
            errorMessages.add(MSG.ITEM_NOT_EXISTS);
        } catch (RuntimeException runtimeException) {
            errorMessages.add(MSG.PROFILE_NOT_LOADED);
        }

        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_LOAD, callback);
        mon.stop();
        return response;
    }

    /**
     * This method stores a value into a specific field of the profile which belongs to the item
     * defined by the tenantID, itemID and the itemTypeID. The field can be addressed
     * by a XPath expression. If the field does not exist it will be created.
     *
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the addressed item
     * @param itemID       the itemID if the addressed item
     * @param itemType     the itemType of the addressed item
     * @param path
     * @param key
     * @param value        the value which will be saved in the field
     * @return a response object containing information about the success of the operation
     */
    @PUT
    @Path("/field/store")
    @Consumes("application/x-www-form-urlencoded")
    public Response storeField(@FormParam("apikey") String apiKey,
                               @FormParam("tenantid") String tenantID,
                               @FormParam("itemid") String itemID,
                               @DefaultValue("ITEM") @FormParam("itemtype") String itemType,
                               @FormParam("path") String path,
                               @FormParam("key") String key,
                               @FormParam("value") String value) {

        return storeFieldInternal(apiKey, tenantID, itemID, itemType, path, key, value);

    }

    /**
     * This method stores a value into a specific field of the profile which belongs to the item
     * defined by the tenantID, itemID and the itemTypeID. The field can be addressed
     * by a XPath expression. If the field does not exist it will be created.
     *
     * @param params
     * @return a response object containing information about the success of the operation
     */
    @PUT
    @Path("/field/store")
    public Response storeField(JSONParams params) {

        return storeFieldInternal(params.getApikey(), params.getTenantid(), params.getItemid(), params.getItemtype(), params.getPath(), params.getKey(), params.getValue());
    }
    
        /**
     * This method stores a value into a specific field of the profile which belongs to the item
     * defined by the tenantID, itemID and the itemTypeID. The field can be addressed
     * by a XPath expression. If the field does not exist it will be created.
     *
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the addressed item
     * @param itemID       the itemID if the addressed item
     * @param itemType     the itemType of the addressed item
     * @param path
     * @param value        the value which will be saved in the field
     * @return a response object containing information about the success of the operation
     */
    @PUT
    @Path("/field/push")
    @Consumes("application/x-www-form-urlencoded")
    public Response pushFieldToArray(@FormParam("apikey") String apiKey,
                               @FormParam("tenantid") String tenantID,
                               @FormParam("itemid") String itemID,
                               @DefaultValue("ITEM") @FormParam("itemtype") String itemType,
                               @FormParam("path") String path,
                               @FormParam("value") String value) {

        return pushFieldInternal(apiKey, tenantID, itemID, itemType, path, value);
    }
    
            /**
     * This method stores a value into a specific field of the profile which belongs to the item
     * defined by the tenantID, itemID and the itemTypeID. The field can be addressed
     * by a XPath expression. If the field does not exist it will be created.
     *
     * @param params
     * @return a response object containing information about the success of the operation
     */
    @PUT
    @Path("/field/push")
    public Response pushFieldToArray(JSONParams params) {

        return pushFieldInternal(params.getApikey(), params.getTenantid(), params.getItemid(), params.getItemtype(), params.getPath(), params.getValue());
    }
    
    /**
     * This method deletes a specific field of the profile which belongs to the item
     * defined by the tenantID, itemID and the itemTypeID. The field can be addressed
     * by a XPath expression
     *
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the addressed item
     * @param itemID       the itemID if the addressed item
     * @param itemType     the itemType of the addressed item
     * @param path
     * @return a response object containing information about the success of the operation
     */
    @DELETE
    @Path("/field/delete")
    public Response deleteField(@QueryParam("apikey") String apiKey,
                                @QueryParam("tenantid") String tenantID,
                                @QueryParam("itemid") String itemID,
                                @DefaultValue("ITEM") @QueryParam("itemtype") String itemType,
                                @QueryParam("path") String path) {

        Monitor mon = MonitorFactory.start(JAMON_PROFILE_FIELD_DELETE);

        List<Message> errorMessages = new ArrayList<>();
        List<Message> responseObject = new ArrayList<>();

        try {
            if (checkParameters(apiKey, tenantID, itemID, itemType, errorMessages) &&
                    checkParameterField(path, errorMessages)) {
                Integer coreTenantID = operatorDAO.getTenantId(apiKey, tenantID);
                if (coreTenantID == null) {
                    errorMessages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
                }
                else {
                    if (profileService.deleteProfileField(coreTenantID, itemID, itemType, path))
                        responseObject.add(MSG.PROFILE_FIELD_DELETED);
                    else
                        errorMessages.add(MSG.PROFILE_FIELD_NOT_DELETED);
                }
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                if (e.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
                } else
                    errorMessages.add(MSG.PROFILE_FIELD_NOT_DELETED.append(e.getMessage()));
            }
            else if (e instanceof ItemNotFoundException) {
                errorMessages.add(MSG.ITEM_NOT_EXISTS.append(
                    " ItemNotFoundException: " + e.getMessage()));
            }  else {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        "Exception: " + e.getMessage()));
            }

        } 
        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_FIELD_DELETE, null);
        mon.stop();
        return response;
    }

    /**
     * This method loads the value from a specific field of the profile which belongs to the item
     * defined by the tenantID, itemID and the itemTypeID. The field can be addressed
     * by a XPath expression. If multiple profile fields are found an error message will be returned.
     *
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the addressed item
     * @param itemID       the itemID if the addressed item
     * @param itemType     the itemType of the addressed item
     * @param path
     * @param callback     if set and responseType is json the result will be returned
     *                     via this javascript callback function (optional)
     * @return a response object containing the value of the field.
     * @see ResponseProfileField
     */
    @GET
    @Path("/field/load")
    public Response loadField(
                              @QueryParam("apikey") String apiKey,
                              @QueryParam("tenantid") String tenantID,
                              @QueryParam("itemid") String itemID,
                              @DefaultValue("ITEM") @QueryParam("itemtype") String itemType,
                              @QueryParam("path") String path,
                              @QueryParam("callback") String callback) {

        Monitor mon = MonitorFactory.start(JAMON_PROFILE_FIELD_LOAD);

        List<Message> errorMessages = new ArrayList<>();
        Object responseObject = null;

        try {
            if (checkParameters(apiKey, tenantID, itemID, itemType, errorMessages) &&
                    checkParameterField(path, errorMessages)) {
                Integer coreTenantID = operatorDAO.getTenantId(apiKey, tenantID);
                if (coreTenantID == null)
                    errorMessages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
                else {
                    String values = profileService.loadProfileFieldJSON(coreTenantID, itemID, itemType, path);
                    if (values != null)
                    {
                        responseObject = new ResponseProfileField("profile/field/load",
                                    tenantID, itemID, itemType, path, values);
                    }
                    else
                        errorMessages.add(MSG.PROFILE_FIELD_NOT_LOADED);
                }
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                if (e.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
                } else
                    errorMessages.add(MSG.PROFILE_FIELD_NOT_LOADED.append(e.getMessage()));
            }
            else if (e instanceof ItemNotFoundException) {
                errorMessages.add(MSG.ITEM_NOT_EXISTS.append(
                    " ItemNotFoundException: " + e.getMessage()));
            } else {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        "Exception: " + e.getMessage()));
            }
        }
        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_FIELD_LOAD, callback);
        mon.stop();
        return response;
    }


    private Response storeProfileInternal(String apiKey, String tenantID, String itemID, String itemType, String profile) {
        Monitor mon = MonitorFactory.start(JAMON_PROFILE_STORE);

        List<Message> errorMessages = new ArrayList<>();
        List<Message> responseObject = new ArrayList<>();

        try {
            if (checkParameters(apiKey, tenantID, itemID, itemType, errorMessages) &&
                    checkParameterProfile(profile, errorMessages)) {
                Integer coreTenantID = operatorDAO.getTenantId(apiKey, tenantID);
                if (coreTenantID == null)
                    errorMessages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
                else {
                    if (profileService.storeProfile(coreTenantID, itemID, itemType, profile))
                        responseObject.add(MSG.PROFILE_SAVED);
                    else
                        errorMessages.add(MSG.PROFILE_NOT_SAVED);
                }
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            if (illegalArgumentException.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
            } else
                errorMessages.add(MSG.PROFILE_NOT_SAVED);
        } catch (RuntimeException runtimeException) {
            errorMessages.add(MSG.PROFILE_NOT_SAVED);
        }

        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_STORE, null);
        mon.stop();
        return response;
    }
    
    private Response storeItemWithProfileInternal(String apiKey, String tenantID, String itemID, String itemType,
                                 String itemDescription, String itemUrl, String itemImageUrl, String profile) {

        Monitor mon = MonitorFactory.start(JAMON_PROFILE_STORE);

        List<Message> errorMessages = new ArrayList<>();
        List<Message> responseObject = new ArrayList<>();

        try {
            Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantID);
            if (checkParameters(coreTenantId, itemID, itemDescription, itemUrl, errorMessages) && checkParameterProfile(profile, errorMessages)) {

                itemType = checkItemType(itemType, coreTenantId, Item.DEFAULT_STRING_ITEM_TYPE, errorMessages);
                if (itemType != null) {
                    itemDAO.insertOrUpdate(coreTenantId, itemID, itemType, itemDescription, itemUrl, itemImageUrl);
            

                    if (profileService.storeProfile(coreTenantId, itemID, itemType, profile))
                        responseObject.add(MSG.PROFILE_SAVED);
                    else
                        errorMessages.add(MSG.PROFILE_NOT_SAVED);
                }
            
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            if (illegalArgumentException.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
            } else
                errorMessages.add(MSG.PROFILE_NOT_SAVED);
        } catch (RuntimeException runtimeException) {
            errorMessages.add(MSG.PROFILE_NOT_SAVED);
        }

        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_STORE, null);
        mon.stop();
        return response;
    }
    
    
    private Response storeFieldInternal(String apiKey, String tenantID, String itemID, String itemType, String path, String key, String value) {
        Monitor mon = MonitorFactory.start(JAMON_PROFILE_FIELD_STORE);

        List<Message> errorMessages = new ArrayList<>();
        List<Message> responseObject = new ArrayList<>();

        try {
            if (checkParameters(apiKey, tenantID, itemID, itemType, errorMessages) &&
                    checkParameterField(path, errorMessages) &&
                    checkParameterKey(key, errorMessages) &&
                    checkParameterValue(value, errorMessages)) {
                Integer coreTenantID = operatorDAO.getTenantId(apiKey, tenantID);
                if (coreTenantID == null)
                    errorMessages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
                else {
                    if (profileService.storeProfileField(
                            coreTenantID, itemID, itemType,
                            path, key, value))
                        responseObject.add(MSG.PROFILE_FIELD_SAVED);
                    else
                        errorMessages.add(MSG.PROFILE_FIELD_NOT_SAVED);
                }
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                if (e.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
                } else {
                    errorMessages.add(MSG.PROFILE_FIELD_NOT_SAVED.append(e.getMessage()));
                }
            } else {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    "Exception: " + e.getMessage()));
            }

        } 
        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_FIELD_STORE, null);
        mon.stop();
        return response; 
    }
    
    private Response pushFieldInternal(String apiKey, String tenantID, String itemID, String itemType, String path, String value) {
        Monitor mon = MonitorFactory.start(JAMON_PROFILE_FIELD_STORE);

        List<Message> errorMessages = new ArrayList<>();
        List<Message> responseObject = new ArrayList<>();

        try {
            if (checkParameters(apiKey, tenantID, itemID, itemType, errorMessages) &&
                    checkParameterValue(value, errorMessages)) {
                Integer coreTenantID = operatorDAO.getTenantId(apiKey, tenantID);
                if (coreTenantID == null)
                    errorMessages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
                else {
                    if (profileService.pushToArrayField(
                            coreTenantID, itemID, itemType,
                            path, value))
                        responseObject.add(MSG.PROFILE_FIELD_SAVED);
                    else
                        errorMessages.add(MSG.PROFILE_FIELD_NOT_SAVED);
                }
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                if (e.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
                } else {
                    errorMessages.add(MSG.PROFILE_FIELD_NOT_SAVED.append(e.getMessage()));
                }
            } else {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    "Exception: " + e.getMessage()));
            }

        } 
        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_FIELD_PUSH, null);
        mon.stop();
        return response; 
    }
    /**
     * This method takes an object and creates a <code>Response</code> object
     * out of it which will be returned. If <code>messages</code> contains error
     * messages they will be send back instead.
     * The format of the <code>Response</code>
     * depends on the <code>responseType</code>.
     * Supported types are <code>application/xml</code> and <code>application/json</code>
     *
     * @param respondData  an object which will be returned as a
     *                     <code>Response</code> object
     * @param messages     a list of <code>Message</code> objects which contain
     *                     error messages of the API request
     * @param callback     if set and responseType is json the result will be returned
     *                     via this javascript callback function (optional)
     * @return a <code>Response</code> object containing the <code>responseData</code>
     *         in the format defined with <code>responseType</code>
     */
    private Response formatResponse(Object respondData,
                                    List<Message> messages,
                                    String serviceName,
                                    String callback) {

        //handle error messages if existing
        if (messages.size() > 0) {
            throw new EasyRecException(messages, serviceName, WS.RESPONSE_TYPE_JSON, callback);
        }

        if (respondData instanceof List) {
            respondData = new ResponseSuccessMessage(serviceName, (List<SuccessMessage>) respondData);
        }

        //convert respondData to Respond object
        if (callback != null) {
            return Response.ok(new JSONPObject(callback, respondData),
                    WS.RESPONSE_TYPE_JSCRIPT).build();
        } else {
            return Response.ok(respondData, WS.RESPONSE_TYPE_JSON).build();
        }

    }

    /**
     * This method checks if the <code>apiKey, tenantID, itemID</code> and <code>itemType</code>
     * is not null or an empty string. If this is not the case the corresponding error message is
     * added to the <code>messages</code> list.
     *
     * @param apiKey   the apiKey which will be checked
     * @param tenantID the tenantID which will be checked
     * @param itemID   the itemID which will be checked
     * @param itemType the itemType which will be checked
     * @param messages a <code>List&lt;Message&gt;</code> where the error messages are appended
     * @return returns <code>true</code> if all parameters are positively checked and
     *         <code>false</code> otherwise
     */
    private boolean checkParameters(String apiKey, String tenantID,
                                    String itemID, String itemType, List<Message> messages) {
        boolean result = true;
        if (Strings.isNullOrEmpty(apiKey)) {
            messages.add(MSG.PROFILE_NO_API_KEY);
            result = false;
        }
        if (Strings.isNullOrEmpty(tenantID)) {
            messages.add(MSG.PROFILE_NO_TENANT_ID);
            result = false;
        }
        if (Strings.isNullOrEmpty(itemID)) {
            messages.add(MSG.ITEM_NO_ID);
            result = false;
        }
        if (Strings.isNullOrEmpty(itemType)) {
            messages.add(MSG.PROFILE_NO_ITEM_TYPE);
            result = false;
        }
        return result;
    }

        private boolean checkParameters(Integer coreTenantId, String itemId, String itemDescription, String itemUrl,
                                 List<Message> messages) {

        boolean result = true;
        if (coreTenantId == null) {
            messages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
            result = false;
        }
        if (Strings.isNullOrEmpty(itemId)) {
            messages.add(MSG.ITEM_NO_ID);
            result = false;
        }
        if (Strings.isNullOrEmpty(itemDescription)) {
            messages.add(MSG.ITEM_NO_DESCRIPTION);
            result = false;
        } else {
            if (itemDescription.length() > 500) {
                itemDescription = itemDescription.substring(0, 499);
            }
        }

        if (itemUrl == null) {
            messages.add(MSG.ITEM_NO_URL);
            result = false;
        }
        return result;
    }
    
    /**
     * This method checks if the <code>profile</code> is not null
     * or an empty string. If this is not the case the corresponding
     * error message is added to the <code>messages</code> list.
     *
     * @param profile  the profile which will be checked
     * @param messages a <code>List&lt;Message&gt;</code> where the error messages are appended
     * @return returns <code>true</code> if all parameters are positively checked and
     *         <code>false</code> otherwise
     */
    private boolean checkParameterProfile(String profile, List<Message> messages) {
        try {
            objectMapper.readTree(profile);
        } catch (IOException ex) {
            messages.add(MSG.INVALID_JSON);
            return false;
        }
        return true;
    }

    /**
     * This method checks if the <code>profile field</code> is not null
     * or an empty string. If this is not the case the corresponding
     * error message is added to the <code>messages</code> list.
     *
     * @param field    the field which will be checked
     * @param messages a <code>List&lt;Message&gt;</code> where the error messages are appended
     * @return returns <code>true</code> if all parameters are positively checked and
     *         <code>false</code> otherwise
     */
    private boolean checkParameterField(String field, List<Message> messages) {
        if (Strings.isNullOrEmpty(field)) {
            messages.add(MSG.PROFILE_NO_FIELD_PROVIDED);
            return false;
        } else
            return true;
    }
    
    /**
     * This method checks if the <code>profile field key</code> is not null
     * or an empty string. If this is not the case the corresponding
     * error message is added to the <code>messages</code> list.
     *
     * @param key    the key which will be checked
     * @param messages a <code>List&lt;Message&gt;</code> where the error messages are appended
     * @return returns <code>true</code> if all parameters are positively checked and
     *         <code>false</code> otherwise
     */
    private boolean checkParameterKey(String key, List<Message> messages) {
        if (Strings.isNullOrEmpty(key)) {
            messages.add(MSG.PROFILE_NO_KEY_PROVIDED);
            return false;
        } else
            return true;
    }

    /**
     * This method checks if the <code>profile field value</code> is not null
     * or an empty string. If this is not the case the corresponding
     * error message is added to the <code>messages</code> list.
     *
     * @param value    the value which will be checked
     * @param messages a <code>List&lt;Message&gt;</code> where the error messages are appended
     * @return returns <code>true</code> if all parameters are positively checked and
     *         <code>false</code> otherwise
     */
    private boolean checkParameterValue(Object value, List<Message> messages) {
        if (value == null) {
            messages.add(MSG.PROFILE_NO_VALUE_PROVIDED);
            return false;
        } else
            return true;
    }
    
    private String checkItemType(String itemType, Integer coreTenantId, @Nullable String defaultValue, List<Message> messages) {
        if (itemType != null)
            itemType = CharMatcher.WHITESPACE.trimFrom(itemType);

        if (Strings.isNullOrEmpty(itemType))
            return defaultValue;
        else
            try {
                typeMappingService.getIdOfItemType(coreTenantId, itemType, true);

                return itemType;
            } catch (IllegalArgumentException ex) {
                messages.add(MSG.OPERATION_FAILED.append(String.format(" itemType %s not found for tenant", itemType)));
                return null;
            }
    }
    
}
