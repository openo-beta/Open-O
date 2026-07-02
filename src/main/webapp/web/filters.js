/*
 * "oscarFilters" AngularJS module.
 *
 * The DHDR viewer uses AngularJS built-in filters (date, filter, orderBy, json) plus the custom
 * "age" filter (patient header). Only "age" is ported here from the oscarPro oscarFilters module;
 * add other filters if a view later needs one.
 */
angular.module('oscarFilters', [])
    .filter('age', function () {
        return function (input) {
            if (input != null && input.years != null) {
                if (input.years < 1 && input.months < 1) {
                    return input.days + "d";
                }
                if (input.years < 2) {
                    return input.months + "m";
                }
                return input.years + " y";
            }
            return "";
        };
    });
