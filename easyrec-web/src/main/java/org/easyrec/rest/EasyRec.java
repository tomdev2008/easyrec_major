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
package org.easyrec.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.common.base.CharMatcher;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.sun.jersey.spi.resource.Singleton;
import org.easyrec.exception.core.ClusterException;
import org.easyrec.model.core.ClusterVO;
import org.easyrec.model.core.transfer.TimeConstraintVO;
import org.easyrec.model.core.web.*;
import org.easyrec.model.plugin.LogEntry;
import org.easyrec.model.plugin.NamedConfiguration;
import org.easyrec.model.plugin.archive.ArchivePseudoConfiguration;
import org.easyrec.model.plugin.archive.ArchivePseudoGenerator;
import org.easyrec.model.plugin.archive.ArchivePseudoStatistics;
import org.easyrec.model.plugin.sessiontousermapping.SessionToUserMappingConfiguration;
import org.easyrec.model.plugin.sessiontousermapping.SessionToUserMappingGenerator;
import org.easyrec.model.web.EasyRecSettings;
import org.easyrec.model.web.Recommendation;
import org.easyrec.plugin.configuration.GeneratorContainer;
import org.easyrec.plugin.container.PluginRegistry;
import org.easyrec.plugin.stats.GeneratorStatistics;
import org.easyrec.rest.nodomain.exception.EasyRecRestException;
import org.easyrec.service.core.ClusterService;
import org.easyrec.service.core.ProfileService;
import org.easyrec.service.core.TenantService;
import org.easyrec.service.domain.TypeMappingService;
import org.easyrec.service.web.RemoteAssocService;
import org.easyrec.service.web.nodomain.ShopRecommenderService;
import org.easyrec.store.dao.IDMappingDAO;
import org.easyrec.store.dao.core.ItemDAO;
import org.easyrec.store.dao.core.types.AssocTypeDAO;
import org.easyrec.store.dao.plugin.LogEntryDAO;
import org.easyrec.store.dao.web.BackTrackingDAO;
import org.easyrec.store.dao.web.OperatorDAO;
import org.easyrec.store.dao.web.RemoteTenantDAO;
import org.easyrec.utils.MyUtils;
import org.easyrec.vocabulary.MSG;
import org.easyrec.vocabulary.WS;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author szavrel
 */
@Path("{type: (1.[0-1](?:/json)?)}")
@Produces({"application/xml", "application/json"})
@Singleton
public class EasyRec {

    @Context
    public HttpServletRequest request;

    private final OperatorDAO operatorDAO;
    private final ItemDAO itemDAO;
    private final RemoteAssocService remoteAssocService;
    private final RemoteTenantDAO remoteTenantDAO;
    private final TenantService tenantService;
    private final ShopRecommenderService shopRecommenderService;
    private final TypeMappingService typeMappingService;
    private final String dateFormat;
    private final IDMappingDAO idMappingDAO;
    //added by FK on 2012-12-18 to enable adding profile data to recommendations
    private final ProfileService profileService;
    private final ClusterService clusterService;
    private final EasyRecSettings easyrecSettings;
    private final PluginRegistry pluginRegistry;
    private final GeneratorContainer generatorContainer;
    private final ObjectMapper objectMapper;
    private final LogEntryDAO logEntryDAO;
    private final BackTrackingDAO backTrackingDAO;


    // Jamon Loggers
    private final static String JAMON_REST_VIEW = "rest.view";
    private final static String JAMON_REST_BUY = "rest.buy";
    private final static String JAMON_REST_RATE = "rest.rate";
    private final static String JAMON_REST_ACTION = "rest.action";
    private final static String JAMON_REST_TRACK = "rest.track";
    private final static String JAMON_REST_ALSO_VIEWED = "rest.alsoviewed";
    private final static String JAMON_REST_ALSO_BOUGHT = "rest.alsobought";
    private final static String JAMON_REST_ALSO_RATED = "rest.alsorated";
    private final static String JAMON_REST_RECS_FOR_USER = "rest.recsforuser";
    private final static String JAMON_REST_MOST_BOUGHT = "rest.mostbought";
    private final static String JAMON_REST_MOST_VIEWED = "rest.mostviewed";
    private final static String JAMON_REST_MOST_RATED = "rest.mostrated";
    private final static String JAMON_REST_BEST_RATED = "rest.bestrated";
    private final static String JAMON_REST_WORST_RATED = "rest.worstrated";
    private final static String JAMON_REST_IMPORT_RULE = "rest.import.rule";
    private final static String JAMON_REST_IMPORT_ITEM = "rest.import.item";
    private final static String JAMON_REST_CREATE_CLUSTER = "rest.create.cluster";
    private final static String JAMON_REST_ITEM_ACTIVE = "rest.item.active.rule";
    private final static String JAMON_REST_RELATED_ITEMS = "rest.related.items";
    private final static String JAMON_REST_ITEMTYPES = "rest.itemtypes";
    private final static String JAMON_REST_ADDITEMTYPE = "rest.additemtype";
    private final static String JAMON_REST_DELETEITEMTYPE = "rest.deleteitemtype";
    private final static String JAMON_REST_CLUSTERS = "rest.clusters";
    private final static String JAMON_REST_ITEMS_OF_CLUSTERS = "rest.items.from.clusters";
    private final static String JAMON_REST_ACTIONHISTORY = "rest.history";
    private final static String JAMON_REST_START_PLUGINS = "rest.start.plugins";

    public EasyRec(OperatorDAO operatorDAO, RemoteTenantDAO remoteTenantDAO,
                   ShopRecommenderService shopRecommenderService, TenantService tenantService,
                   TypeMappingService typeMappingService, ItemDAO itemDAO, RemoteAssocService remoteAssocService,
                   IDMappingDAO idMappingDAO,
                   //added by FK on 2012-12-18 for enabling profile data in recommendations
                   ProfileService profileService,
                   ClusterService clusterService,
                   EasyRecSettings easyrecSettings,
                   PluginRegistry pluginRegistry,
                   GeneratorContainer generatorContainer,
                   String dateFormatString,
                   ObjectMapper objectMapper,
                   LogEntryDAO logEntryDAO,
                   BackTrackingDAO backTrackingDAO) {
        this.operatorDAO = operatorDAO;
        this.remoteTenantDAO = remoteTenantDAO;
        this.shopRecommenderService = shopRecommenderService;
        this.tenantService = tenantService;
        this.typeMappingService = typeMappingService;
        this.itemDAO = itemDAO;
        this.remoteAssocService = remoteAssocService;
        this.dateFormat = dateFormatString;
        this.profileService = profileService;
        this.idMappingDAO = idMappingDAO;
        this.clusterService = clusterService;
        this.easyrecSettings = easyrecSettings;
        this.pluginRegistry = pluginRegistry;
        this.generatorContainer = generatorContainer;
        this.objectMapper = objectMapper;
        this.logEntryDAO = logEntryDAO;
        this.backTrackingDAO = backTrackingDAO;
    }

    @GET
    @Path("/view")
    public Response view(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                         @QueryParam("tenantid") String tenantId, @QueryParam("userid") String userId,
                         @QueryParam("sessionid") String sessionId, @QueryParam("itemid") String itemId,
                         @QueryParam("itemdescription") String itemDescription, @QueryParam("itemurl") String itemUrl,
                         @QueryParam("itemimageurl") String itemImageUrl, @QueryParam("actiontime") String actionTime,
                         @QueryParam("itemtype") String itemType, @QueryParam("callback") String callback,
                         @QueryParam("token") String token, @QueryParam("actioninfo") String actionInfo)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_VIEW);

        if (easyrecSettings.getSecuredAPIMethods().contains("view")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_VIEW, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        // Collect a List of messages for the user to understand,
        // what went wrong (e.g. Wrong API key).
        List<Message> messages = new ArrayList<>();

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        //        if (r.isMaxActionLimitExceeded()) {
        //            messages.add(Message.MAXIMUM_ACTIONS_EXCEEDED);
        //        }

        checkParams(coreTenantId, itemId, itemDescription, itemUrl, sessionId, actionInfo, messages);
        Date actionDate = null;

        if (actionTime != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
            actionDate = MyUtils.dateFormatCheck(actionTime, dateFormatter);

            if (actionDate == null)
                messages.add(MSG.DATE_PARSE);
        }

        if (messages.size() > 0) {
            if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON))
                throw new EasyRecException(messages, WS.ACTION_VIEW, WS.RESPONSE_TYPE_JSON, callback);
            else
                throw new EasyRecException(messages, WS.ACTION_VIEW);
        }

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);
        itemType = checkItemType(itemType, type, coreTenantId, tenantId, WS.ACTION_VIEW, callback);
        Session session = new Session(sessionId, request.getRemoteAddr());
        Item item = shopRecommenderService.viewItem(r, userId, itemId, itemType, itemDescription,
                itemUrl, itemImageUrl, actionDate, session, actionInfo);

        ResponseItem respItem = new ResponseItem(tenantId, WS.ACTION_VIEW, userId, sessionId, null, item);
        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, respItem), WS.RESPONSE_TYPE_JSCRIPT)
                        .build();
            else
                return Response.ok(respItem, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(respItem, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/rate")
    public Response rate(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                         @QueryParam("tenantid") String tenantId, @QueryParam("userid") String userId,
                         @QueryParam("sessionid") String sessionId, @QueryParam("itemid") String itemId,
                         @QueryParam("ratingvalue") String ratingValue,
                         @QueryParam("itemdescription") String itemDescription, @QueryParam("itemurl") String itemUrl,
                         @QueryParam("itemimageurl") String itemImageUrl, @QueryParam("actiontime") String actionTime,
                         @QueryParam("itemtype") String itemType, @QueryParam("callback") String callback,
                         @QueryParam("token") String token, @QueryParam("actioninfo") String actionInfo)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_RATE);

        if (easyrecSettings.getSecuredAPIMethods().contains("rate")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_RATE, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        // Collect a List of messages for the user to understand,
        // what went wrong (e.g. Wrong API key).
        List<Message> messages = new ArrayList<>();

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);



        checkParams(coreTenantId, itemId, itemDescription, itemUrl, sessionId, actionInfo, messages);

        Date actionDate = null;

        if (actionTime != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
            actionDate = MyUtils.dateFormatCheck(actionTime, dateFormatter);

            if (actionDate == null)
                messages.add(MSG.DATE_PARSE);
        }

        Integer rateValue = -1;
        try {
            rateValue = Integer.valueOf(ratingValue);

            if (coreTenantId != null && (rateValue < tenantService.getTenantById(coreTenantId).getRatingRangeMin() ||
                    rateValue > tenantService.getTenantById(coreTenantId).getRatingRangeMax()))
                throw new Exception();
        } catch (Exception e) {
            messages.add(MSG.ITEM_INVALID_RATING_VALUE);
        }

        if (messages.size() > 0) {
            if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON))
                throw new EasyRecException(messages, WS.ACTION_RATE, WS.RESPONSE_TYPE_JSON, callback);
            else
                throw new EasyRecException(messages, WS.ACTION_RATE);
        }

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);
        //        if (r.isMaxActionLimitExceeded()) {
        //            messages.add(Message.MAXIMUM_ACTIONS_EXCEEDED);
        //        }

        itemType = checkItemType(itemType, type, coreTenantId, tenantId, WS.ACTION_RATE, callback);
        Session session = new Session(sessionId, request.getRemoteAddr());

        Item item = shopRecommenderService.rateItem(r, userId, itemId, itemType, itemDescription,
                itemUrl, itemImageUrl, rateValue, actionDate, session, actionInfo);

        ResponseItem respItem = new ResponseItem(tenantId, WS.ACTION_RATE, userId, sessionId, ratingValue, item);

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, respItem), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(respItem, WS.RESPONSE_TYPE_JSON).build();
        } else {
            return Response.ok(respItem, WS.RESPONSE_TYPE_XML).build();
        }
    }

    @GET
    @Path("/buy")
    public Response buy(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                        @QueryParam("tenantid") String tenantId, @QueryParam("userid") String userId,
                        @QueryParam("sessionid") String sessionId, @QueryParam("itemid") String itemId,
                        @QueryParam("itemdescription") String itemDescription, @QueryParam("itemurl") String itemUrl,
                        @QueryParam("itemimageurl") String itemImageUrl, @QueryParam("actiontime") String actionTime,
                        @QueryParam("itemtype") String itemType, @QueryParam("callback") String callback,
                        @QueryParam("token") String token, @QueryParam("actioninfo") String actionInfo)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_BUY);

        if (easyrecSettings.getSecuredAPIMethods().contains("buy")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_BUY, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        // Collect a List of messages for the user to understand,
        // what went wrong (e.g. Wrong API key).
        List<Message> messages = new ArrayList<>();

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        checkParams(coreTenantId, itemId, itemDescription, itemUrl, sessionId, actionInfo, messages);

        Date actionDate = null;

        if (actionTime != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
            actionDate = MyUtils.dateFormatCheck(actionTime, dateFormatter);

            if (actionDate == null)
                messages.add(MSG.DATE_PARSE);
        }

        if (messages.size() > 0) {
            if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON))
                throw new EasyRecException(messages, WS.ACTION_BUY, WS.RESPONSE_TYPE_JSON, callback);
            else
                throw new EasyRecException(messages, WS.ACTION_BUY);
        }

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);
        //        if (r.isMaxActionLimitExceeded()) {
        //            messages.add(Message.MAXIMUM_ACTIONS_EXCEEDED);
        //        }

        itemType = checkItemType(itemType, type, coreTenantId, tenantId, WS.ACTION_BUY, callback);
        Session session = new Session(sessionId, request.getRemoteAddr());

        Item item = shopRecommenderService.purchaseItem(r, userId, itemId, itemType, itemDescription,
                itemUrl, itemImageUrl, actionDate, session, actionInfo);

        ResponseItem respItem = new ResponseItem(tenantId, WS.ACTION_BUY, userId, sessionId, null, item);

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, respItem), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(respItem, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(respItem, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/sendaction")
    public Response sendAction(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                               @QueryParam("tenantid") String tenantId, @QueryParam("userid") String userId,
                               @QueryParam("sessionid") String sessionId, @QueryParam("itemid") String itemId,
                               @QueryParam("actiontype") String actionType, @QueryParam("actionvalue") String actionValue,
                               @QueryParam("itemdescription") String itemDescription, @QueryParam("itemurl") String itemUrl,
                               @QueryParam("itemimageurl") String itemImageUrl, @QueryParam("actiontime") String actionTime,
                               @QueryParam("itemtype") String itemType, @QueryParam("callback") String callback,
                               @QueryParam("token") String token, @QueryParam("actioninfo") String actionInfo)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_ACTION);

        if (easyrecSettings.getSecuredAPIMethods().contains("sendaction")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_SENDACTION, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        // Collect a List of messages for the user to understand,
        // what went wrong (e.g. Wrong API key).
        List<Message> messages = new ArrayList<>();

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        checkParams(coreTenantId, itemId, itemDescription, itemUrl, sessionId, actionInfo, messages);
        Integer actValue = -1;
        if (Strings.isNullOrEmpty(actionType)) {
            messages.add(MSG.MISSING_ACTIONTYPE);
        } else {
            Boolean hasValue = tenantService.hasActionValue(coreTenantId, actionType);
            if (hasValue == null) {
                messages.add(MSG.INVALID_ACTIONTYPE);
            } else {
                if (hasValue) {
                    if (Strings.isNullOrEmpty(actionValue)) {
                        messages.add(MSG.MISSING_ACTION_VALUE);
                    } else {
                        try {
                            actValue = Integer.valueOf(actionValue);
                        } catch (Exception e) {
                            messages.add(MSG.ITEM_INVALID_RATING_VALUE);
                        }
                    }
                }
            }
        }

        Date actionDate = null;

        if (actionTime != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
            actionDate = MyUtils.dateFormatCheck(actionTime, dateFormatter);

            if (actionDate == null)
                messages.add(MSG.DATE_PARSE);
        }

        if (messages.size() > 0) {
            if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON))
                throw new EasyRecException(messages, WS.ACTION_SENDACTION, WS.RESPONSE_TYPE_JSON, callback);
            else
                throw new EasyRecException(messages, WS.ACTION_SENDACTION);
        }

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);
        //        if (r.isMaxActionLimitExceeded()) {
        //            messages.add(Message.MAXIMUM_ACTIONS_EXCEEDED);
        //        }

        itemType = checkItemType(itemType, type, coreTenantId, tenantId, WS.ACTION_RATE, callback);
        Session session = new Session(sessionId, request.getRemoteAddr());

        Item item = shopRecommenderService.sendAction(r, userId, itemId, itemType, itemDescription,
                itemUrl, itemImageUrl, actionType, actValue, actionDate, session, actionInfo);

        ResponseItem respItem = new ResponseItem(tenantId, WS.ACTION_SENDACTION, userId, sessionId, actionValue, item);

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, respItem), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(respItem, WS.RESPONSE_TYPE_JSON).build();
        } else {
            return Response.ok(respItem, WS.RESPONSE_TYPE_XML).build();
        }
    }

    @GET
    @Path("/track")
    public Response track(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                         @QueryParam("tenantid") String tenantId, @QueryParam("userid") String userId,
                         @QueryParam("sessionid") String sessionId, @QueryParam("itemfromid") String itemFromId,
                         @QueryParam("itemfromtype") String itemFromType, @QueryParam("itemtoid") String itemToId,
                         @QueryParam("itemtotype") String itemToType, @QueryParam("rectype") String recType,
                         @QueryParam("callback") String callback, @QueryParam("token") String token)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_TRACK);

        if (easyrecSettings.getSecuredAPIMethods().contains("track")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_TRACK, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        // Collect a List of messages for the user to understand,
        // what went wrong (e.g. Wrong API key).
        List<Message> messages = new ArrayList<>();

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            messages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
        if (Strings.isNullOrEmpty(sessionId))
            messages.add(MSG.USER_NO_SESSION_ID);
        if (itemFromId != null && itemFromId.equals(itemToId))
            messages.add(MSG.ITEMFROM_EQUAL_ITEMTO);
        
        RemoteTenant r = remoteTenantDAO.get(coreTenantId);
        if (Strings.isNullOrEmpty(itemFromType)) itemFromType = Item.DEFAULT_STRING_ITEM_TYPE;
        if (Strings.isNullOrEmpty(itemToType)) itemToType = Item.DEFAULT_STRING_ITEM_TYPE;
        int itemFromTypeId = checkItemType(itemFromType, coreTenantId, tenantId, messages);
        int itemToTypeId = checkItemType(itemToType, coreTenantId, tenantId, messages);
        Item itemTo = null;
        Integer recTypeId = 0;
        if (itemFromTypeId != 0) {
            
            if (!Strings.isNullOrEmpty(itemFromId)) {
                Item itemFrom = itemDAO.get(r, itemFromId, itemFromType);
                if (itemFrom == null)
                    messages.add(MSG.ITEM_FROM_ID_DOES_NOT_EXIST);
            }

            if (itemToTypeId != 0) {
                itemTo = itemDAO.get(r, itemToId, itemToType);

                if (itemTo == null)
                    messages.add(MSG.ITEM_TO_ID_DOES_NOT_EXIST);

                if (recType == null)
                    messages.add(MSG.REC_TYPE_NEEDED);
                else {
                    recTypeId = WS.recTypes.get(recType);
                    if (recTypeId == null) {                   
                        try {
                            recTypeId = typeMappingService.getIdOfAssocType(coreTenantId, recType);
                        } catch (Exception e) {
                            messages.add(MSG.ASSOC_TYPE_DOES_NOT_EXIST);
                        }
                        if (recTypeId == null) {
                            messages.add(MSG.REC_TYPE_NEEDED);
                        }
                    }
                }
            }
        }

        if (messages.size() > 0) {
            if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON))
                throw new EasyRecException(messages, WS.ACTION_TRACK, WS.RESPONSE_TYPE_JSON, callback);
            else
                throw new EasyRecException(messages, WS.ACTION_TRACK);
        }

        // if userid is empty use sessionid instead of the userid
        userId = Strings.isNullOrEmpty(userId) ? sessionId: userId;
        
        backTrackingDAO.track(idMappingDAO.lookup(userId), coreTenantId, Strings.isNullOrEmpty(itemFromId) ? 0: idMappingDAO.lookup(itemFromId), itemFromTypeId, idMappingDAO.lookup(itemToId), itemToTypeId, recTypeId);

        ResponseItem respItem = new ResponseItem(tenantId, WS.ACTION_TRACK, userId, sessionId, null, itemTo);
        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, respItem), WS.RESPONSE_TYPE_JSCRIPT)
                        .build();
            else
                return Response.ok(respItem, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(respItem, WS.RESPONSE_TYPE_XML).build();
    }
    

    @GET
    @Path("/otherusersalsoviewed")
    public Response otherUsersAlsoViewed(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                         @QueryParam("tenantid") String tenantId, @QueryParam("userid") String userId,
                                         @QueryParam("sessionid") String sessionId, @QueryParam("itemid") String itemId,
                                         @QueryParam("numberOfResults") Integer numberOfResults,
                                         @QueryParam("offset") @DefaultValue("0") Integer offset,
                                         @QueryParam("itemtype") String itemType,
                                         @QueryParam("requesteditemtype") String requestedItemType,
                                         @QueryParam("callback") String callback,
                                         @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                         @QueryParam("token") String token) throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_ALSO_VIEWED);

        if (easyrecSettings.getSecuredAPIMethods().contains("otherusersalsoviewed")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_OTHER_USERS_ALSO_VIEWED, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Recommendation rec = null;
        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_OTHER_USERS_ALSO_VIEWED, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);

        if (r.isMaxActionLimitExceeded())
            exceptionResponse(WS.ACTION_OTHER_USERS_ALSO_VIEWED, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

        itemType = checkItemType(itemType, type, coreTenantId, tenantId, WS.ACTION_OTHER_USERS_ALSO_VIEWED, callback);
        requestedItemType = checkItemType(requestedItemType, type, coreTenantId, tenantId, WS.ACTION_OTHER_USERS_ALSO_VIEWED, callback, null);
        Session session = new Session(sessionId, request);

        try {
            if ((numberOfResults == null) || (numberOfResults > WS.DEFAULT_NUMBER_OF_RESULTS))
                numberOfResults = WS.DEFAULT_NUMBER_OF_RESULTS;

            rec = shopRecommenderService.alsoViewedItems(coreTenantId, userId, itemId, itemType, requestedItemType,
                    session, numberOfResults, offset);
            //added by FK on 2012-12-18 for adding profile data to recommendations.
            if (withProfile) {
                addProfileDataToItems(rec);
            }
        } catch (EasyRecRestException sre) {
            exceptionResponse(WS.ACTION_OTHER_USERS_ALSO_VIEWED, sre.getMessageObject(), type,
                    callback);
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, rec), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(rec, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(rec, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/recommendationsforuser")
    public Response recommendationsForUser(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                           @QueryParam("tenantid") String tenantId, @QueryParam("userid") String userId,
                                           @QueryParam("numberOfResults") Integer numberOfResults,
                                           @QueryParam("offset") @DefaultValue("0") Integer offset,
                                           @QueryParam("requesteditemtype") String requestedItemType,
                                           @QueryParam("callback") String callback,
                                           @QueryParam("actiontype") @DefaultValue(TypeMappingService.ACTION_TYPE_VIEW) String actiontype,
                                           @QueryParam("assoctype") String associationType,
                                           @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                           @QueryParam("token") String token)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_RECS_FOR_USER);

        if (easyrecSettings.getSecuredAPIMethods().contains("recommendationsforuser")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_RECOMMENDATIONS_FOR_USER, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Recommendation rec = null;
        Session session = new Session(null, request);

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_RECOMMENDATIONS_FOR_USER, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        RemoteTenant remoteTenant = remoteTenantDAO.get(coreTenantId);

        if (remoteTenant.isMaxActionLimitExceeded())
            exceptionResponse(WS.ACTION_RECOMMENDATIONS_FOR_USER, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

        if (Strings.isNullOrEmpty(userId))
            exceptionResponse(WS.ACTION_RECOMMENDATIONS_FOR_USER, MSG.USER_NO_ID, type, callback);

        requestedItemType = checkItemType(requestedItemType, type, coreTenantId, tenantId, WS.ACTION_RECOMMENDATIONS_FOR_USER, callback, Item.DEFAULT_STRING_ITEM_TYPE);


        if (typeMappingService.getIdOfActionType(coreTenantId, actiontype) == null) {
            exceptionResponse(WS.ACTION_RECOMMENDATIONS_FOR_USER, MSG.OPERATION_FAILED.append(String.format(" actionType %s not found for tenant %s", actiontype, tenantId)), type, callback);
        }

        if (associationType != null)
            try {
                typeMappingService.getIdOfAssocType(coreTenantId, associationType);
            } catch (IllegalArgumentException e) {
                exceptionResponse(
                        WS.ACTION_RECOMMENDATIONS_FOR_USER,
                        MSG.OPERATION_FAILED.append(String.format(" associationType %s not found for tenant %s", associationType, tenantId)),
                        type,
                        callback);
            }

        if ((numberOfResults == null) || (numberOfResults > WS.DEFAULT_NUMBER_OF_RESULTS))
            numberOfResults = WS.DEFAULT_NUMBER_OF_RESULTS;

        if (rec == null || rec.getRecommendedItems().isEmpty()) {
            try {
                rec = shopRecommenderService.itemsBasedOnActionHistory(coreTenantId, userId, session, actiontype, null, WS.ACTION_HISTORY_DEPTH, associationType,
                        requestedItemType, numberOfResults, offset);
                //added by FK on 2012-12-18 for adding profile data to recommendations.
                if (withProfile) {
                    addProfileDataToItems(rec);
                }
            } catch (EasyRecRestException sre) {
                exceptionResponse(WS.ACTION_RECOMMENDATIONS_FOR_USER, sre.getMessageObject(), type, callback);
            }
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, rec), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(rec, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(rec, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/actionhistoryforuser")
    public Response actionHistoryForUser(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                         @QueryParam("tenantid") String tenantId, @QueryParam("userid") String userId,
                                         @QueryParam("numberOfResults") Integer numberOfResults,
                                         @QueryParam("offset") @DefaultValue("0") Integer offset,
                                         @QueryParam("requesteditemtype") String requestedItemType,
                                         @QueryParam("callback") String callback,
                                         @QueryParam("actiontype") @DefaultValue(TypeMappingService.ACTION_TYPE_VIEW) String actiontype,
                                         @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                         @QueryParam("token") String token)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_ACTIONHISTORY);

        if (easyrecSettings.getSecuredAPIMethods().contains("actionhistoryforuser")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_HISTORY, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Recommendation rec = null;
        Session session = new Session(null, request);

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_HISTORY, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        RemoteTenant remoteTenant = remoteTenantDAO.get(coreTenantId);

        if (remoteTenant.isMaxActionLimitExceeded())
            exceptionResponse(WS.ACTION_HISTORY, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

        if (Strings.isNullOrEmpty(userId))
            exceptionResponse(WS.ACTION_HISTORY, MSG.USER_NO_ID, type, callback);

        requestedItemType = checkItemType(requestedItemType, type, coreTenantId, tenantId, WS.ACTION_HISTORY, callback, null);


        if (typeMappingService.getIdOfActionType(coreTenantId, actiontype) == null) {
            exceptionResponse(WS.ACTION_HISTORY, MSG.OPERATION_FAILED.append(String.format(" actionType %s not found for tenant %s", actiontype, tenantId)), type, callback);
        }

        if ((numberOfResults == null) || (numberOfResults > WS.DEFAULT_NUMBER_OF_RESULTS))
            numberOfResults = WS.DEFAULT_NUMBER_OF_RESULTS;

        if (rec == null || rec.getRecommendedItems().isEmpty()) {
            try {
                rec = shopRecommenderService.actionHistory(coreTenantId, userId, session, actiontype,
                                                           requestedItemType, numberOfResults + 5, numberOfResults,
                                                           offset); // +5 to compensate for inactive items

                if (withProfile) {
                    addProfileDataToItems(rec);
                }
            } catch (EasyRecRestException sre) {
                exceptionResponse(WS.ACTION_HISTORY, sre.getMessageObject(), type, callback);
            }
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, rec), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(rec, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(rec, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/otherusersalsobought")
    public Response otherUsersAlsoBought(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                         @QueryParam("tenantid") String tenantId, @QueryParam("userid") String userId,
                                         @QueryParam("sessionid") String sessionId, @QueryParam("itemid") String itemId,
                                         @QueryParam("numberOfResults") Integer numberOfResults,
                                         @QueryParam("offset") @DefaultValue("0") Integer offset,
                                         @QueryParam("itemtype") String itemType,
                                         @QueryParam("requesteditemtype") String requestedItemType,
                                         @QueryParam("callback") String callback,
                                         @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                         @QueryParam("token") String token)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_ALSO_BOUGHT);

        if (easyrecSettings.getSecuredAPIMethods().contains("otherusersalsobought")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_OTHER_USERS_ALSO_BOUGHT, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Recommendation rec = null;
        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_OTHER_USERS_ALSO_BOUGHT, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);

        if (r.isMaxActionLimitExceeded())
            exceptionResponse(WS.ACTION_OTHER_USERS_ALSO_BOUGHT, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

        itemType = checkItemType(itemType, type, coreTenantId, tenantId, WS.ACTION_OTHER_USERS_ALSO_BOUGHT, callback);
        requestedItemType = checkItemType(requestedItemType, type, coreTenantId, tenantId, WS.ACTION_OTHER_USERS_ALSO_BOUGHT, callback, null);
        Session session = new Session(sessionId, request);

        try {
            if ((numberOfResults == null) || (numberOfResults > WS.DEFAULT_NUMBER_OF_RESULTS))
                numberOfResults = WS.DEFAULT_NUMBER_OF_RESULTS;

            rec = shopRecommenderService.alsoBoughtItems(coreTenantId, userId, itemId, itemType, requestedItemType,
                    session, numberOfResults, offset);
            //added by FK on 2012-12-18 for adding profile data to recommendations.
            if (withProfile) {
                addProfileDataToItems(rec);
            }
        } catch (EasyRecRestException sre) {
            exceptionResponse(WS.ACTION_OTHER_USERS_ALSO_BOUGHT, sre.getMessageObject(), type, callback);
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, rec), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(rec, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(rec, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/itemsratedgoodbyotherusers")
    public Response itemsRatedGoodByOtherUsers(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                               @QueryParam("tenantid") String tenantId,
                                               @QueryParam("userid") String userId,
                                               @QueryParam("sessionid") String sessionId,
                                               @QueryParam("itemid") String itemId,
                                               @QueryParam("numberOfResults") Integer numberOfResults,
                                               @QueryParam("offset") @DefaultValue("0") Integer offset,
                                               @QueryParam("itemtype") String itemType,
                                               @QueryParam("requesteditemtype") String requestedItemType,
                                               @QueryParam("callback") String callback,
                                               @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                               @QueryParam("token") String token) throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_ALSO_RATED);

        if (easyrecSettings.getSecuredAPIMethods().contains("itemsratedgoodbyotherusers")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_ITEMS_RATED_GOOD_BY_OTHER_USERS, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Recommendation rec = null;
        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_ITEMS_RATED_GOOD_BY_OTHER_USERS, MSG.TENANT_WRONG_TENANT_APIKEY, type,
                    callback);

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);

        if (r.isMaxActionLimitExceeded())
            exceptionResponse(WS.ACTION_ITEMS_RATED_GOOD_BY_OTHER_USERS, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

        itemType = checkItemType(itemType, type, coreTenantId, tenantId, WS.ACTION_ITEMS_RATED_GOOD_BY_OTHER_USERS, callback);
        requestedItemType = checkItemType(requestedItemType, type, coreTenantId, tenantId, WS.ACTION_ITEMS_RATED_GOOD_BY_OTHER_USERS, callback, null);
        Session session = new Session(sessionId, request);

        try {
            if ((numberOfResults == null) || (numberOfResults > WS.DEFAULT_NUMBER_OF_RESULTS))
                numberOfResults = WS.DEFAULT_NUMBER_OF_RESULTS;

            rec = shopRecommenderService.alsoGoodRatedItems(coreTenantId, userId, itemId, itemType, requestedItemType,
                    session, numberOfResults, offset);
            //added by FK on 2012-12-18 for adding profile data to recommendations.
            if (withProfile) {
                addProfileDataToItems(rec);
            }
        } catch (EasyRecRestException sre) {
            exceptionResponse(WS.ACTION_ITEMS_RATED_GOOD_BY_OTHER_USERS, sre.getMessageObject(),
                    type, callback);
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, rec), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(rec, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(rec, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/mostboughtitems")
    public Response mostBoughtItems(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                    @QueryParam("tenantid") String tenantId,
                                    @QueryParam("numberOfResults") Integer numberOfResults,
                                    @QueryParam("offset") @DefaultValue("0") Integer offset,
                                    @QueryParam("timeRange") String timeRange,
                                    @QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate,
                                    @QueryParam("requesteditemtype") String requesteditemtype,
                                    @QueryParam("clusterid") String clusterId,
                                    @QueryParam("callback") String callback,
                                    @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                    @QueryParam("token") String token) throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_MOST_BOUGHT);

        if (easyrecSettings.getSecuredAPIMethods().contains("mostboughtitems")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_MOST_BOUGHT, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Recommendation rr = null;
        Integer cluster = null;
        if (clusterId != null) {
            cluster = this.idMappingDAO.lookup(clusterId);
        }
        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_MOST_BOUGHT, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);

        if (r.isMaxActionLimitExceeded())
            exceptionResponse(WS.ACTION_MOST_BOUGHT, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

        TimeConstraintVO tc = checkTimeConstraints(startDate, endDate);

        if (tc == null)
            exceptionResponse(WS.ACTION_MOST_BOUGHT, MSG.DATE_PARSE, type, callback);

        requesteditemtype = checkItemType(requesteditemtype, type, coreTenantId, tenantId, WS.ACTION_MOST_BOUGHT, callback, null);
        List<Item> items;

        if (tc != null) {
            items = shopRecommenderService.mostBoughtItems(coreTenantId, requesteditemtype,
                    cluster, numberOfResults != null ? numberOfResults : WS.DEFAULT_NUMBER_OF_RESULTS, offset,
                                                           timeRange, tc,
                    new Session(null, request));

            rr = new Recommendation(tenantId, WS.ACTION_MOST_BOUGHT, null, null, null, items);
            //added by FK on 2012-12-18 for adding profile data to recommendations.
            if (withProfile) {
                addProfileDataToItems(rr);
            }
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, rr), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(rr, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(rr, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/mostvieweditems")
    public Response mostViewedItems(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                    @QueryParam("tenantid") String tenantId,
                                    @QueryParam("numberOfResults") Integer numberOfResults,
                                    @QueryParam("offset") @DefaultValue("0") Integer offset,
                                    @QueryParam("timeRange") String timeRange,
                                    @QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate,
                                    @QueryParam("requesteditemtype") String requestedItemType,
                                    @QueryParam("clusterid") String clusterId,
                                    @QueryParam("callback") String callback,
                                    @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                    @QueryParam("token") String token)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_MOST_VIEWED);

        if (easyrecSettings.getSecuredAPIMethods().contains("mostvieweditems")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_MOST_VIEWED, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Recommendation rr = null;
        Integer cluster = null;
        if (clusterId != null) {
            cluster = this.idMappingDAO.lookup(clusterId);
        }
        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);


        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_MOST_VIEWED, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);

        if (r.isMaxActionLimitExceeded())
            exceptionResponse(WS.ACTION_MOST_VIEWED, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

        TimeConstraintVO tc = checkTimeConstraints(startDate, endDate);

        if (tc == null)
            exceptionResponse(WS.ACTION_MOST_VIEWED, MSG.DATE_PARSE, type, callback);

        requestedItemType = checkItemType(requestedItemType, type, coreTenantId, tenantId, WS.ACTION_MOST_VIEWED, callback, null);
        List<Item> items;

        if (tc != null) {
            items = shopRecommenderService.mostViewedItems(coreTenantId, requestedItemType,
                    cluster, numberOfResults != null ? numberOfResults : WS.DEFAULT_NUMBER_OF_RESULTS, offset,
                                                           timeRange,
                                                           tc,
                    new Session(null, request));

            rr = new Recommendation(tenantId, WS.ACTION_MOST_VIEWED, null, null, null, items);
            //added by FK on 2012-12-18 for adding profile data to recommendations.
            if (withProfile) {
                addProfileDataToItems(rr);
            }
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, rr), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(rr, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(rr, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/mostrateditems")
    public Response mostRatedItems(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                   @QueryParam("tenantid") String tenantId,
                                   @QueryParam("numberOfResults") Integer numberOfResults,
                                   @QueryParam("offset") @DefaultValue("0") Integer offset,
                                   @QueryParam("timeRange") String timeRange, @QueryParam("startDate") String startDate,
                                   @QueryParam("endDate") String endDate,
                                   @QueryParam("requesteditemtype") String requestedItemType,
                                   @QueryParam("clusterid") String clusterId,
                                   @QueryParam("callback") String callback,
                                   @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                   @QueryParam("token") String token) throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_MOST_RATED);

        if (easyrecSettings.getSecuredAPIMethods().contains("mostrateditems")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_MOST_RATED, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Recommendation rr = null;
        Integer cluster = null;
        if (clusterId != null) {
            cluster = this.idMappingDAO.lookup(clusterId);
        }
        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_MOST_RATED, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);

        if (r.isMaxActionLimitExceeded())
            exceptionResponse(WS.ACTION_MOST_RATED, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

        TimeConstraintVO tc = checkTimeConstraints(startDate, endDate);

        if (tc == null)
            exceptionResponse(WS.ACTION_MOST_RATED, MSG.DATE_PARSE, type, callback);

        requestedItemType = checkItemType(requestedItemType, type, coreTenantId, tenantId, WS.ACTION_MOST_RATED, callback, null);
        List<Item> items;

        if (tc != null) {
            items = shopRecommenderService.mostRatedItems(coreTenantId, requestedItemType,
                    cluster, numberOfResults != null ? numberOfResults : WS.DEFAULT_NUMBER_OF_RESULTS, offset,
                    timeRange, tc, new Session(null, request));

            rr = new Recommendation(tenantId, WS.ACTION_MOST_RATED, null, null, null, items);
            //added by FK on 2012-12-18 for adding profile data to recommendations.
            if (withProfile) {
                addProfileDataToItems(rr);
            }
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, rr), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(rr, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(rr, WS.RESPONSE_TYPE_XML).build();
    }


    @GET
    @Path("/itemsofcluster")
    public Response getItemsOfCluster(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                      @QueryParam("tenantid") String tenantId,
                                      @QueryParam("clusterid") String clusterId,
                                      @QueryParam("numberOfResults") Integer numberOfResults,
                                      @QueryParam("offset") @DefaultValue("0") Integer offset,
                                      @QueryParam("strategy") String strategy,
                                      @QueryParam("usefallback") @DefaultValue("false") Boolean useFallback,
                                      @QueryParam("requesteditemtype") String requestedItemType,
                                      @QueryParam("callback") String callback,
                                      @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                      @QueryParam("token") String token) {

        Monitor monitor = MonitorFactory.start(JAMON_REST_ITEMS_OF_CLUSTERS);

        if (easyrecSettings.getSecuredAPIMethods().contains("itemsofcluster")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_ITEMS_OF_CLUSTER, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Recommendation recommendation = null;
        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_ITEMS_OF_CLUSTER, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        RemoteTenant remoteTenant = remoteTenantDAO.get(coreTenantId);

        if (remoteTenant.isMaxActionLimitExceeded())
            exceptionResponse(WS.ACTION_ITEMS_OF_CLUSTER, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

        if (clusterId == null)
            exceptionResponse(WS.ACTION_ITEMS_OF_CLUSTER, MSG.CLUSTER_NO_ID, type, callback);

        requestedItemType = checkItemType(requestedItemType, type, coreTenantId, tenantId, WS.ACTION_ITEMS_OF_CLUSTER, callback, null);
        List<Item> items;

        if (clusterId != null)
            try {
                Integer coreItemType = typeMappingService.getIdOfItemType(coreTenantId, requestedItemType);

                items = shopRecommenderService.itemsOfCluster(coreTenantId, clusterId,
                        numberOfResults != null ? numberOfResults : WS.DEFAULT_NUMBER_OF_RESULTS, offset, strategy,
                                                              useFallback,
                        coreItemType, new Session(null, request));

                recommendation = new Recommendation(tenantId, WS.ACTION_ITEMS_OF_CLUSTER, null, null, null, items);
                //added by FK on 2012-12-18 for adding profile data to recommendations.
                if (withProfile) {
                    addProfileDataToItems(recommendation);
                }
            } catch (EasyRecRestException sre) {
                exceptionResponse(WS.ACTION_ITEMS_OF_CLUSTER, sre.getMessageObject(), type,
                        callback);
            }

        monitor.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, recommendation), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(recommendation, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(recommendation, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/bestrateditems")
    public Response bestRatedItems(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                   @QueryParam("tenantid") String tenantId, @QueryParam("userid") String userId,
                                   @QueryParam("numberOfResults") Integer numberOfResults,
                                   @QueryParam("offset") @DefaultValue("0") Integer offset,
                                   @QueryParam("timeRange") String timeRange, @QueryParam("startDate") String startDate,
                                   @QueryParam("endDate") String endDate,
                                   @QueryParam("requesteditemtype") String requestedItemType,
                                   @QueryParam("callback") String callback,
                                   @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                   @QueryParam("token") String token)
            throws EasyRecException {
            Monitor mon = MonitorFactory.start(JAMON_REST_BEST_RATED);

            if (easyrecSettings.getSecuredAPIMethods().contains("bestrateditems")) {
                Operator o = operatorDAO.getOperatorFromToken(token);
                if (o == null)
                    exceptionResponse(WS.ACTION_BEST_RATED, MSG.WRONG_TOKEN, type, callback);
                else
                    apiKey = o.getApiKey();
            }

            Recommendation rr = null;
            Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

            if (coreTenantId == null)
                exceptionResponse(WS.ACTION_BEST_RATED, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

            RemoteTenant r = remoteTenantDAO.get(coreTenantId);

            if (r.isMaxActionLimitExceeded())
                exceptionResponse(WS.ACTION_BEST_RATED, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

            TimeConstraintVO tc = checkTimeConstraints(startDate, endDate);

            if (tc == null)
                exceptionResponse(WS.ACTION_BEST_RATED, MSG.DATE_PARSE, type, callback);

            requestedItemType = checkItemType(requestedItemType, type, coreTenantId, tenantId, WS.ACTION_BEST_RATED,
                                              callback, null);
            List<Item> items;

            if (tc != null) {
                items = shopRecommenderService.bestRatedItems(coreTenantId, userId, requestedItemType,
                                                              numberOfResults != null ? numberOfResults : WS.DEFAULT_NUMBER_OF_RESULTS,
                                                              offset, timeRange, tc,
                                                              new Session(null, request));

                rr = new Recommendation(tenantId, WS.ACTION_BEST_RATED, null, null, null, items);
                //added by FK on 2012-12-18 for adding profile data to recommendations.
                if (withProfile) {
                    addProfileDataToItems(rr);
                }
            }

            mon.stop();

            if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
                if (callback != null)
                    return Response.ok(new JSONPObject(callback, rr), WS.RESPONSE_TYPE_JSCRIPT).build();
                else
                    return Response.ok(rr, WS.RESPONSE_TYPE_JSON).build();
            } else
                return Response.ok(rr, WS.RESPONSE_TYPE_XML).build();

    }

    @GET
    @Path("/worstrateditems")
    public Response worstRatedItems(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                    @QueryParam("tenantid") String tenantId, @QueryParam("userid") String userId,
                                    @QueryParam("numberOfResults") Integer numberOfResults,
                                    @QueryParam("offset") @DefaultValue("0") Integer offset,
                                    @QueryParam("timeRange") String timeRange,
                                    @QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate,
                                    @QueryParam("requesteditemtype") String requestedItemType,
                                    @QueryParam("callback") String callback,
                                    @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                    @QueryParam("token") String token)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_WORST_RATED);

        if (easyrecSettings.getSecuredAPIMethods().contains("worstrateditems")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_WORST_RATED, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Recommendation rr = null;
        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_WORST_RATED, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);

        if (r.isMaxActionLimitExceeded())
            exceptionResponse(WS.ACTION_WORST_RATED, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

        TimeConstraintVO tc = checkTimeConstraints(startDate, endDate);

        if (tc == null)
            exceptionResponse(WS.ACTION_WORST_RATED, MSG.DATE_PARSE, type, callback);

        requestedItemType = checkItemType(requestedItemType, type, coreTenantId, tenantId, WS.ACTION_WORST_RATED, null, callback);
        List<Item> items;

        if (tc != null) {
            items = shopRecommenderService.worstRatedItems(coreTenantId, userId, requestedItemType,
                    numberOfResults != null ? numberOfResults : WS.DEFAULT_NUMBER_OF_RESULTS, offset, timeRange, tc,
                    new Session(null, request));

            rr = new Recommendation(tenantId, WS.ACTION_WORST_RATED, null, null, null, items);
            //added by FK on 2012-12-18 for adding profile data to recommendations.
            if (withProfile) {
                addProfileDataToItems(rr);
            }
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, rr), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(rr, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(rr, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/relateditems")
    public Response relatedItems(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                 @QueryParam("tenantid") String tenantId,
                                 @QueryParam("assoctype") String assocType, @QueryParam("userid") String userId,
                                 @QueryParam("sessionid") String sessionId, @QueryParam("itemid") String itemId,
                                 @QueryParam("numberOfResults") Integer numberOfResults,
                                 @QueryParam("offset") @DefaultValue("0") Integer offset,
                                 @QueryParam("itemtype") String itemType,
                                 @QueryParam("requesteditemtype") String requestedItemType,
                                 @QueryParam("callback") String callback,
                                 @QueryParam("withProfile") @DefaultValue("false") boolean withProfile,
                                 @QueryParam("token") String token)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_RELATED_ITEMS);

        if (easyrecSettings.getSecuredAPIMethods().contains("relateditems")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_RELATED_ITEMS, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Recommendation rec = null;
        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);
        Integer assocTypeId = null;

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_RELATED_ITEMS, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);

        if (r.isMaxActionLimitExceeded())
            exceptionResponse(WS.ACTION_RELATED_ITEMS, MSG.MAXIMUM_ACTIONS_EXCEEDED, type, callback);

        if (itemId == null)
            exceptionResponse(WS.ACTION_RELATED_ITEMS, MSG.ITEM_NO_ID, type, callback);

        if (assocType != null) {
            assocTypeId = typeMappingService.getIdOfAssocType(coreTenantId, assocType, Boolean.TRUE); // only visible assocTypes can be queried
            if (assocTypeId == null)
                exceptionResponse(WS.ACTION_RELATED_ITEMS, MSG.ASSOC_TYPE_DOES_NOT_EXIST, type, callback);
        } else {
            assocType = AssocTypeDAO.ASSOCTYPE_IS_RELATED;
        }

        itemType = checkItemType(itemType, type, coreTenantId, tenantId, WS.ACTION_RELATED_ITEMS, callback);
        requestedItemType = checkItemType(requestedItemType, type, coreTenantId, tenantId, WS.ACTION_RELATED_ITEMS, callback, null);
        Session session = new Session(null, request);

        try {
            if ((numberOfResults == null) || (numberOfResults > WS.DEFAULT_NUMBER_OF_RESULTS))
                numberOfResults = WS.DEFAULT_NUMBER_OF_RESULTS;

            rec = shopRecommenderService.relatedItems(coreTenantId, assocType, userId, itemId, itemType,
                                                      requestedItemType, session,
                    numberOfResults, offset);
            //added by FK on 2012-12-18 for adding profile data to recommendations.
            if (withProfile) {
                addProfileDataToItems(rec);
            }
        } catch (EasyRecRestException e) {
            exceptionResponse(WS.ACTION_RELATED_ITEMS, e.getMessageObject(), type,
                    callback);
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, rec), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(rec, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(rec, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/setitemactive")
    public Response setItemActive(@PathParam("type") String type, @QueryParam("apikey") String apiKey,
                                  @QueryParam("tenantid") String tenantId, @QueryParam("itemid") String itemId,
                                  @QueryParam("active") Boolean active, @QueryParam("itemtype") String itemType,
                                  @QueryParam("callback") String callback,
                                  @QueryParam("token") String token)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_ITEM_ACTIVE);

        if (easyrecSettings.getSecuredAPIMethods().contains("setitemactive")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_SET_ITEM_ACTIVE, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_SET_ITEM_ACTIVE, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        if (itemId == null)
            exceptionResponse(WS.ACTION_SET_ITEM_ACTIVE, MSG.ITEM_NO_ID, type, callback);

        if (active == null)
            exceptionResponse(WS.ACTION_SET_ITEM_ACTIVE, MSG.ITEM_NO_ACTIVE, type, callback);

        itemType = checkItemType(itemType, type, coreTenantId, tenantId, WS.ACTION_SET_ITEM_ACTIVE, callback);
        RemoteTenant r = remoteTenantDAO.get(coreTenantId);
        Item item = itemDAO.get(r, itemId, itemType);

        if (item == null)
            exceptionResponse(WS.ACTION_SET_ITEM_ACTIVE, MSG.ITEM_NOT_EXISTS, type, callback);
        else {
            if (item.isActive() && !active)
                itemDAO.deactivate(coreTenantId, itemId, itemType);

            if (!item.isActive() && active)
                itemDAO.activate(coreTenantId, itemId, itemType);
        }

        ResponseItem respItem = new ResponseItem(tenantId, WS.ACTION_SET_ITEM_ACTIVE + active, null, null, null, item);

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, respItem), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(respItem, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(respItem, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/itemtypes")
    public Response itemTypes(@PathParam("type") String type,
                              @QueryParam("apikey") String apiKey,
                              @QueryParam("tenantid") String tenantId,
                              @QueryParam("callback") String callback,
                              @QueryParam("token") String token)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_ITEMTYPES);

        if (easyrecSettings.getSecuredAPIMethods().contains("itemtypes")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_ITEMTYPES, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_ITEMTYPES, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        ResponseItemTypes responseItemTypes = null;

        try {
            Set<String> itemTypes = shopRecommenderService.getItemTypes(coreTenantId);
            responseItemTypes = new ResponseItemTypes(tenantId, itemTypes);
        } catch (EasyRecRestException e) {
            exceptionResponse(WS.ACTION_ITEMTYPES, e.getMessageObject().append("Failed to retrieve item types"), type,
                    callback);
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null) {
                return Response.ok(new JSONPObject(callback, responseItemTypes), WS.RESPONSE_TYPE_JSCRIPT).build();
            } else {
                return Response.ok(responseItemTypes, WS.RESPONSE_TYPE_JSON).build();
            }
        } else {
            return Response.ok(responseItemTypes, WS.RESPONSE_TYPE_XML).build();
        }
    }
    
    @GET
    @Path("/additemtype")
    public Response addItemType(@PathParam("type") String type,
                              @QueryParam("apikey") String apiKey,
                              @QueryParam("tenantid") String tenantId,
                              @QueryParam("itemtype") String itemType,
                              @QueryParam("visible") @DefaultValue("true") Boolean visible,
                              @QueryParam("callback") String callback,
                              @QueryParam("token") String token)
            throws EasyRecException {
        Monitor mon = MonitorFactory.start(JAMON_REST_ADDITEMTYPE);

        if (easyrecSettings.getSecuredAPIMethods().contains("additemtype")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_ADDITEMTYPE, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_ADDITEMTYPE, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);
        
        String error = tenantService.isValidItemTypeName(itemType);
        
        if (!"".equals(error))
            exceptionResponse(WS.ACTION_ADDITEMTYPE, new ErrorMessage(998, error), type, callback);
        
        Integer newId = tenantService.insertItemTypeForTenant(coreTenantId, itemType, visible);        
         
        mon.stop();
        
        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null) {
                return Response.ok(new JSONPObject(callback, newId), WS.RESPONSE_TYPE_JSCRIPT).build();
            } else {
                return Response.ok(newId, WS.RESPONSE_TYPE_JSON).build();
            }
        } else {
            return Response.ok(newId, WS.RESPONSE_TYPE_XML).build();
        }
        
    }
    
    @GET
    @Path("/deleteitemtype")
    public Response deleteItemType(@PathParam("type") String type,
                              @QueryParam("apikey") String apiKey,
                              @QueryParam("tenantid") String tenantId,
                              @QueryParam("itemtype") String itemType,
                              @QueryParam("callback") String callback,
                              @QueryParam("token") String token)
            throws EasyRecException {
        Monitor mon = MonitorFactory.start(JAMON_REST_DELETEITEMTYPE);

        if (easyrecSettings.getSecuredAPIMethods().contains("deleteitemtype")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_DELETEITEMTYPE, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_DELETEITEMTYPE, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);
        
        String error = tenantService.isValidItemTypeName(itemType);
        
        if (!"".equals(error))
            exceptionResponse(WS.ACTION_DELETEITEMTYPE, new ErrorMessage(998, error), type, callback);
        
        try {
            Integer typeId = typeMappingService.getIdOfItemType(coreTenantId, itemType);    
            tenantService.deleteItemTypeForTenant(coreTenantId, typeId);
        } catch (IllegalArgumentException iae) {
            exceptionResponse(WS.ACTION_DELETEITEMTYPE, new ErrorMessage(998, "Item type does not exist!"), type, callback);
        } catch (Exception e) {
            exceptionResponse(WS.ACTION_DELETEITEMTYPE, new ErrorMessage(998, e.getMessage()), type, callback);
        }
        mon.stop();
        
        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null) {
                return Response.ok(new JSONPObject(callback, new SuccessMessage(930, "itemType " + itemType + " and all references successfuly removed!")), WS.RESPONSE_TYPE_JSCRIPT).build();
            } else {
                return Response.ok(new SuccessMessage(930, "itemType " + itemType + " and all references successfuly removed!"), WS.RESPONSE_TYPE_JSON).build();
            }
        } else {
            return Response.ok(new SuccessMessage(930, "itemType " + itemType + " and all references successfuly removed!"), WS.RESPONSE_TYPE_XML).build();
        }
    }

    @GET
    @Path("/clusters")
    public Response clusters(@PathParam("type") String type,
                             @QueryParam("apikey") String apiKey,
                             @QueryParam("tenantid") String tenantId,
                             @QueryParam("callback") String callback,
                             @QueryParam("token") String token)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_CLUSTERS);

        if (easyrecSettings.getSecuredAPIMethods().contains("clusters")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_CLUSTERS, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);

        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_CLUSTERS, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        ResponseClusters responseClusters = null;

        try {
            List<ClusterVO> clusters = shopRecommenderService.getClusters(coreTenantId);
            responseClusters = new ResponseClusters(tenantId, clusters);
        } catch (EasyRecRestException e) {
            exceptionResponse(WS.ACTION_CLUSTERS, e.getMessageObject().append("Failed to retrieve clusters"), type,
                    callback);
        }

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null) {
                return Response.ok(new JSONPObject(callback, responseClusters), WS.RESPONSE_TYPE_JSCRIPT).build();
            } else {
                return Response.ok(responseClusters, WS.RESPONSE_TYPE_JSON).build();
            }
        } else {
            return Response.ok(responseClusters, WS.RESPONSE_TYPE_XML).build();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // import API
    ///////////////////////////////////////////////////////////////////////////
    @GET
    @Path("/importrule")
    public Response importrule(@PathParam("type") String type, @QueryParam("token") String token,
                               @QueryParam("tenantid") String tenantId, @QueryParam("itemfromid") String itemFromId,
                               @QueryParam("itemfromtype") String itemFromType, @QueryParam("itemtoid") String itemToId,
                               @QueryParam("itemtotype") String itemToType, @QueryParam("assocvalue") String assocvalue,
                               @QueryParam("assoctype") String assocType, @QueryParam("callback") String callback,
                               @QueryParam("apikey") String apiKey)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_IMPORT_RULE);

        if (easyrecSettings.getSecuredAPIMethods().contains("importrule")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_IMPORT_RULE, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        Item itemFrom = null;
        Item itemTo = null;
        Float assocValue = null;
        Integer assocTypeId = null;
        Integer coreTenantId = null;

        // Collect a List of messages for the user to understand,
        // what went wrong (e.g. Wrong API key).
        List<Message> messages = Lists.newArrayList();

        if (itemFromType != null) itemFromType = CharMatcher.WHITESPACE.trimFrom(itemFromType);
        if (itemToType != null) itemToType = CharMatcher.WHITESPACE.trimFrom(itemToType);

        if (Strings.isNullOrEmpty(itemFromType)) itemFromType = Item.DEFAULT_STRING_ITEM_TYPE;
        if (Strings.isNullOrEmpty(itemToType)) itemToType = Item.DEFAULT_STRING_ITEM_TYPE;

        // TODO create itemtypes if they don't exist?

        coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);
        if (coreTenantId == null)
            exceptionResponse(WS.ACTION_IMPORT_RULE, MSG.TENANT_WRONG_TENANT_APIKEY, type, callback);

        if (itemFromId != null && itemFromId.equals(itemToId))
            messages.add(MSG.ITEMFROM_EQUAL_ITEMTO);

        RemoteTenant r = remoteTenantDAO.get(coreTenantId);

        if (r != null) {
            itemFrom = itemDAO.get(r, itemFromId, itemFromType);

            if (itemFrom == null)
                messages.add(MSG.ITEM_FROM_ID_DOES_NOT_EXIST);

            itemTo = itemDAO.get(r, itemToId, itemToType);

            if (itemTo == null)
                messages.add(MSG.ITEM_TO_ID_DOES_NOT_EXIST);

            if (assocType == null)
                messages.add(MSG.ASSOC_TYPE_NEEDED);
            else {
                try {
                    assocTypeId = typeMappingService.getIdOfAssocType(coreTenantId, assocType);
                } catch (Exception e) {
                    messages.add(MSG.ASSOC_TYPE_DOES_NOT_EXIST);
                }
            }

        } else
            messages.add(MSG.TENANT_WRONG_TENANT);

        try {
            assocValue = Float.parseFloat(assocvalue);

            if (assocValue < 0 || assocValue > 100)
                messages.add(MSG.INVALID_ASSOC_VALUE);
        } catch (Exception e) {
            messages.add(MSG.INVALID_ASSOC_VALUE);
        }


        if (messages.size() > 0) {
            if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON))
                throw new EasyRecException(messages, WS.ACTION_IMPORT_RULE, WS.RESPONSE_TYPE_JSON, callback);
            else
                throw new EasyRecException(messages, WS.ACTION_IMPORT_RULE);
        }

        remoteAssocService.addRule(coreTenantId, itemFromId, itemFromType, itemToId, itemToType, assocTypeId,
                assocValue);

        ResponseRule respRule =
                new ResponseRule(tenantId, WS.ACTION_IMPORT_RULE, itemFrom.getItemId(), itemTo.getItemId(), assocType,
                        Float.toString(assocValue));
        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, respRule), WS.RESPONSE_TYPE_JSCRIPT).build();
            else
                return Response.ok(respRule, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(respRule, WS.RESPONSE_TYPE_XML).build();
    }

    @GET
    @Path("/importitem")
    public Response importitem(@PathParam("type") String type,
                               @QueryParam("token") String token,
                               @QueryParam("tenantid") String tenantId,
                               @QueryParam("itemid") String itemId,
                               @QueryParam("itemdescription") String itemDescription,
                               @QueryParam("itemurl") String itemUrl,
                               @QueryParam("itemimageurl") String itemImageUrl,
                               @QueryParam("itemtype") String itemType,
                               @QueryParam("callback") String callback,
                               @QueryParam("apikey") String apiKey)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_IMPORT_ITEM);

        if (easyrecSettings.getSecuredAPIMethods().contains("importitem")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_IMPORT_ITEM, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        // Collect a List of messages for the user to understand,
        // what went wrong (e.g. Wrong API key).
        List<Message> messages = new ArrayList<>();
        Integer coreTenantId = null;

        coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);
        checkParameters(coreTenantId, itemId, itemDescription, itemUrl, messages);

        if (messages.size() > 0) {
            if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON))
                throw new EasyRecException(messages, WS.ACTION_IMPORT_ITEM, WS.RESPONSE_TYPE_JSON, callback);
            else
                throw new EasyRecException(messages, WS.ACTION_IMPORT_ITEM);
        }

        itemType = checkItemType(itemType, type, coreTenantId, tenantId, WS.ACTION_IMPORT_ITEM, callback);

        itemDAO.insertOrUpdate(coreTenantId, itemId, itemType, itemDescription, itemUrl, itemImageUrl);

        ResponseItem respItem = new ResponseItem(tenantId, WS.ACTION_IMPORT_ITEM, null, null, null,
                itemDAO.get(remoteTenantDAO.get(coreTenantId), itemId, itemType));

        mon.stop();

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null)
                return Response.ok(new JSONPObject(callback, respItem), WS.RESPONSE_TYPE_JSCRIPT)
                        .build();
            else
                return Response.ok(respItem, WS.RESPONSE_TYPE_JSON).build();
        } else
            return Response.ok(respItem, WS.RESPONSE_TYPE_XML).build();
    }

    /**
     * With this call you can create a new cluster.
     *
     * @param type               "1.0" for XML response, "1.0/json" for JSON
     * @param token
     * @param tenantId
     * @param clusterId
     * @param clusterDescription
     * @param clusterParent
     * @param callback
     *
     * @param apiKey
     * @throws EasyRecException
     */
    @GET
    @Path("/createcluster")
    public Response createcluster(@PathParam("type") String type,
                                  @QueryParam("token") String token,
                                  @QueryParam("tenantid") String tenantId,
                                  @QueryParam("clusterid") String clusterId,
                                  @QueryParam("clusterdescription") String clusterDescription,
                                  @DefaultValue("CLUSTERS")
                                  @QueryParam("clusterparent") String clusterParent,
                                  @QueryParam("callback") String callback,
                                  @QueryParam("apikey") String apiKey)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_CREATE_CLUSTER);

        if (easyrecSettings.getSecuredAPIMethods().contains("createcluster")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_CREATE_CLUSTER, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        // Collect a List of messages for the user to understand,
        // what went wrong (e.g. Wrong API key).
        List<Message> errorMessages = new ArrayList<>();
        List<Message> successMessages = new ArrayList<>();
        Integer coreTenantId = null;

        coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);
        checkParameters(coreTenantId, clusterId, errorMessages);

        if (errorMessages.size() > 0) {
            if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON))
                throw new EasyRecException(errorMessages, WS.ACTION_CREATE_CLUSTER, WS.RESPONSE_TYPE_JSON, callback);
            else
                throw new EasyRecException(errorMessages, WS.ACTION_CREATE_CLUSTER);
        }

        try {
            clusterService.addCluster(coreTenantId, clusterId, clusterDescription, clusterParent);
        } catch (ClusterException e) {
            // ToDo: more fine-grained Exceptions and consequently more specific error numbers
            errorMessages.add(new ErrorMessage(799, e.getMessage()));
        }

        successMessages.add(MSG.CLUSTER_SUCCESSFULLY_CREATED);

        Response response = formatResponse(successMessages, errorMessages,
                WS.ACTION_CREATE_CLUSTER, type, callback);

        mon.stop();

        return response;
    }

    // ========= operator API methods =========

    /**
     * This call starts the plugins of the tenant and the operator
     * with the provided token.
     *
     * @param type     "1.0" for XML response, "1.0/json" for JSON
     * @param token    security toke you get from the user interface or via login API call
     * @param tenantId id of the tenant whose plugins should be started
     * @param callback
     * @param apiKey
     * @param forceRun
     *
     * @throws EasyRecException
     */

    @GET
    @Path("/startplugins")
    public Response startplugins(@PathParam("type") String type,
                                 @QueryParam("token") String token,
                                 @QueryParam("tenantid") String tenantId,
                                 @QueryParam("callback") String callback,
                                 @QueryParam("apikey") String apiKey,
                                 @QueryParam("forcerun") final boolean forceRun)
            throws EasyRecException {

        Monitor mon = MonitorFactory.start(JAMON_REST_START_PLUGINS);

        // Collect a List of messages for the user to understand,
        // what went wrong (e.g. Wrong API key).
        List<Message> errorMessages = new ArrayList<>();
        List<Message> successMessages = new ArrayList<>();

        if (easyrecSettings.getSecuredAPIMethods().contains("startplugins")) {
            Operator o = operatorDAO.getOperatorFromToken(token);
            if (o == null)
                exceptionResponse(WS.ACTION_START_PLUGINS, MSG.WRONG_TOKEN, type, callback);
            else
                apiKey = o.getApiKey();
        }

        final Integer coreTenantId = operatorDAO.getTenantId(apiKey, tenantId);
        if (coreTenantId == null) {
            exceptionResponse(WS.ACTION_START_PLUGINS, MSG.TENANT_NOT_EXISTS, type, callback);
        }

        if (!easyrecSettings.isGenerator()) {
            exceptionResponse(WS.ACTION_START_PLUGINS, MSG.PLUGIN_START_IN_FRONTEND_MODE, type, callback);
        }

        if (!pluginRegistry.isAllExecutablesStopped()) {
            exceptionResponse(WS.ACTION_START_PLUGINS, MSG.PLUGIN_START_ALREADY_RUNNING, type, callback);
        }

        final Properties tenantConfig = tenantService.getTenantConfig(coreTenantId);
        if (tenantConfig == null) {
            exceptionResponse(WS.ACTION_START_PLUGINS, MSG.PLUGIN_START_NO_TENANT_CONFIG, type, callback);
        }

        Runnable r = new Runnable() {
            public void run() {
                if ("true".equals(tenantConfig.getProperty(RemoteTenant.AUTO_ARCHIVER_ENABLED))) {
                    String daysString = tenantConfig.getProperty(RemoteTenant.AUTO_ARCHIVER_TIME_RANGE);
                    final int days = Integer.parseInt(daysString);
                    ArchivePseudoConfiguration configuration = new ArchivePseudoConfiguration(days);
                    configuration.setAssociationType("ARCHIVE");
                    NamedConfiguration namedConfiguration = new NamedConfiguration(coreTenantId, 0,
                            ArchivePseudoGenerator.ID, "Archive", configuration, true);

                    generatorContainer.runGenerator(namedConfiguration,
                            // create a log entry only for archiver runs where actions were actually archived
                            // --> remove log entries where the number of archived actions is 0
                            new Predicate<GeneratorStatistics>() {
                                public boolean apply(GeneratorStatistics input) {
                                    ArchivePseudoStatistics archivePseudoStatistics = (ArchivePseudoStatistics) input;

                                    return archivePseudoStatistics.getNumberOfArchivedActions() > 0;
                                }
                            }, true, true);
                }
                
                if ("true".equals(tenantConfig.getProperty(RemoteTenant.SESSION_TO_USER_MAPPING_ENABLED))) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date lastRun;
                    RemoteTenant remoteTenant = remoteTenantDAO.get(coreTenantId);
                    try {
                        lastRun = sdf.parse(remoteTenant.getCreationDate());
                    } catch (ParseException ex) {
                        lastRun = new Date(System.currentTimeMillis() - (365 * 86400000l)); //fallback one year
                    }
                    List<LogEntry> lastRunEntry = logEntryDAO.getLogEntriesForTenant(remoteTenant.getId(), SessionToUserMappingGenerator.ASSOCTYPE, 0, 1);

                    if ((lastRunEntry != null) && (!lastRunEntry.isEmpty())) {
                        LogEntry le = lastRunEntry.get(0);
                        lastRun = le.getStartDate();
                    }

                    SessionToUserMappingConfiguration configuration = new SessionToUserMappingConfiguration(lastRun);
                    configuration.setAssociationType("SESSION_USER_MAPPING");
                    NamedConfiguration namedConfiguration = new NamedConfiguration(remoteTenant.getId(), SessionToUserMappingGenerator.ASSOCTYPE,
                            SessionToUserMappingGenerator.ID, "Session-to-User-mapping", configuration, true);


                    generatorContainer.runGenerator(namedConfiguration, true);
                }

                generatorContainer.runGeneratorsForTenant(coreTenantId, forceRun);
            }
        };

        new Thread(r).start();

        successMessages.add(MSG.PLUGIN_STARTED);

        Response response = formatResponse(successMessages, errorMessages,
                WS.ACTION_START_PLUGINS, type, callback);

        mon.stop();

        return response;
    }

    // ================ private methods =================

    private void addProfileDataToItems(Recommendation recommendation) {
        for (Item item : recommendation.getRecommendedItems()) {
            item.setProfileData(profileService.getProfile(item));
        }
    }

    private void exceptionResponse(String operation, Message message, String type, String callback)
            throws EasyRecException {
        List<Message> messages = new ArrayList<>();
        messages.add(message);

        if (type.endsWith(WS.RESPONSE_TYPE_PATH_JSON))
            throw new EasyRecException(messages, operation, WS.RESPONSE_TYPE_JSON, callback);
        else
            throw new EasyRecException(messages, operation);
    }

    private void checkParameters(Integer coreTenantId, String itemId, String itemDescription, String itemUrl,
                                 List<Message> messages) throws EasyRecException {

        if (coreTenantId == null)
            messages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
        if (Strings.isNullOrEmpty(itemId))
            messages.add(MSG.ITEM_NO_ID);
        if (Strings.isNullOrEmpty(itemDescription)) {
            messages.add(MSG.ITEM_NO_DESCRIPTION);
        } else {
            if (itemDescription.length() > 500) {
                itemDescription = itemDescription.substring(0, 499);
            }
        }

        if (itemUrl == null)
            messages.add(MSG.ITEM_NO_URL);
    }

    private void checkParameters(Integer coreTenantId, String clusterID,
                                 List<Message> messages) throws EasyRecException {
        if (coreTenantId == null)
            messages.add(MSG.TENANT_WRONG_TENANT_APIKEY);
        if (clusterID == null)
            messages.add(MSG.CLUSTER_NO_ID);
    }

    private void checkParams(Integer coreTenantId, String itemId, String itemDescription, String itemUrl,
                             String sessionId, List<Message> messages) throws EasyRecException {
        checkParameters(coreTenantId, itemId, itemDescription, itemUrl, messages);

        if (Strings.isNullOrEmpty(sessionId))
            messages.add(MSG.USER_NO_SESSION_ID);
    }
    
    private void checkParams(Integer coreTenantId, String itemId, String itemDescription, String itemUrl,
                             String sessionId, String actionInfo, List<Message> messages) throws EasyRecException {
        checkParameters(coreTenantId, itemId, itemDescription, itemUrl, messages);

        if (Strings.isNullOrEmpty(sessionId))
            messages.add(MSG.USER_NO_SESSION_ID);
        
        if (!Strings.isNullOrEmpty(actionInfo)) {
            if (actionInfo.length() > 500) {
                messages.add(MSG.ACTION_INFO_TOO_LONG);
            } else {
                try {
                    objectMapper.readTree(actionInfo);
                } catch (IOException ex) {
                    messages.add(MSG.INVALID_JSON);
                }
            }
        }
    }

    private int checkItemType(String itemType, Integer coreTenantId, String tenantId, List<Message> messages) {
        if (itemType != null)
            itemType = itemType.trim();

        try {
            int type = typeMappingService.getIdOfItemType(coreTenantId, itemType, true);

            return type;
        } catch (IllegalArgumentException ex) {
            messages.add(MSG.OPERATION_FAILED.append(
                    String.format(" itemType %s not found for tenant %s", itemType, tenantId)));
            return 0;
        }
    }
    
    private String checkItemType(String itemType, String type, Integer coreTenantId, String tenantId, String operation,
                                 String callback) {
        return checkItemType(itemType, type, coreTenantId, tenantId, operation, callback, Item.DEFAULT_STRING_ITEM_TYPE);
    }

    private String checkItemType(String itemType, String type, Integer coreTenantId, String tenantId, String operation,
                                 String callback, @Nullable String defaultValue) {
        if (itemType != null)
            itemType = itemType.trim();

        if (Strings.isNullOrEmpty(itemType))
            return defaultValue;
        else
            try {
                typeMappingService.getIdOfItemType(coreTenantId, itemType, true);

                return itemType;
            } catch (IllegalArgumentException ex) {
                exceptionResponse(operation, MSG.OPERATION_FAILED.append(
                        String.format(" itemType %s not found for tenant %s", itemType, tenantId)), type, callback);

                return null;
            }
    }

    private TimeConstraintVO checkTimeConstraints(String startTime, String endTime) {
        Date startDate = null;
        Date endDate;

        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
        if (startTime != null) {
            startDate = MyUtils.dateFormatCheck(startTime, dateFormatter);

            if (startDate == null)
                return null;
        }

        if (endTime == null)
            endDate = new Date(System.currentTimeMillis());
        else {
            endDate = MyUtils.dateFormatCheck(endTime, dateFormatter);

            if (endDate == null)
                return null;
        }

        return new TimeConstraintVO(startDate, endDate);
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
     * @param callback     if set and responseType is jason the result will be returned
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
            if (responseType.endsWith(WS.RESPONSE_TYPE_PATH_JSON))
                throw new EasyRecException(messages, serviceName, WS.RESPONSE_TYPE_JSON, callback);
            else
                throw new EasyRecException(messages, serviceName);
        }

        if (respondData instanceof List) {
            respondData = new ResponseSuccessMessage(serviceName, (List<SuccessMessage>) respondData);
        }

        //convert respondData to Respond object
        if (responseType.endsWith(WS.RESPONSE_TYPE_PATH_JSON)) {
            if (callback != null) {
                return Response.ok(new JSONPObject(callback, respondData),
                        WS.RESPONSE_TYPE_JSCRIPT).build();
            } else {
                return Response.ok(respondData, WS.RESPONSE_TYPE_JSON).build();
            }
        } else if (WS.XML_PATH.equals(responseType)) {
            return Response.ok(respondData, WS.RESPONSE_TYPE_XML).build();
        } else {
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
    }
}

