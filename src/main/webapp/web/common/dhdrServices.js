/*

    Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
    This software is published under the GPL GNU General Public License.
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

    This software was written for the
    Department of Family Medicine
    McMaster University
    Hamilton
    Ontario, Canada

*/
/*
 * DHDR client service. Trimmed to the endpoints the DHDR viewer consumes; the gateway-log, UAO and
 * token-expiry methods from the oscarPro original are intentionally omitted because they belong to
 * the EHR-connectivity admin surface (see DHDRService.java, which excludes the matching endpoints).
 */
angular.module("dhdrServices", [])
    .service("dhdrService", function ($http, $q, $log) {
        return {
            apiPath: '../ws/rs',
            configHeaders: {headers: {"Content-Type": "application/json", "Accept": "application/json"}},
            configHeadersWithCache: {headers: {"Content-Type": "application/json", "Accept": "application/json"}, cache: true},

            searchByDemographicNo2: function (demographicNo, searchConfig) {
                var deferred = $q.defer();
                $http({
                    url: this.apiPath + '/dhdr/searchByDemographicNo2?demographicNo=' + demographicNo,
                    method: "POST",
                    data: searchConfig,
                    headers: this.configHeaders,
                }).then(function (response) {
                    deferred.resolve(response.data);
                }, function (data, status, headers) {
                    deferred.reject("An error occured while getting phr content");
                });
                return deferred.promise;
            },
            // Calls the ported /dhdr/getConsentOveride endpoint, which returns the PCOI viewlet URL
            // (in referenceURL) plus a correlation token (uuid). Replaces oscarPro's closed /kaiemr
            // viewlet launch; the override target is fixed server-side, so key is no longer sent.
            getConsentOveride: function (demographicNo, key) {
                var deferred = $q.defer();
                $http({
                    url: this.apiPath + '/dhdr/getConsentOveride?demographicNo=' + demographicNo,
                    method: "GET",
                    headers: this.configHeaders,
                }).then(function (response) {
                    deferred.resolve(response);
                }, function (data, status, headers) {
                    console.log("data error ", data);
                    deferred.reject("An error occured check log for additional details");
                });
                return deferred.promise;
            },
            logConsentOverride: function (demographicNo, uniqueToken, dataReceived, status) {
                var deferred = $q.defer();
                $http({
                    url: this.apiPath + '/dhdr/logConsentOverride/' + demographicNo + '/' + uniqueToken + '?status=' + status,
                    method: "POST",
                    data: dataReceived,
                    headers: this.configHeaders,
                }).then(function (response) {
                    deferred.resolve(response);
                }, function (data, status, headers) {
                    console.log("data error ", data);
                    deferred.reject("An error occured check log for additional details");
                });
                return deferred.promise;
            }
        };
    });
