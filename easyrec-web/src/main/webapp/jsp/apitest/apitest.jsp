<%-- 
    Document   : apitest
    Created on : 18.08.2015, 14:49:51
    Author     : stephan
--%>

<div ng-controller="TestController">
    <br/>
    <label id="host">Host: {{host}}</label><br/>
    <br/>
<!--    <input id="host" ng-model="host" style="width:100%" value="http://localhost:8080/easyrec-web/api/1.1/json/profile/"><br>-->
    API call<br/>
    <select ng-model="apicall" ng-options="call.name group by call.type for call in calls" id="servletname" style="width:30%;">
    </select>

<hr/>
    <label id="apikey" >API Key: {{apikey}}</label><br/>
    <label id="tenantid" >Tenant ID: {{tenantid}}</label><br/>
<br>
<div ng-hide="apicall.type === 'Rankings' || apicall.type === 'Cluster' || apicall.type === 'Import & Other'">
sessionid*<input ng-model="sessionid" id="sessionid" style="width:100%"><br>
</div>
<div ng-hide="(apicall.type === 'Rankings' && (apicall.name !== 'bestrateditems' && apicall.name !== 'worstrateditems'))  || apicall.type === 'Cluster' || apicall.type === 'Import & Other'">
userid<input ng-model="userid" id="userid" style="width:100%"><br>
</div>
<div ng-show="(apicall.type === 'Actions' && apicall.name !== 'track') || (apicall.type === 'Recommendations' && (apicall.name !== 'recommendationsforuser') && (apicall.name !== 'actionhistoryforuser')) || apicall.name === 'setitemactive' || apicall.name === 'importitem'">
itemid*<input ng-model="itemid" id="itemid" style="width:100%"><br>
</div>
<div ng-show="(apicall.type === 'Actions' && apicall.name !== 'track') || (apicall.type === 'Recommendations' && (apicall.name !== 'recommendationsforuser') && (apicall.name !== 'actionhistoryforuser')) || apicall.name === 'setitemactive' || apicall.name === 'importitem' || apicall.name === 'additemtype' || apicall.name === 'deleteitemtype'">
itemtype<input ng-model="itemtype" id="itemtype" style="width:100%"><br>  
</div>
<div ng-show="(apicall.type === 'Actions' && apicall.name !== 'track') || apicall.name === 'importitem'">
<hr/>
itemdescription*<input ng-model="itemdescription" id="itemdescription" style="width:100%"><br>
itemurl*<input ng-model="itemurl" id="itemurl" style="width:100%"><br>
itemimageurl<input ng-model="itemimageurl" id="itemimageurl" style="width:100%"><br>
</div>
<div ng-show="apicall.type === 'Actions' && apicall.name !== 'track'">
actioninfo<input ng-model="actioninfo" id="actioninfo" style="width:100%"><br>    
</div>
<div ng-show="apicall.name === 'track' || apicall.name === 'importrule'">
<hr/>
itemfromid<input ng-model="itemfromid" id="itemid" style="width:100%"><br>
itemfromtype<input ng-model="itemfromtype" id="itemtype" style="width:100%"><br>
itemtoid*<input ng-model="itemtoid" id="itemdescription" style="width:100%"><br>
itemtotype<input ng-model="itemtotype" id="itemurl" style="width:100%"><br>
</div>
<div ng-show="apicall.name === 'track'">
rectype<input ng-model="rectype" id="itemimageurl" style="width:100%"><br>
</div>
<div ng-hide="apicall.name === 'track' || apicall.type !== 'Actions'">actiontime<input id="actiontime" style="width:100%"><br></div>
<div ng-show="apicall.name === 'rate'">
ratingvalue*<input ng-model="ratingvalue" id="ratingvalue" style="width:100%"><br>
</div>
<div ng-show="apicall.name === 'sendaction' || apicall.name === 'recommendationsforuser' || apicall.name === 'actionhistoryforuser'">
actiontype*<input ng-model="actiontype" id="actiontype" style="width:100%"><br>
</div>
<div ng-show="apicall.name === 'sendaction'">
actionvalue<input ng-model="actionvalue" id="actionvalue" style="width:100%"><br>
</div>
<div ng-show="apicall.type === 'Recommendations' || apicall.type === 'Rankings' || apicall.name === 'itemsofcluster'">
<hr>
offset<input ng-model="offset" id="offset" style="width:100%"><br>
numberOfResults<input ng-model="numberOfResults" id="numberOfResults" style="width:100%"><br>
requesteditemtype<input ng-model="requesteditemtype" id="requesteditemtype" style="width:100%"><br>
withProfile<input ng-model="withProfile" id="withProfile" style="width:100%"><br>
</div>
<div ng-show="apicall.type === 'Rankings'">
timeRange<input ng-model="timeRange" id="timeRange" style="width:100%"><br>
startDate<input ng-model="startDate" id="startDate" style="width:100%"><br>
endDate<input ng-model="endDate" id="endDate" style="width:100%"><br>
</div>
<div ng-show="apicall.name === 'itemsofcluster' || (apicall.type === 'Rankings' && (apicall.name !== 'bestrateditems' && apicall.name !== 'worstrateditems')) || apicall.name === 'createcluster' ">
clusterid<input ng-model="clusterid" id="clusterid" style="width:100%"><br>
</div>
<div ng-show="apicall.name === 'itemsofcluster'">
<hr/>
strategy<input ng-model="strategy" id="strategy" style="width:100%"><br>
usefallback<input ng-model="usefallback" id="usefallback" style="width:100%"><br>
</div>
<hr/>
<div ng-show="apicall.name === 'createcluster'">
clusterdescription<input ng-model="clusterdescription" id="clusterdescription" style="width:100%"><br>
clusterparent<input ng-model="clusterparent" id="clusterparent" style="width:100%"><br>
</div>
<div ng-show="apicall.name === 'relateditems' || apicall.name === 'recommendationsforuser' || apicall.name === 'importrule'">
assoctype<input ng-model="assoctype" id="assoctype" style="width:100%"><br>
</div>
<div ng-show="apicall.name === 'importrule'">
assocvalue<input ng-model="assocvalue" id="assocvalue" style="width:100%"><br>
</div>
<div ng-show="apicall.name === 'setitemactive'">
active<input ng-model="isactive" id="active" style="width:100%"><br>
</div>
<div ng-show="apicall.name === 'additemtype'">
visible<input ng-model="visible" id="visible" style="width:100%"><br>
</div>
token<input ng-model="token" id="token" style="width:100%"><br>
<label>Request:</label><br/>
GET {{apicall.method}} {{host}}{{apicall.name}}?apikey={{apikey}}&tenantid={{tenantid}}<span ng-hide="apicall.type === 'Rankings' || apicall.type === 'Cluster' || apicall.type === 'Import & Other'">&sessionid={{sessionid}}</span><span ng-hide="(apicall.type === 'Rankings' && (apicall.name !== 'bestrateditems' && apicall.name !== 'worstrateditems'))  || apicall.type === 'Cluster' || apicall.type === 'Import & Other'">&userid={{userid}}</span><span ng-show="(apicall.type === 'Actions' && apicall.name !== 'track') || (apicall.type === 'Recommendations' && (apicall.name !== 'recommendationsforuser') && (apicall.name !== 'actionhistoryforuser')) || apicall.name === 'setitemactive' || apicall.name === 'importitem'">&itemid={{itemid}}&itemtype={{itemtype}}</span><span ng-show="(apicall.type === 'Actions' && apicall.name !== 'track') || apicall.name === 'importitem'">&itemdescription={{itemdescription}}&itemurl={{itemurl}}&itemimageurl={{itemimageurl}}</span><span ng-show="apicall.type === 'Actions' && apicall.name !== 'track'">&actioninfo={{actioninfo}}</span><span ng-show="apicall.name === 'track' || apicall.name === 'importrule'">&itemfromid={{itemfromid}}&itemfromtype={{itemfromtype}}&itemtoid={{itemtoid}}&itemtotype={{itemtotype}}</span><span ng-show="apicall.name === 'track'">&rectype={{rectype}}</span><span ng-hide="apicall.name === 'track' || apicall.type !== 'Actions'">&actiontime={{actiontime}}</span><span ng-show="apicall.name === 'rate'">&ratingvalue={{ratingvalue}}</span><span ng-show="apicall.name === 'sendaction' || apicall.name === 'recommendationsforuser' || apicall.name === 'actionhistoryforuser'">&actiontype={{actiontype}}</span><span ng-show="apicall.name === 'sendaction'">&actionvalue={{actionvalue}}</span><span ng-show="apicall.type === 'Recommendations' || apicall.type === 'Rankings' || apicall.name === 'itemsofcluster'">&numberOfResults={{numberOfResults}}&requesteditemtype={{requesteditemtype}}&withProfile={{withProfile}}</span><span ng-show="apicall.type === 'Rankings'">&timeRange={{timeRange}}&startDate={{startDate}}&endDate={{endDate}}</span><span ng-show="apicall.name === 'itemsofcluster' || (apicall.type === 'Rankings' && (apicall.name !== 'bestrateditems' && apicall.name !== 'worstrateditems')) || apicall.name === 'createcluster' ">&clusterid={{clusterid}}</span><span ng-show="apicall.name === 'itemsofcluster'">&strategy={{strategy}}&usefallback={{usefallback}}</span><span ng-show="apicall.name === 'createcluster'">&clusterdescription={{clusterdescription}}&clusterparent={{clusterparent}}</span><span ng-show="apicall.name === 'relateditems' || apicall.name === 'recommendationsforuser' || apicall.name === 'importrule'">&assoctype={{assoctype}}</span><span ng-show="apicall.name === 'importrule'">&assocvalue={{assoctype}}</span><span ng-show="apicall.name === 'setitemactive'">&active={{isactive}}</span>&token={{token}}
<br/>
<a href="#" class="button--filled easyrecblue" ng-click="req()">Send Action</a><br/>
<label>Result:</label><br/>
Status: {{status}}<br/>
Data: {{data}}
</div>

