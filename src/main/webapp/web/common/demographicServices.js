/*
 * DHDR-scoped shim for the AngularJS "demographicServices" module.
 *
 * The oscarPro DHDR viewer depends on a full demographicServices module that does not exist on
 * this codebase. The viewer only calls demographicService.getDemographic(), so this shim provides
 * exactly that one function, wrapping the existing DemographicService REST endpoint
 * (GET /ws/rs/demographics/{id}). Extend this module rather than porting the full oscarPro version.
 */
angular.module("demographicServices", [])
    .service("demographicService", function ($http, $q, $log) {
        return {
            apiPath: '../ws/rs/',
            configHeadersWithCache: {headers: {"Content-Type": "application/json", "Accept": "application/json"}, cache: true},

            getDemographic: function (demographicNo) {
                var deferred = $q.defer();
                $http.get(this.apiPath + 'demographics/' + demographicNo, this.configHeadersWithCache).then(function (response) {
                    deferred.resolve(response.data);
                }, function () {
                    deferred.reject("An error occurred while fetching demographic");
                });
                return deferred.promise;
            }
        };
    });
