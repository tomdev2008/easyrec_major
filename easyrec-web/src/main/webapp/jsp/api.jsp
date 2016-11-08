<%@ taglib prefix="easyrec" uri="/WEB-INF/tagLib.tld" %>
<%--
  ~ Copyright 2015 Research Studios Austria Forschungsgesellschaft mBH
  ~
  ~ This file is part of easyrec.
  ~
  ~ easyrec is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ easyrec is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with easyrec.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<script src="${webappPath}/js/angular/angular.min.js" type="text/javascript"></script>
<script src="${webappPath}/api-js/angularjs/easyrec.js" type="text/javascript"></script>
<script>
    var easyrec = angular.module('easyrec', ['easyrecControllers', 'easyrecServices']);
    
    easyrec.config(['easyrecProvider', function(easyrecProvider){
        easyrecProvider.setBaseUrl('${webappPath}');
        easyrecProvider.setApiKey('${apikey}');
        easyrecProvider.setTenant('${tenantId}');     
    }]);
    
    var easyrecControllers = angular.module('easyrecControllers', []);
    
    easyrecControllers.controller('MenuController', ['$scope',
        function ($scope) {
            
            $scope.templates = [
                {name: 'API Documentation',
                url: 'apidoc'},
                {name: 'Test the API',
                url: '${webappPath}/apitest?operatorId=${operatorId}&tenantId=${tenantId}'},
                {name: 'Test the Profile API',
                url: '${webappPath}/apitestprofile?operatorId=${operatorId}&tenantId=${tenantId}'}
            ];
            
            $scope.setActive = function(i) {
                $scope.active = $scope.templates[i];
            };
            
            $scope.active = $scope.templates[0];
            
        }
    ]);
    
    easyrecControllers.controller('TestController', ['$scope', '$http', 'easyrec',
        function ($scope, $http, easyrec) {
            $scope.calls = [
                {name: 'view', type :'Actions'},
                {name: 'buy', type :'Actions'},
                {name: 'rate', type :'Actions'},
                {name: 'sendaction', type :'Actions'},    
                {name: 'track', type :'Actions'},
                {name: 'otherusersalsoviewed', type :'Recommendations'},
                {name: 'otherusersalsobought', type :'Recommendations'},
                {name: 'itemsratedgoodbyotherusers', type :'Recommendations'},
                {name: 'relateditems',type :'Recommendations'},
                {name: 'recommendationsforuser',type :'Recommendations'},
                {name: 'actionhistoryforuser',type :'Recommendations'},
                {name: 'mostvieweditems',type :'Rankings'},
                {name: 'mostboughtitems',type :'Rankings'},
                {name: 'mostrateditems',type :'Rankings'},
                {name: 'bestrateditems',type :'Rankings'},
                {name: 'worstrateditems',type :'Rankings'},
                {name: 'clusters',type :'Cluster'},
                {name: 'itemsofcluster',type :'Cluster'},
                {name: 'createcluster',type :'Cluster'},
                {name: 'importrule',type :'Import & Other'},
                {name: 'importitem',type :'Import & Other'},
                {name: 'setitemactive',type :'Import & Other'},
                {name: 'itemtypes',type :'Import & Other'},
                {name: 'additemtype',type :'Import & Other'},
                {name: 'deleteitemtype',type :'Import & Other'}
            ];
            
            $scope.host = "${webappPath}/api/1.1/json/";
            $scope.apicall = $scope.calls[0];
            $scope.apikey = "${apikey}";
            $scope.tenantid = "${tenantId}";
            
            $scope.req = function() {
                       
                switch ($scope.apicall.name) {
                    case 'view':
                        easyrec.view($scope.sessionid, 
                                $scope.itemid,
                                $scope.itemtype,
                                $scope.itemdescription,
                                $scope.itemurl,
                                $scope.itemimageurl,
                                $scope.userid,
                                $scope.actioninfo,
                                $scope.actiontime,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'buy':
                        easyrec.buy($scope.sessionid, 
                                $scope.itemid,
                                $scope.itemtype,
                                $scope.itemdescription,
                                $scope.itemurl,
                                $scope.itemimageurl,
                                $scope.userid,
                                $scope.actioninfo,
                                $scope.actiontime,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'rate':
                        easyrec.rate($scope.sessionid, 
                                $scope.itemid,
                                $scope.itemtype,
                                $scope.itemdescription,
                                $scope.itemurl,
                                $scope.itemimageurl,
                                $scope.userid,
                                $scope.actioninfo,
                                $scope.ratingvalue,
                                $scope.actiontime,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'sendaction':
                        easyrec.sendaction($scope.sessionid, 
                                $scope.itemid,
                                $scope.itemtype,
                                $scope.itemdescription,
                                $scope.itemurl,
                                $scope.itemimageurl,
                                $scope.userid,
                                $scope.actioninfo,
                                $scope.actiontype,
                                $scope.actionvalue,
                                $scope.actiontime,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'track':
                        easyrec.track($scope.sessionid, 
                                $scope.itemfromid,
                                $scope.itemfromtype,
                                $scope.itemtoid,
                                $scope.itemtotype,
                                $scope.userid,
                                $scope.rectype,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'otherusersalsoviewed':
                        easyrec.alsoviewed($scope.sessionid, 
                                $scope.userid,                
                                $scope.itemid,
                                $scope.itemtype,
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'otherusersalsobought':
                        easyrec.alsobought($scope.sessionid, 
                                $scope.userid,                
                                $scope.itemid,
                                $scope.itemtype,
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'itemsratedgoodbyotherusers':
                        easyrec.alsogoodrated($scope.sessionid, 
                                $scope.userid,                
                                $scope.itemid,
                                $scope.itemtype,
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'relateditems':
                        easyrec.alsoviewed($scope.sessionid, 
                                $scope.userid,                
                                $scope.itemid,
                                $scope.itemtype,
                                $scope.assoctype,
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'recommendationsforuser':
                        easyrec.recsforuser($scope.sessionid, 
                                $scope.userid,                
                                $scope.actiontype,
                                $scope.assoctype,
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'actionhistoryforuser':
                        easyrec.actionhistoryforuser($scope.sessionid, 
                                $scope.userid,                
                                $scope.actiontype,
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'mostvieweditems':
                        easyrec.mostviewed(
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.timeRange,
                                $scope.startDate,
                                $scope.endDate,
                                $scope.clusterid,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'mostboughtitems':
                        easyrec.mostbought(
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.timeRange,
                                $scope.startDate,
                                $scope.endDate,
                                $scope.clusterid,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'mostrateditems':
                        easyrec.mostrated(
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.timeRange,
                                $scope.startDate,
                                $scope.endDate,
                                $scope.clusterid,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'bestrateditems':
                        easyrec.bestrated(
                                $scope.userid,
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.timeRange,
                                $scope.startDate,
                                $scope.endDate,
                                $scope.clusterid,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'worstrateditems':
                        easyrec.worstrated(
                                $scope.userid,
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.timeRange,
                                $scope.startDate,
                                $scope.endDate,
                                $scope.clusterid,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'clusters':
                        easyrec.clusters(
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'itemsofcluster':
                        easyrec.itemsofcluster(
                                $scope.offset,
                                $scope.numberOfResults,
                                $scope.requesteditemtype,
                                $scope.withProfile,
                                $scope.clusterid,
                                $scope.strategy,
                                $scope.usefallback,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'createcluster':
                        easyrec.createcluster(
                                $scope.clusterid,
                                $scope.clusterdescription,
                                $scope.clusterparent,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'importrule':
                        easyrec.importrule(
                                $scope.itemfromid,
                                $scope.itemfromtype,
                                $scope.itemtoid,
                                $scope.itemtotype,
                                $scope.assoctype,
                                $scope.assocvalue,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'importitem':
                        easyrec.importitem(
                                $scope.itemid,
                                $scope.itemtype,
                                $scope.itemdescription,
                                $scope.itemurl,
                                $scope.itemimageurl,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'setitemactive':
                        easyrec.setitemactive(
                                $scope.itemid,
                                $scope.itemtype,
                                $scope.active,
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'itemtypes':
                        easyrec.itemtypes(
                                $scope.token).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'additemtype':
                        easyrec.additemtype(
                                $scope.token, 
                                $scope.itemtype,
                                $scope.visible).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                    case 'deleteitemtype':
                        easyrec.deleteitemtype(
                                $scope.token,
                                $scope.itemtype).then(
                            function(data, status) {
                                $scope.status = status;
                                $scope.data = data;
                                }, function(data, status) {
                                $scope.status = status;
                                $scope.data = data || "Request failed"; 
                        });
                        break;
                }
        
//                var url = $scope.host + $scope.apicall.name;
//                var option = {
//                    apikey : $scope.apikey,
//                    tenantid : $scope.tenantid,
//                    itemid : $scope.itemid,
//                    itemtype : $scope.itemtype,
//                    itemdescription : $scope.itemdescription,
//                    itemurl : $scope.itemurl,
//                    itemimageurl : $scope.itemimageurl
//                };
//
//                var pars = {
//                    method: 'GET',
//                    url: url,
//                    params: option
//                };
//                
//                $http(pars).
//                success(function(data, status){
//                    $scope.status = status;
//                    $scope.data = data;
//                }).
//                error(function(data, status){
//                    $scope.status = status;
//                    $scope.data = data || "Request failed";
//                });
            };
        }

    ]);
    
    easyrecControllers.controller('TestProfileController', ['$scope', '$http', 
        function ($scope, $http) {
            
            /**
            * The workhorse; converts an object to x-www-form-urlencoded serialization.
            * @param {Object} obj
            * @return {String}
            */ 
           var encodeParam = function(obj) {
             var query = '', name, value, fullSubName, subName, subValue, innerObj, i;

             for(name in obj) {
               value = obj[name];

               if(value instanceof Array) {
                 for(i=0; i<value.length; ++i) {
                   subValue = value[i];
                   fullSubName = name + '[' + i + ']';
                   innerObj = {};
                   innerObj[fullSubName] = subValue;
                   query += param(innerObj) + '&';
                 }
               }
               else if(value instanceof Object) {
                 for(subName in value) {
                   subValue = value[subName];
                   fullSubName = name + '[' + subName + ']';
                   innerObj = {};
                   innerObj[fullSubName] = subValue;
                   query += param(innerObj) + '&';
                 }
               }
               else if(value !== undefined && value !== null)
                 query += encodeURIComponent(name) + '=' + encodeURIComponent(value) + '&';
             }

             return query.length ? query.substr(0, query.length - 1) : query;
           };
            
            $scope.calls = [
                {name: 'store', method :'POST'},
                {name: 'delete', method :'DELETE'},
                {name: 'load', method :'GET'},
                {name: 'field/store', method :'PUT'},
                {name: 'field/push', method :'PUT'},
                {name: 'field/load', method :'GET'},
                {name: 'field/delete', method :'DELETE'},
                {name: 'storeitemwithprofile', method :'POST'}
            ];
            
            $scope.host = "${webappPath}/api/1.1/json/profile/";
            $scope.apicall = $scope.calls[0];
            $scope.apikey = "${apikey}";
            $scope.tenantid = "${tenantId}";
            $scope.itemid = "42";
            $scope.itemtype = "ITEM";
            $scope.encode = "json";
            $scope.template =  { url : "${webappPath}/apitestprofile"};
            
            $scope.req = function() {
                
                var url = $scope.host + $scope.apicall.name;
                var option = {
                    apikey : $scope.apikey,
                    tenantid : $scope.tenantid,
                    itemid : $scope.itemid,
                    itemtype : $scope.itemtype,
                    itemdescription : $scope.itemdescription,
                    itemurl : $scope.itemurl,
                    itemimageurl : $scope.itemimageurl,
                    profile : $scope.profile,
                    path : $scope.path,
                    key : $scope.key,
                    value : $scope.value
                };
                var meth = $scope.apicall.method;
                var pars = {
                    method: meth,
                    url: url
                };
                if (meth==='GET' || meth==='DELETE') {
                    pars.params = option;
                } else {
                    if ($scope.encode==='json') {
                        pars.data = option;
                    } else {
                        pars.data = encodeParam(option);
                        pars.headers = {'Content-Type' : 'application/x-www-form-urlencoded'};
                    }
                }
                
                $http(pars).
                success(function(data, status){
                    $scope.status = status;
                    $scope.data = data;
                }).
                error(function(data, status){
                    $scope.status = status;
                    $scope.data = data || "Request failed";
                });
            };
            
    }]);
    
</script>
<div ng-app="easyrec">
    <div class="appendbody" ng-controller="MenuController">
        <h1>Recommendation API</h1>

        <div ng-repeat="template in templates" style="display: inline-block" ng-cloak>
            <a ng-if="active !== template" href="javascript:void(0)" ng-click="setActive($index)" style="clear:right">
                <span>{{template.name}}</span>
            </a>
            <span ng-if="active === template" class="bullmenu">{{template.name}}</span>
            <span ng-show="!$last"> &nbsp;&bull;  </span>
        </div>

        <div id="content" ng-include="active.url"/>

    </div>
</div>

<script type="text/ng-template" id="apidoc" >
    <p>
        We provide full access to easyrec's functionality through a <a href="https://sourceforge.net/p/easyrec/wiki/API/" target="_blank">REST API</a>.
        Recommendations are returned in XML or JSON notation. <br/>
        We also povide an AngularJS service that covers all API functions. You can find more details about using it at the <a href="https://sourceforge.net/p/easyrec/wiki/API/#angularjs-service" target="_blank">AngularJS service page</a> of our wiki.<br/>
        <b>Legacy:</b> You can also include a small snippet of <easyrec:wikiLink name="javascript code" pageName="JavaScript_API_v0.98"/> in your website to get
        recommendations.<br/>
        To get started with easyrec we recommend to read the <a href="https://sourceforge.net/p/easyrec/wiki/get_started/" target="_blank"> getting started guide.</a>
        and if you are completely lost feel free to ask us at the <a href="https://sourceforge.net/p/easyrec/forum/" target="_blank">forums</a>.
    </p>

    <br/>
    <table width="100%">
        <tr>
            <td colspan="2"><span class="headline">Actions</span>
                <hr>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                view
            </td>
            <td style="width:200px;height:40px;">
                <a target="_blank" href="http://sourceforge.net/p/easyrec/wiki/ActionAPI/#view">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Sending_Actions">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                buy
            </td>
            <td>
                <a target="_blank" href="http://sourceforge.net/p/easyrec/wiki/ActionAPI/#buy">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Sending_Actions">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                rate
            </td>
            <td>
                <a target="_blank" href="http://sourceforge.net/p/easyrec/wiki/ActionAPI/#rate">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Sending_Actions">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                sendaction
            </td>
            <td>
                <a target="_blank" href="http://sourceforge.net/p/easyrec/wiki/ActionAPI/#sendaction">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Sending_Actions">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                track
            </td>
            <td>
                <a target="_blank" href="http://sourceforge.net/p/easyrec/wiki/ActionAPI/#track">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
    </table>

    <br/>

    <table width="100%">
        <tr>
            <td colspan="2"><span class="headline">Recommendations</span>
                <hr>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                other users also viewed
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/RecommendationAPI/#other-users-also-viewed">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Recommendations">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                other users also bought
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/RecommendationAPI/#other-users-also-bought">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Recommendations">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                items rated good by other users
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/RecommendationAPI/#items-rated-good-by-other-users">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Recommendations">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                recommendations for user
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/RecommendationAPI/#recommendations-for-user">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Recommendations">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                related items
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/RecommendationAPI/#related-items">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Recommendations">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                action history for user
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/RecommendationAPI/#action-history-for-user">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Recommendations">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
    </table>

    <br/>

    <table width="100%">
        <tr>
            <td colspan="2"><span class="headline">Community Rankings</span>
                <hr>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                most viewed items
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/RankingsAPI/#most-viewed-items">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Rankings">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                most bought items
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/RankingsAPI/#most-bought-items">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Rankings">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                most rated items
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/RankingsAPI/#most-rated-items">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Rankings">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                best rated items
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/RankingsAPI/#best-rated-items">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Rankings">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                worst rated items
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/RankingsAPI/#worst-rated-items">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Rankings">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
    </table>

    <br/>

    <table width="100%">
        <tr>
            <td colspan="2"><span class="headline">Cluster</span>
                <hr>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                clusters
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ClusterAPI/#clusters">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Cluster_Related_Information">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>

        <tr>
            <td>
                items of cluster
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ClusterAPI/#items-of-cluster">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Cluster_Related_Information">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>

        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                create cluster
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ClusterAPI/#create-cluster">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
    </table>

    <br/>

    <table width="100%">
        <tr>
            <td colspan="2"><span class="headline">Import API & other functions</span>
                <hr>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                Import rule
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ImportAPI/#import-rule">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                Import/update item
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ImportAPI/#importupdate-item">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                set item active
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ImportAPI/#set-item-active">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                item types
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ImportAPI/#item-types">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
                <a target="_blank"
                   href="http://easyrec.sourceforge.net/wiki/index.php?title=JavaScript_API_v0.98#Receiving_Itemtypes">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-js.png"/>
                </a>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                add itemtype
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ImportAPI/#add-itemtype">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                delete itemtype
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ImportAPI/#delete-itemtype">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
    </table>

    <br/>
    <table width="100%">
        <tr>
            <td colspan="2"><span class="headline">Profile API</span>
                <hr>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                Store
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ProfileAPI/#store">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                Store item with profile
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ProfileAPI/#store-item-with-profile">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
        <tr  style="background-color: rgb(239, 239, 239);">
            <td>
                Delete
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ProfileAPI/#delete">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                Field Delete
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ProfileAPI/#field-delete">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                Load
            </td>
            <td style="width:200px;">
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ProfileAPI/#load">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                Field Load
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ProfileAPI/#field-load">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
        <tr style="background-color: rgb(239, 239, 239);">
            <td>
                Field Store
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ProfileAPI/#field-store">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
        <tr>
            <td>
                Field Push
            </td>
            <td>
                <a target="_blank"
                   href="http://sourceforge.net/p/easyrec/wiki/ProfileAPI/#field-push">
                    <img style="" alt="wiki link" src="${webappPath}/img/button_wiki-rest.png"/>
                </a>
            </td>
        </tr>
    </table>

</script>

