"use strict";

(function() {
    var siApp = angular.module('siApp', [
        'ngRoute',
        'ngResource',
        'ngMessages',
        'ngAnimate',
        'ngTouch',
        'infinite-scroll',
        'ui.bootstrap',
        'ui.utils',
        'easypiechart',
        'ngFileUpload',

        /* siApp */
        'siApp.controllers',
        'siApp.directives',
        'siApp.filters',
        'siApp.services'
    ]);

    siApp.config(function ($routeProvider, $httpProvider, $provide) {

        $provide.factory('siHttpInterceptor', function($location) {
            return {
                'response': function (response) {
                    var redirectUrl = response.headers("Redirect-Url");
                    if (redirectUrl) {
                        $location.path(redirectUrl);
                    }
                    return response;
                }
            };
        });
        $httpProvider.interceptors.push('siHttpInterceptor');

        $routeProvider
            .when('/', {
                title: 'Social Intuition',
                templateUrl: '/templates/home',
                controller: 'HomeController'
            })
            .when('/sink', {
                title: 'Kitchen Sink',
                templateUrl: '/templates/sink'
            })
            .when('/signup', {
                title: 'Sign up',
                templateUrl: '/templates/signup',
                controller: 'SignupController',
                caseInsensitiveMatch: true
            })
            .when('/verification', {
                title: 'Verification',
                templateUrl: '/templates/verification',
                controller: 'VerificationController'
            })
            .when('/login', {
                title: 'Login',
                templateUrl: '/templates/login',
                controller: 'LoginController',
                caseInsensitiveMatch: true
            })
            .when('/profile/:username', {
                title: 'Profile',
                templateUrl: '/templates/profile',
                controller: 'ProfileController',
                caseInsensitiveMatch: true
            })
            .when('/intuition/:id', {
                title: 'Intuition',
                templateUrl: '/templates/intuition',
                controller: 'IntuitionController',
                caseInsensitiveMatch: true
            })
            .when('/dashboard', {
                title: 'Dashboard',
                templateUrl: '/templates/dashboard',
                controller: 'DashboardController'
            })
            .when('/privacy-policy', {
                title: 'Privacy Policy',
                templateUrl: '/templates/privacy-policy',
                caseInsensitiveMatch: true
            })
            .when('/terms-of-service', {
                title: 'Terms of Service',
                templateUrl: '/templates/terms-of-service',
                caseInsensitiveMatch: true
            })
            .when('/score-history', {
                title: 'Score History',
                templateUrl: '/templates/score-history',
                controller: 'ScoreHistoryController',
                caseInsensitiveMatch: true
            })
            .when('/r/:referralCode', {
                title: 'Social Intuition',
                templateUrl: '/templates/progress',
                controller: 'ReferralController',
                caseInsensitiveMatch: true
            })
            .when('/404', {
                title: 'Page Not Found',
                templateUrl: '/templates/404'
            })
            .otherwise({
                title: 'Page Not Found',
                templateUrl: 'html/404.html'
            });
    })

    .run(function($rootScope, $templateCache, $location, utilityService, userService) {
        $rootScope.title = 'Social Intuition'; // default to this for initial page rendering
        $rootScope.path = utilityService.path;

        $rootScope.$on('$routeChangeStart', function (event, next, current) {
            if (next.templateUrl === '/templates/home') {
                // need to clear the browser cache, otherwise it will always return welcome home
                next.templateUrl = next.templateUrl + '?' + new Date().getTime();
            }
        });

        $rootScope.$on('$routeChangeSuccess', function (event, current, previous) {
            // CLEAR CACHING AS THE RESPONSE FROM THE SERVER FOR FETCHING THESE WILL VARY BASED ON THE USER'S SESSION
            $templateCache.remove('/templates/home');
            $templateCache.remove('/templates/profile');

            if (current) { // could be undefined if coming from an ajax request (I think)
                $rootScope.title = current.$$route.title;
                $rootScope.isHomeWelcomePage = utilityService.isHomeWelcomePage();
            }
            var newUrl = $location.path();
            ga('set', 'page', newUrl);
            ga('send', 'pageview');
        });

        $rootScope.$on('$viewContentLoaded', function() {
            //$templateCache.removeAll();
        });

        // load new intuition default settings
        utilityService.loadNewIntuitionSettings();
        utilityService.loadClientSideConfig();

        // load user for frontend state
        userService.loadUser();

        // load root scope references to functions
        $rootScope.logout = function () {
            var callback = function (response) {
                window.location = '/';
            };
            userService.logout(callback);
        };

        // scroll from header element to element in page
        function scrollToElement(elSelector, duration) {
            var el = $(elSelector);
            if (el.offset()) {
                $('html, body').animate({
                    scrollTop: el.offset().top - 50
                }, duration);
            }
        }
        $rootScope.scrollTo = function (elSelector) {
            scrollToElement(elSelector, 1500);
        };
        $rootScope.scrollTop = function () {
            scrollToElement('.home', 500);
        };
    });

})();

