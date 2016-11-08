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
var easyrecServices = angular.module('easyrecServices', []);

easyrecServices.service('delay', ['$q', '$timeout', 
        function ($q, $timeout) {
            return {
              /**
               * ggfgfgf
               * @param {type} $q
               * @param {type} $http
               * @returns {themoviedb.hmtl_L31.themoviedb.hmtlAnonym$5}
               */
              start: function () {
                  var deferred = $q.defer();
                  $timeout(deferred.resolve, 200);
                  return deferred.promise;
              }  
            };
        }]);

easyrecServices.provider('easyrec', function easyrecProvider() {
    var baseUrl = 'http://demo.easyrec.org';
    var apikey = '';
    var tenant = '';
    
    this.setBaseUrl = function(url) {
        baseUrl = url;
    };
    
    this.setApiKey = function(key) {
        apikey = key;
    };
    
    this.setTenant = function(t) {
        tenant = t;
    };
    
    this.$get = ['$q','$http', function easyrecFactory($q, $http) {
        return {
            view: function(sessionid, itemid, itemtype, itemdescription, itemurl, itemimageurl, userid, actioninfo, actiontime, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/view',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        sessionid : sessionid,
                        itemid : itemid,
                        itemtype : itemtype,
                        itemdescription : itemdescription,
                        itemurl : itemurl,
                        itemimageurl : itemimageurl,
                        userid: userid,
                        actioninfo : actioninfo,
                        actiontime: actiontime,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            buy: function(sessionid, itemid, itemtype, itemdescription, itemurl, itemimageurl, userid, actioninfo, actiontime, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/buy',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        sessionid : sessionid,
                        itemid : itemid,
                        itemtype : itemtype,
                        itemdescription : itemdescription,
                        itemurl : itemurl,
                        itemimageurl : itemimageurl,
                        userid: userid,
                        actioninfo : actioninfo,
                        actiontime: actiontime,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            rate: function(sessionid, itemid, itemtype, itemdescription, itemurl, itemimageurl, userid, actioninfo, ratingvalue, actiontime, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/rate',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        sessionid : sessionid,
                        itemid : itemid,
                        itemtype : itemtype,
                        itemdescription : itemdescription,
                        itemurl : itemurl,
                        itemimageurl : itemimageurl,
                        userid: userid,
                        actioninfo : actioninfo,
                        ratingvalue : ratingvalue,
                        actiontime: actiontime,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            sendaction: function(sessionid, itemid, itemtype, itemdescription, itemurl, itemimageurl, userid, actioninfo, actiontype, actionvalue, actiontime, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/sendaction',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        sessionid : sessionid,
                        itemid : itemid,
                        itemtype : itemtype,
                        itemdescription : itemdescription,
                        itemurl : itemurl,
                        itemimageurl : itemimageurl,
                        userid: userid,
                        actioninfo : actioninfo,
                        actiontype : actiontype,
                        actionvalue : actionvalue,
                        actiontime: actiontime,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            track: function(sessionid, itemfromid, itemfromtype, itemtoid, itemtotype, userid, rectype, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/track',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        sessionid: sessionid,
                        userid: userid,
                        itemfromid: itemfromid,
                        itemfromtype: itemfromtype,
                        itemtoid: itemtoid,
                        itemtotype: itemtotype,
                        rectype: rectype,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            alsoviewed: function(sessionid, userid, itemid, itemtype, offset, numberOfResults, requesteditemtype, withProfile, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/otherusersalsoviewed',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        sessionid: sessionid,
                        userid: userid,
                        itemid: itemid,
                        itemtype: itemtype,
                        requesteditemtype : requesteditemtype,
                        offset : offset,
                        numberOfResults : numberOfResults,
                        withProfile : withProfile,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            alsobought: function(sessionid, userid, itemid, itemtype, offset, numberOfResults, requesteditemtype, withProfile, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/otherusersalsobought',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        sessionid: sessionid,
                        userid: userid,
                        itemid: itemid,
                        itemtype: itemtype,
                        requesteditemtype : requesteditemtype,
                        offset : offset,
                        numberOfResults : numberOfResults,
                        withProfile : withProfile,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            alsogoodrated: function(sessionid, userid, itemid, itemtype, offset, numberOfResults, requesteditemtype, withProfile, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/itemsratedgoodbyotherusers',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        sessionid: sessionid,
                        userid: userid,
                        itemid: itemid,
                        itemtype: itemtype,
                        requesteditemtype : requesteditemtype,
                        offset : offset,
                        numberOfResults : numberOfResults,
                        withProfile : withProfile,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            relateditems: function(sessionid, userid, itemid, itemtype, assoctype, offset, numberOfResults, requesteditemtype, withProfile, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/relateditems',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        sessionid : sessionid,
                        userid : userid,
                        itemid: itemid,
                        itemtype : itemtype,
                        assoctype : assoctype,
                        requesteditemtype : requesteditemtype,
                        offset : offset,
                        numberOfResults : numberOfResults,
                        withProfile: withProfile,
                        token : token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            /**
             * 
             * @param {type} sessionid
             * @param {type} userid
             * @param {type} actiontype
             * @param {type} assoctype
             * @param {type} requesteditemtype
             * @param {type} numberOfResults
             * @param {type} withProfile
             * @param {type} token
             * @returns {$q@call;defer.promise}
             */
            recsforuser: function(sessionid, userid, actiontype, assoctype, offset, numberOfResults,  requesteditemtype, withProfile, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/recommendationsforuser',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        sessionid : sessionid,
                        userid : userid,
                        actiontype : actiontype,
                        assoctype : assoctype,
                        requesteditemtype : requesteditemtype,
                        offset : offset,
                        numberOfResults : numberOfResults,
                        withProfile: withProfile,
                        token : token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            actionhistoryforuser: function(sessionid, userid, actiontype, offset, numberOfResults, requesteditemtype, withProfile, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/actionhistoryforuser',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        sessionid : sessionid,
                        userid : userid,
                        actiontype : actiontype,
                        requesteditemtype : requesteditemtype,
                        offset : offset,
                        numberOfResults : numberOfResults,
                        withProfile: withProfile,
                        token : token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            mostviewed: function(offset, numberOfResults, requesteditemtype, withProfile, timeRange, startDate, endDate, clusterid, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/mostvieweditems',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        requesteditemtype : requesteditemtype,
                        withProfile : withProfile,
                        timeRange : timeRange,
                        startDate : startDate,
                        endDate : endDate,
                        clusterid : clusterid,
                        offset : offset,
                        numberOfResults : numberOfResults,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            mostbought: function(offset, numberOfResults, requesteditemtype, withProfile, timeRange, startDate, endDate, clusterid, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/mostboughtitems',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        requesteditemtype : requesteditemtype,
                        withProfile : withProfile,
                        timeRange : timeRange,
                        startDate : startDate,
                        endDate : endDate,
                        clusterid : clusterid,
                        offset : offset,
                        numberOfResults : numberOfResults,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            mostrated: function(offset, numberOfResults, requesteditemtype, withProfile, timeRange, startDate, endDate, clusterid, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/mostrateditems',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        requesteditemtype : requesteditemtype,
                        withProfile : withProfile,
                        timeRange : timeRange,
                        startDate : startDate,
                        endDate : endDate,
                        clusterid : clusterid,
                        offset : offset,
                        numberOfResults : numberOfResults,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            bestrated: function(userid, offset, numberOfResults, requesteditemtype, withProfile, timerange, startDate, endDate, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/bestrateditems',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        userid : userid,
                        offset : offset,
                        numberOfResults : numberOfResults,
                        requesteditemtype : requesteditemtype,
                        withProfile : withProfile,
                        timeRange : timerange,
                        startDate : startDate,
                        endDate : endDate,
                        token : token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            worstrated: function(userid, offset, numberOfResults, requesteditemtype, withProfile, timerange, startDate, endDate, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/bestrateditems',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        userid : userid,
                        offset : offset,
                        numberOfResults : numberOfResults,
                        requesteditemtype : requesteditemtype,
                        withProfile : withProfile,
                        timeRange : timerange,
                        startDate : startDate,
                        endDate : endDate,
                        token : token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            clusters: function(token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/clusters',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.value);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            itemsofcluster: function(offset, numberOfResults, requesteditemtype, withProfile, clusterid, strategy, usefallback,token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/itemsofcluster',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        offset : offset,
                        numberOfResults: numberOfResults,
                        requesteditemtype: requesteditemtype,
                        withProfile: withProfile,
                        clusterid: clusterid,
                        strategy: strategy,
                        usefallback: usefallback,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.value);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            createcluster: function(clusterid, clusterdescription, clusterparent, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/createcluster',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        clusterid: clusterid,
                        clusterdescription: clusterdescription,
                        clusterparent: clusterparent,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.value);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            importrule: function(itemfromid, itemfromtype, itemtoid, itemtotype, assoctype, assocvalue, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/importrule',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        itemfromid: itemfromid,
                        itemfromtype: itemfromtype,
                        itemtoid: itemtoid,
                        itemtotype: itemtotype,
                        assoctype: assoctype,
                        assocvalue: assocvalue,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.value);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            importitem: function(itemid, itemtype, itemdescription, itemurl, itemimageurl, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/importitem',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        itemid: itemid,
                        itemtype: itemtype,
                        itemdescription: itemdescription,
                        itemurl: itemurl,
                        itemimageurl: itemimageurl,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.value);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            setitemactive: function(itemid, itemtype, active, token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/setitemactive',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        itemid: itemid,
                        itemtype: itemtype,
                        active: active,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.value);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            itemtypes: function(token) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/itemtypes',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        token: token
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.itemType);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            additemtype: function(token, itemtype, visible) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/additemtype',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        token: token,
                        itemtype: itemtype,
                        visible: visible
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            deleteitemtype: function(token, itemtype) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/deleteitemtype',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        token: token,
                        itemtype: itemtype
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data);
                    } else { //was error
                        deferred.reject(data);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            getprofile: function(itemid, itemtype) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/profile/load',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        itemid: itemid,
                        itemtype : itemtype
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.value);
                    } else { //was error
                        deferred.reject(data[0].message);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            getprofilefield: function(itemid, itemtype, path) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/profile/field/load',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        itemid: itemid,
                        itemtype : itemtype,
                        path : path
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.value);
                    } else { //was error
                        deferred.reject(data[0].message);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            storeprofile: function(itemid, itemtype,requesteditemtype) {
                var deferred = $q.defer();
                $http.get(baseUrl + '/api/1.1/json/profile/store',
                {params : {
                        apikey: apikey,
                        tenantid: tenant,
                        itemid: itemid,
                        itemtype : itemtype,
                        requesteditemtype : requesteditemtype
                    }
                }).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data[0].message);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },
            storeitemwithprofile: function(itemid, itemtype, itemdescription, itemurl, itemimageurl, profile) {
                var deferred = $q.defer();
                $http.post(baseUrl + '/api/1.1/json/profile/storeitemwithprofile',
                {
                        apikey: apikey,
                        tenantid: tenant,
                        itemid: itemid,
                        itemtype : itemtype,
                        itemdescription : itemdescription,
                        itemurl : itemurl,
                        itemimageurl : itemimageurl,
                        profile : profile
                    }
                ).success(function(data, status) {
                    if (!angular.isArray(data)) {
                        deferred.resolve(data.recommendedItems);
                    } else { //was error
                        deferred.reject(data[0].message);
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            },

            getdata: function(filename) {
                var deferred = $q.defer();
                $http.get('http://localhost:8084/AAA/' + filename,
                {params : {
 
                    }
                }).success(function(data, status) {
                    if (angular.isArray(data)) {
                        deferred.resolve(data);
                    } else { //was error
                        deferred.reject("Error loading products!");
                    }
                }).error(function(data, status) {
                    deferred.reject(status + data);
                });
                return deferred.promise;
            }          
        };
    }];
});