"use strict";

    var siAppFilters = angular.module('siApp.filters', [
    ]);

    siAppFilters.filter('numberToLetter', function () {
        return function (input, toLower) {
            var retval = 'FORMAT-REJECTED: ' + input; // initialize to this
            if (!isNaN(input)) { // expecting to start as a number
                // see reference to: http://stackoverflow.com/questions/3145030/convert-integer-into-its-character-equivilent-in-javascript
                if (toLower) {
                    retval = String.fromCharCode(97 + input);
                } else { // default to upper case
                    retval = String.fromCharCode(65 + input);
                }
                retval += '.'; // add the dot suffix as in "B. "
            }
            return retval;
        };
    });
