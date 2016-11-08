/*
 * Copyright 2012 Research Studios Austria Forschungsgesellschaft mBH
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

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.sun.jersey.spi.resource.Singleton;
import org.easyrec.service.core.exception.ItemNotFoundException;
import org.easyrec.model.core.web.Message;
import org.easyrec.model.core.web.SuccessMessage;
import org.easyrec.service.core.ProfileService;
import org.easyrec.service.core.exception.FieldNotFoundException;
import org.easyrec.service.core.exception.MultipleProfileFieldsFoundException;
import org.easyrec.store.dao.web.OperatorDAO;
import org.easyrec.vocabulary.MSG;
import org.easyrec.vocabulary.WS;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class is a REST webservice facade for the ProfileService
 *
 * @author Fabian Salcher
 */

/*
* The path of the URI must begin with the version number of the API.
* Followed by an optional "json" part. If this part is missing the result
* will be returned as XML.
*/
@Path("1.0{responseType: (/json)?}/profile")
@Produces({"application/xml", "application/json"})
@Singleton

public class ProfileWebservice {

    @Context
    public HttpServletRequest request;

    private ProfileService profileService;
    private OperatorDAO operatorDAO;

    private final static String JAMON_PROFILE_LOAD = "rest.profile.load";
    private final static String JAMON_PROFILE_STORE = "rest.profile.store";
    private final static String JAMON_PROFILE_DELETE = "rest.profile.delete";
    private final static String JAMON_PROFILE_FIELD_STORE = "rest.profile.field.store";
    private final static String JAMON_PROFILE_FIELD_LOAD = "rest.profile.field.load";
    private final static String JAMON_PROFILE_FIELD_DELETE = "rest.profile.field.delete";

    public ProfileWebservice(ProfileService profileService, OperatorDAO operatorDAO) {
        this.profileService = profileService;
        this.operatorDAO = operatorDAO;
    }


    /**
     * This method stores the given profile to the Item defined by the tenantID,
     * itemID and the itemTypeID. If there is already a Profile it will be overwritten.
     * If the Item does not exist it will be created.
     *
     * @param responseType defines the media type of the result
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the item where the profile will be stored
     * @param itemID       the itemID if the item where the profile will be stored
     * @param itemType     the itemType of the item where the profile will be stored
     * @param profile      the XML profile which will be stored
     * @param callback     if set and responseType is json the result will be returned
     *                     via this javascript callback function (optional)
     * @return a response object containing information about the success of the operation
     */
    @GET
    @Path("/store")
    public Response storeProfile(@PathParam("responseType") String responseType,
                                 @QueryParam("apikey") String apiKey,
                                 @QueryParam("tenantid") String tenantID,
                                 @QueryParam("itemid") String itemID,
                                 @DefaultValue("ITEM") @QueryParam("itemtype") String itemType,
                                 @QueryParam("profile") String profile,
                                 @QueryParam("callback") String callback) {

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
                WS.PROFILE_STORE, responseType, callback);
        mon.stop();
        return response;
    }

    /**
     * This method deletes the profile of the item defined by the tenantID,
     * itemID and the itemTypeID
     *
     * @param responseType defines the media type of the result
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the item whose profile will be deleted
     * @param itemID       the itemID if the item whose profile will be deleted
     * @param itemType     the itemType of the item whose profile will be deleted
     * @param callback     if set and responseType is json the result will be returned
     *                     via this javascript callback function (optional)
     * @return a response object containing information about the success of the operation
     */
    @GET
    @Path("/delete")
    public Response deleteProfile(@PathParam("responseType") String responseType,
                                  @QueryParam("apikey") String apiKey,
                                  @QueryParam("tenantid") String tenantID,
                                  @QueryParam("itemid") String itemID,
                                  @DefaultValue("ITEM") @QueryParam("itemtype") String itemType,
                                  @QueryParam("callback") String callback) {

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
                errorMessages.add(MSG.PROFILE_NOT_DELETED);
        } catch (ItemNotFoundException itemNotFoundException) {
            errorMessages.add(MSG.ITEM_NOT_EXISTS);
        } catch (RuntimeException runtimeException) {
            errorMessages.add(MSG.PROFILE_NOT_DELETED);
        }

        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_DELETE, responseType, callback);
        mon.stop();
        return response;
    }

    /**
     * This method returns the profile of the item defined by the tenantID,
     * itemID and the itemTypeID
     *
     * @param responseType defines the media type of the result
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the item whose profile will be returned
     * @param itemID       the itemID if the item whose profile will be returned
     * @param itemType     the itemType of the item whose profile will be returned
     * @param callback     if set and responseType is json the result will be returned
     *                     via this javascript callback function (optional)
     * @return a response object containing the wanted profile
     */
    @GET
    @Path("/load")
    public Response loadProfile(@PathParam("responseType") String responseType,
                                @QueryParam("apikey") String apiKey,
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
                        responseObject = new ResponseProfile("profile/load", tenantID, itemID, itemType, profile);
                    else
                        errorMessages.add(MSG.PROFILE_NOT_LOADED);
                }
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            if (illegalArgumentException.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
            } else
                errorMessages.add(MSG.PROFILE_NOT_LOADED);
        } catch (ItemNotFoundException itemNotFoundException) {
            errorMessages.add(MSG.ITEM_NOT_EXISTS);
        } catch (RuntimeException runtimeException) {
            errorMessages.add(MSG.PROFILE_NOT_LOADED);
        }

        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_LOAD, responseType, callback);
        mon.stop();
        return response;
    }

    /**
     * This method stores a value into a specific field of the profile which belongs to the item
     * defined by the tenantID, itemID and the itemTypeID. The field can be addressed
     * by a XPath expression. If the field does not exist it will be created.
     *
     * @param responseType defines the media type of the result
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the addressed item
     * @param itemID       the itemID if the addressed item
     * @param itemType     the itemType of the addressed item
     * @param field        an XPath expression pointing to the field
     *                     where the value will be saved
     * @param value        the value which will be saved in the field
     * @param callback     if set and responseType is json the result will be returned
     *                     via this javascript callback function (optional)
     * @return a response object containing information about the success of the operation
     */
    @GET
    @Path("/field/store")
    public Response storeField(@PathParam("responseType") String responseType,
                               @QueryParam("apikey") String apiKey,
                               @QueryParam("tenantid") String tenantID,
                               @QueryParam("itemid") String itemID,
                               @DefaultValue("ITEM") @QueryParam("itemtype") String itemType,
                               @QueryParam("field") String field,
                               @QueryParam("value") String value,
                               @QueryParam("callback") String callback) {

        Monitor mon = MonitorFactory.start(JAMON_PROFILE_FIELD_STORE);

        List<Message> errorMessages = new ArrayList<>();
        List<Message> responseObject = new ArrayList<>();

        try {
            if (checkParameters(apiKey, tenantID, itemID, itemType, errorMessages) &&
                    checkParameterField(field, errorMessages) &&
                    checkParameterValue(value, errorMessages)) {
                Integer coreTenantID = operatorDAO.getTenantId(apiKey, tenantID);
                if (coreTenantID == null)
                    errorMessages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
                else {
                    if (profileService.storeProfileField(
                            coreTenantID, itemID, itemType,
                            field, value))
                        responseObject.add(MSG.PROFILE_FIELD_SAVED);
                    else
                        errorMessages.add(MSG.PROFILE_FIELD_NOT_SAVED);
                }
            }
        } catch (Exception e) {
            if (e instanceof SAXException) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " SAXException: " + e.getMessage()));
            }
            if (e instanceof TransformerException) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " TransformerException: " + e.getMessage()));
            }
            if (e instanceof XPathExpressionException) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " XPathExpressionException: " + e.getMessage()));
            }
            if (e instanceof DOMException) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " DOMException: " + e.getMessage()));
            }
            if (e instanceof MultipleProfileFieldsFoundException) {
                errorMessages.add(MSG.PROFILE_MULTIPLE_FIELDS_WITH_SAME_NAME);
            }
            if (e instanceof IllegalArgumentException) {
                if (e.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
            } else
                errorMessages.add(MSG.PROFILE_FIELD_NOT_SAVED);
            }
            if (e instanceof RuntimeException) {
                errorMessages.add(MSG.PROFILE_FIELD_NOT_SAVED);
            }
        } 
        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_FIELD_STORE, responseType, callback);
        mon.stop();
        return response;
    }

    /**
     * This method deletes a specific field of the profile which belongs to the item
     * defined by the tenantID, itemID and the itemTypeID. The field can be addressed
     * by a XPath expression
     *
     * @param responseType defines the media type of the result
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the addressed item
     * @param itemID       the itemID if the addressed item
     * @param itemType     the itemType of the addressed item
     * @param field        an XPath expression pointing to the field
     *                     which will be deleted
     * @param callback     if set and responseType is json the result will be returned
     *                     via this javascript callback function (optional)
     * @return a response object containing information about the success of the operation
     */
    @GET
    @Path("/field/delete")
    public Response deleteField(@PathParam("responseType") String responseType,
                                @QueryParam("apikey") String apiKey,
                                @QueryParam("tenantid") String tenantID,
                                @QueryParam("itemid") String itemID,
                                @DefaultValue("ITEM") @QueryParam("itemtype") String itemType,
                                @QueryParam("field") String field,
                                @QueryParam("callback") String callback) {

        Monitor mon = MonitorFactory.start(JAMON_PROFILE_FIELD_DELETE);

        List<Message> errorMessages = new ArrayList<>();
        List<Message> responseObject = new ArrayList<>();

        try {
            if (checkParameters(apiKey, tenantID, itemID, itemType, errorMessages) &&
                    checkParameterField(field, errorMessages)) {
                Integer coreTenantID = operatorDAO.getTenantId(apiKey, tenantID);
                if (coreTenantID == null) {
                    errorMessages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
                }
                else {
                    if (profileService.deleteProfileField(coreTenantID, itemID, itemType, field))
                        responseObject.add(MSG.PROFILE_FIELD_DELETED);
                    else
                        errorMessages.add(MSG.PROFILE_FIELD_NOT_DELETED);
                }
            }
        } catch (Exception e) {
            if (e instanceof SAXException) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " SAXException: " + e.getMessage()));
            }
            if (e instanceof TransformerException) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " TransformerException: " + e.getMessage()));
            }
            if (e instanceof XPathExpressionException) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " XPathExpressionException: " + e.getMessage()));
            }
            if (e instanceof DOMException) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " DOMException: " + e.getMessage()));
            }
            if (e instanceof FieldNotFoundException) { 
//TODO: pretty useless Exception for user; first there is no file involved; second user cannt do anything about it, so why bother him/her?
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " FieldNotFoundException: " + e.getMessage()));
            }
            if (e instanceof IllegalArgumentException) {
                if (e.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
                } else
                    errorMessages.add(MSG.PROFILE_FIELD_NOT_DELETED);
            }
            if (e instanceof ItemNotFoundException) {
                errorMessages.add(MSG.ITEM_NOT_EXISTS.append(
                    " ItemNotFoundException: " + e.getMessage()));
            }
            if (e instanceof RuntimeException) {
                errorMessages.add(MSG.PROFILE_FIELD_NOT_DELETED);
            }
        } 
        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_FIELD_DELETE, responseType, callback);
        mon.stop();
        return response;
    }

    /**
     * This method loads the value from a specific field of the profile which belongs to the item
     * defined by the tenantID, itemID and the itemTypeID. The field can be addressed
     * by a XPath expression. If multiple profile fields are found an error message will be returned.
     *
     * @param responseType defines the media type of the result
     * @param apiKey       the apiKey which admits access to the API
     * @param tenantID     the tenantID of the addressed item
     * @param itemID       the itemID if the addressed item
     * @param itemType     the itemType of the addressed item
     * @param field        an XPath expression pointing to the field(s)
     *                     whose value will be returned
     * @param callback     if set and responseType is json the result will be returned
     *                     via this javascript callback function (optional)
     * @return a response object containing the value of the field.
     * @see ResponseProfileField
     */
    @GET
    @Path("/field/load")
    public Response loadField(@PathParam("responseType") String responseType,
                              @QueryParam("apikey") String apiKey,
                              @QueryParam("tenantid") String tenantID,
                              @QueryParam("itemid") String itemID,
                              @DefaultValue("ITEM") @QueryParam("itemtype") String itemType,
                              @QueryParam("field") String field,
                              @QueryParam("callback") String callback) {

        Monitor mon = MonitorFactory.start(JAMON_PROFILE_FIELD_LOAD);

        List<Message> errorMessages = new ArrayList<>();
        Object responseObject = null;

        try {
            if (checkParameters(apiKey, tenantID, itemID, itemType, errorMessages) &&
                    checkParameterField(field, errorMessages)) {
                Integer coreTenantID = operatorDAO.getTenantId(apiKey, tenantID);
                if (coreTenantID == null)
                    errorMessages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
                else {
                    Set<String> values = profileService.loadProfileField(coreTenantID, itemID, itemType, field);
                    if (values != null || values.isEmpty())
                        if (values.size() > 1)
                            errorMessages.add(MSG.PROFILE_MULTIPLE_FIELDS_WITH_SAME_NAME);
                        else
                            responseObject = new ResponseProfileField("profile/field/load",
                                    tenantID, itemID, itemType, field, (String) values.toArray()[0]);
                    else
                        errorMessages.add(MSG.PROFILE_FIELD_NOT_LOADED);
                }
            }
        } catch (Exception e) {
            if (e instanceof SAXException) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " SAXException: " + e.getMessage()));
            }
            if (e instanceof XPathExpressionException) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " XPathExpressionException: " + e.getMessage()));
            }
            if (e instanceof DOMException) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                    " DOMException: " + e.getMessage()));
            }
            if (e instanceof IllegalArgumentException) {
                if (e.getMessage().contains("unknown item type")) {
                errorMessages.add(MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantID)));
                } else
                    errorMessages.add(MSG.PROFILE_FIELD_NOT_DELETED);
            }
            if (e instanceof ItemNotFoundException) {
                errorMessages.add(MSG.ITEM_NOT_EXISTS.append(
                    " ItemNotFoundException: " + e.getMessage()));
            }
            if (e instanceof RuntimeException) {
                errorMessages.add(MSG.PROFILE_FIELD_NOT_DELETED);
            }
            if (e instanceof ParserConfigurationException) {
                //TODO
            }

        }
        Response response = formatResponse(responseObject, errorMessages,
                WS.PROFILE_FIELD_LOAD, responseType, callback);
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
     * @param responseType defines the format of the <code>Response</code> object
     * @param callback     if set and responseType is json the result will be returned
     *                     via this javascript callback function (optional)
     * @return a <code>Response</code> object containing the <code>responseData</code>
     *         in the format defined with <code>responseType</code>
     */
    private Response formatResponse(Object respondData,
                                    List<Message> messages,
                                    String serviceName,
                                    String responseType,
                                    String callback) {

        //handle error messages if existing
        if (messages.size() > 0) {
            if ((WS.RESPONSE_TYPE_PATH_JSON.equals(responseType)))
                throw new EasyRecException(messages, serviceName, WS.RESPONSE_TYPE_JSON, callback);
            else
                throw new EasyRecException(messages, serviceName);
        }

        if (respondData instanceof List) {
            respondData = new ResponseSuccessMessage(serviceName, (List<SuccessMessage>) respondData);
        }

        //convert respondData to Respond object
        if (WS.RESPONSE_TYPE_PATH_JSON.equals(responseType)) {
            if (callback != null) {
                return Response.ok(new JSONPObject(callback, respondData),
                        WS.RESPONSE_TYPE_JSCRIPT).build();
            } else {
                return Response.ok(respondData, WS.RESPONSE_TYPE_JSON).build();
            }
        } else if (WS.RESPONSE_TYPE_PATH_XML.equals(responseType)) {
            return Response.ok(respondData, WS.RESPONSE_TYPE_XML).build();
        } else {
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
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
        if (apiKey == null || apiKey.equals("")) {
            messages.add(MSG.PROFILE_NO_API_KEY);
            result = false;
        }
        if (tenantID == null || tenantID.equals("")) {
            messages.add(MSG.PROFILE_NO_TENANT_ID);
            result = false;
        }
        if (itemID == null || itemID.equals("")) {
            messages.add(MSG.ITEM_NO_ID);
            result = false;
        }
        if (itemType == null || itemType.equals("")) {
            messages.add(MSG.PROFILE_NO_ITEM_TYPE);
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
        if (profile == null) {
            messages.add(MSG.PROFILE_NO_PROFILE_PROVIDED);
            return false;
        } else
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
        if (field == null || field.equals("")) {
            messages.add(MSG.PROFILE_NO_FIELD_PROVIDED);
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
    private boolean checkParameterValue(String value, List<Message> messages) {
        if (value == null) {
            messages.add(MSG.PROFILE_NO_VALUE_PROVIDED);
            return false;
        } else
            return true;
    }
}
