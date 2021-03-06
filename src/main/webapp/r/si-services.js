"use strict";

    var siAppServices = angular.module('siApp.services', []);

    siAppServices.factory("Register", ['$resource', function($resource) {
        return $resource("/api/register");
    }]);

    siAppServices.service("userService", ['$rootScope', '$http', 'notificationService', function($rootScope, $http, notificationService) {
        var userService = {
            user: undefined,
            loadUserListeners: [],
            isLoadInProcess: false,
            signupWithSocialIntuition: function(user, callback) {
                $http.post('/api/signup?src=siw', user).success(callback);
            },
            signupWithSocialPlatform: function(user, callback) {
                $http.post('/api/signup/social-platform', user).success(callback);
            },
            loginWithSocialIntuition: function (user, callback) {
                userService.login(user, '/api/login', callback);
            },
            loginWithSocialPlatform: function (user, callback) {
                userService.login(user, '/api/login/social-platform', callback);
            },
            login: function (user, endpoint, callback) {
                var callbackHook = function (response) {
                    if (response.success) {
                        userService.user = response.data;
                        notificationService.startNotifications();
                        // set misc variables
                        $rootScope.isLoggedIn = userService.isLoggedIn();
                        $rootScope.username = userService.user.username;
                    }
                    callback(response);
                };
                $http.post(endpoint, user).success(callbackHook);
            },
            logout: function (callback) {
                var callbackHook = function (response) {
                    if (response.success) {
                        notificationService.stopNotifications();
                        userService.user = undefined;
                        $rootScope.isLoggedIn = userService.isLoggedIn();
                        $rootScope.username = undefined;
                    }
                    callback(response);
                };
                $http.post('/api/logout').success(callbackHook);
            },
            loadUser: function () {
                if (!userService.isLoadInProcess) {
                    userService.isLoadInProcess = true;
                    var callbackHook = function (response) {
                        if (response.success) {
                            var user = response.data;
                            //userService.user = user;
                            userService.setUser(user);
                            userService.loadUserListeners.forEach(function (callback) {
                                callback(userService.user);
                            });
                            userService.loadUserListeners = []; // reset
                            // set misc variables
                            $rootScope.isLoggedIn = userService.isLoggedIn();
                            //$rootScope.username = user.username;
                            notificationService.startNotifications();
                            // stop loading
                            userService.isLoadInProcess = false;
                        }
                    };
                    $http.get('/api/user').success(callbackHook);
                }
            },
            getUser: function (callback) {
                if (userService.user) {
                    callback(userService.user);
                } else { // else queue up
                    userService.loadUserListeners.push(callback);
                    userService.loadUser();
                }
            },
            setUser: function (user) {
                userService.user = user;
                $rootScope.username = user.username;
            },
            isLoggedIn: function () {
                return userService.user != undefined;
            },
            isNotLoggedIn: function () {
                return !this.isLoggedIn();
            },
            addCohort: function (user, callback) {
                $http.post('/api/cohort', user).success(callback);
            },
            acceptCohort: function (notification, callback) {
                $http.post('/api/cohort/accept', notification).success(callback);
            },
            ignoreCohort: function (notification, callback) {
                $http.post('/api/cohort/ignore', notification).success(callback);
            },
            getCurrentUserImageUrl: function () {
                var secureUrl = undefined;
                if (this.isLoggedIn()) {
                    secureUrl = userService.getUserImageUrl(this.user);
                }
                return secureUrl;
            },
            getUserImageUrl: function (user) {
                var secureUrl = undefined;
                var imageInfo = user.imageInfo;
                if (imageInfo && imageInfo.secureUrl) {
                    secureUrl = imageInfo.secureUrl;
                }
                return secureUrl;
            },
            checkEmailAvailability: function (email, callback) {
                $http.get('/api/search/email/' + email).success(callback);
            }
        };
        return userService;
    }]);

    siAppServices.service("searchService", ['$http', function($http) {
        var searchService = {
            searchByName: function (name, callback) {
                var successCallback = function (response) {
                    var searchDto = response.data;
                    if (callback) {
                        callback(searchDto);
                    }
                };
                var endpoint = '/api/search/name/' + name;
                return $http.get(endpoint).success(successCallback);
            }
        };
        return searchService;
    }]);

    siAppServices.service("notificationService", ['$http', '$interval', function($http, $interval) {
        var notificationService = {
            notifications: undefined,
            intervalPromise: undefined,
            subscribers: [],
            startNotifications: function () {
                function fetchNotifications() {
                    var successCallback = function (response) {
                        var notificationDtos = response.data;
                        notificationService.notificationDtos = notificationDtos;
                        // notify listeners
                        notificationService.subscribers.forEach(function (subscriberCallback) {
                            subscriberCallback(notificationDtos);
                        });
                    };
                    var endpoint = '/api/notification';
                    return $http.get(endpoint).success(successCallback);
                }
                notificationService.intervalPromise = $interval(fetchNotifications, 5000);
            },
            stopNotifications: function () {
                $interval.cancel(notificationService.intervalPromise);
                notificationService.intervalPromise = undefined;
                notificationService.notifications = undefined;
                notificationService.subscribers = [];
            },
            subscribe: function (subscriberCallback) {
                notificationService.subscribers.push(subscriberCallback);
            },
            notificationHandled: function (notification, callback) {
                $http.post('/api/notification/handled', notification).success(callback);
            }
        };
        return notificationService;
    }]);

    siAppServices.service("utilityService", ['$http', '$location', '$rootScope', 'userService', function($http, $location, $rootScope, userService) {
        var utilityService = {
            clientConfig: undefined,
            newIntuitionSettings: undefined,
            newIntuitionSettingsListeners: [],
            referral: undefined,
            loadNewIntuitionSettings: function () {
                var successCallback = function (response) {
                    utilityService.newIntuitionSettings = response.data;
                    var callback; // loop through and remove listeners, and notify them
                    while (callback = utilityService.newIntuitionSettingsListeners.pop()) {
                        callback(utilityService.newIntuitionSettings);
                    }
                };
                $http.get('/api/intuition/new-intuition-settings').success(successCallback);
            },
            loadClientSideConfig: function () {
                var successCallback = function (response) {
                    utilityService.clientConfig = response.data;
                    utilityService.initializeSocialMedia(utilityService.clientConfig);
                };
                $http.get('/api/fetch-client-config').success(successCallback);
            },
            submitFeedback: function (feedback, callback) {
                var successCallback = function (response) {
                    callback(response);
                };
                $http.post('/api/feedback', feedback).success(successCallback);
            },
            requestNewIntuitionSettings: function (callback) {
                if (utilityService.newIntuitionSettings !== undefined) {
                    callback(utilityService.newIntuitionSettings)
                } else {
                    utilityService.newIntuitionSettingsListeners.push(callback);
                    utilityService.loadNewIntuitionSettings();
                }
            },
            newRequest: function () {
                var request = {
                    data: undefined,
                    intuitionId: undefined,
                    commentId: undefined,
                    isGuest: utilityService.isGuest()
                };
                return request;
            },
            extractUsernameFromPath: function () {
                var path = $location.path();
                var profilePrefixLength = '/profile/'.length;
                var username = path.substr(profilePrefixLength);
                username = username.toLowerCase();
                return username;
            },
            isHomeWelcomePage: function () {
                var retval = false;
                var isRootPage = $location.path() === '/';
                if (isRootPage && userService.isNotLoggedIn()) {
                    retval = true;
                }
                return retval;
            },
            path: function (path) {
                $location.path(path);
            },
            fetchReferral: function (referralCode, callback) {
                var successCallback = function (response) {
                    utilityService.referral = response.data;
                    $rootScope.isGuest = utilityService.isGuest();
                    callback(response);
                };
                var endpoint = '/api/fetch-referral/' + referralCode;
                $http.get(endpoint).success(successCallback);
            },
            hasReferral: function () {
                return utilityService.referral !== undefined;
            },
            clearReferral: function () {
                utilityService.referral = undefined;
            },
            isGuest: function () {
                var retval = false;
                var referral = utilityService.referral;
                if (referral && referral.guestAllowed) {
                    retval = true;
                }
                return retval;
            },
            initializeSocialMedia: function (clientConfig) {
                // initialize FB
                FB.init({
                    appId: clientConfig.facebookAppId,
                    cookie: true,
                    xfbml: true,
                    version: 'v2.5'
                });
            }
        };
        return utilityService;
    }]);

    siAppServices.service("intuitionService", ['$http', 'Upload', 'utilityService', 'userService', function($http, Upload, utilityService, userService) {
        var intuitionService = {
            fetchState: {
                start: 0,
                quantity: 5,
                incrementByQuantity: function () {
                    this.start += this.quantity;
                },
                increment: function () {
                    this.start++;
                }
            },
            intuitionDtos: [],
            hasIntuitions: function () {
                return intuitionService.intuitionDtos.length >= 1;
            },
            hasNoIntuitions: function () {
                return intuitionService.intuitionDtos.length == 0;
            },
            resetIntuitionDto: function (intuitionDto, intuitionDtosArray) {
                var intuitionId = intuitionDto.intuition.id;
                var i = 0;
                for (; i < intuitionDtosArray.length; i++) {
                    var candidateIntuitionId = intuitionDtosArray[i].intuition.id;
                    if (intuitionId === candidateIntuitionId) {
                        intuitionDtosArray[i] = intuitionDto;
                        break;
                    }
                }
            },
            removeIntuitionDto: function (intuitionDto, intuitionDtosArray) {
                var targetIntuitionId = intuitionDto.intuition.id;
                var i = 0;
                for (; i < intuitionDtosArray.length; i++) {
                    if (intuitionDtosArray[i].intuition.id === targetIntuitionId) {
                        break; // found the index
                    }
                }
                return intuitionDtosArray.splice(i, 1);
            },
            addIntuition: function (intuition, intuitionPicture, successCallback) {
                var request = utilityService.newRequest();
                request.data = intuition;
                var addIntuitionSuccessCallback = function (response) {
                    var intuitionDto = response.data;
                    intuitionService.intuitionDtos.unshift(intuitionDto); // push to front of array (reverse chronological)
                    intuitionService.fetchState.increment(); // need to adjust start now that one new is added
                    successCallback(intuitionDto);
                };

                Upload.upload({
                    url: '/api/intuition/add',
                    fields: {request: request},
                    file: intuitionPicture
                }).progress(function (evt) {
                    //var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                    //console.log('progress: ' + progressPercentage + '% ' + evt.config.file.name);
                }).success(function (response, status, headers, config) {
                    addIntuitionSuccessCallback(response);
                });
            },
            removeIntuition: function (intuitionDto, successCallback) {
                var request = utilityService.newRequest();
                request.data = intuitionDto.intuition;
                var removeIntuitionSuccessCallback = function (response) {
                    intuitionService.removeIntuitionDto(intuitionDto, intuitionService.intuitionDtos);
                    successCallback(intuitionDto);
                };
                $http.post('/api/intuition/remove', request).success(removeIntuitionSuccessCallback);
            },
            loadMoreIntuitions: function (successCallback) {
                var loadIntuitionDtosSuccessCallback = function (response) {
                    var newIntuitionDtos = response.data;
                    // push onto the bottom or end of the array since the load is reverse chronological
                    intuitionService.intuitionDtos = intuitionService.intuitionDtos.concat(newIntuitionDtos);
                    intuitionService.fetchState.incrementByQuantity();
                    successCallback(newIntuitionDtos);
                };
                var endpoint = '/api/intuition/fetch/activity';
                endpoint += '?start=' + intuitionService.fetchState.start;
                endpoint += '&quantity=' + intuitionService.fetchState.quantity;
                $http.get(endpoint).success(loadIntuitionDtosSuccessCallback);
            },
            getIntuitions: function (successCallback) {
                if (intuitionService.hasIntuitions()) {
                    successCallback(intuitionService.intuitionDtos);
                } else {
                    // if not yet loaded, then fetch from server
                    intuitionService.loadMoreIntuitions(successCallback);
                }
            },
            setOutcome: function (outcome, intuitionId, successCallback) {
                var request = utilityService.newRequest();
                request.data = outcome;
                request.intuitionId = intuitionId;
                var successCallbackHook = function (response) {
                    var intuitionDto = response.data;
                    intuitionService.resetIntuitionDto(intuitionDto, intuitionService.intuitionDtos);
                    successCallback(intuitionDto);
                };
                $http.post('/api/intuition/outcome', request).success(successCallbackHook);
            },
            voteForOutcome: function (outcome, intuitionId, successCallback) {
                var request = utilityService.newRequest();
                request.data = outcome;
                request.intuitionId = intuitionId;
                var successCallbackHook = function (response) {
                    var intuitionDto = response.data;
                    intuitionService.resetIntuitionDto(intuitionDto, intuitionService.intuitionDtos);
                    successCallback(intuitionDto);
                };
                $http.post('/api/intuition/predicted-outcome/cohort-vote', request).success(successCallbackHook);
            },
            addOutcome: function (predictionText, intuitionId, successCallback) {
                var request = utilityService.newRequest();
                request.data = predictionText;
                request.intuitionId = intuitionId;
                var successCallbackHook = function (response) {
                    var intuitionDto = response.data;
                    intuitionService.resetIntuitionDto(intuitionDto, intuitionService.intuitionDtos);
                    successCallback(intuitionDto);
                };
                $http.post('/api/intuition/predicted-outcome', request).success(successCallbackHook);
            },
            getIntuitionById: function (intuitionId, successCallback) {
                var successCallbackHook = function (response) {
                    var intuitionDto = response.data;
                    successCallback(intuitionDto);
                };
                var endpoint = '/api/intuition/' + intuitionId;
                endpoint += '?isGuest=' + utilityService.isGuest();
                $http.get(endpoint).success(successCallbackHook);
            }
        };
        return intuitionService;
    }]);

    siAppServices.service("socialBarService", ['$http', 'intuitionService', function($http, intuitionService) {
        function ServiceCallbackHook(successCallback) {
            this.directiveSuccessCallback = successCallback;
        }

        ServiceCallbackHook.prototype.handleSuccessCallback = function (response) {
            var intuitionDto = response.data;
            intuitionService.resetIntuitionDto(intuitionDto, intuitionService.intuitionDtos);
            this.directiveSuccessCallback(intuitionDto);
        };

        var socialBarService = {
            addIntuitionLike: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/like', request).success(callbackHook);
            },
            removeIntuitionLike: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/like/remove', request).success(callbackHook);
            },
            addIntuitionComment: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/comment', request).success(callbackHook);
            },
            removeIntuitionComment: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/comment/remove', request).success(callbackHook);
            },
            addIntuitionCommentLike: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/comment/like', request).success(callbackHook);
            },
            removeIntuitionCommentLike: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/comment/like/remove', request).success(callbackHook);
            },
            addOutcomeLike: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/outcome/like', request).success(callbackHook);
            },
            removeOutcomeLike: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/outcome/like/remove', request).success(callbackHook);
            },
            addOutcomeComment: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/outcome/comment', request).success(callbackHook);
            },
            removeOutcomeComment: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/outcome/comment/remove', request).success(callbackHook);
            },
            addOutcomeCommentLike: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/outcome/comment/like', request).success(callbackHook);
            },
            removeOutcomeCommentLike: function (request, successCallback) {
                var serviceCallbackHook = new ServiceCallbackHook(successCallback);
                var callbackHook = function (response) {
                    serviceCallbackHook.handleSuccessCallback(response);
                };
                $http.post('/api/intuition/outcome/comment/like/remove', request).success(callbackHook);
            }
        };

        return socialBarService;
    }]);

    siAppServices.service("profileService", ['$http', '$location', 'intuitionService', 'utilityService', 'userService',
                                        function($http, $location, intuitionService, utilityService, userService) {
        var profileService = {
            fetchState: undefined,
            resetFetchState: function () {
                this.fetchState = {
                    start: 0,
                    quantity: 5,
                    incrementByQuantity: function () {
                        this.start += this.quantity;
                    },
                    increment: function () {
                        this.start++;
                    }
                }
            },
            init: function () {
                profileService.resetFetchState();
            },
            addIntuition: function (intuition, successCallback) {
                var addIntuitionSuccessCallback = function (intuitionDto) {
                    this.fetchState.increment(); // need to adjust start now that one new is added
                    successCallback(intuitionDto);
                };
                intuitionService.addIntuition(intuition, addIntuitionSuccessCallback);
            },
            removeIntuition: function (intuitionDto, successCallback) {
                var removeIntuitionSuccessCallback = function (intuitionDto) {
                    successCallback(intuitionDto);
                };
                intuitionService.removeIntuition(intuitionDto, removeIntuitionSuccessCallback);
            },
            loadMoreIntuitions: function (successCallback) {
                var loadIntuitionDtosSuccessCallback = function (response) {
                    profileService.fetchState.incrementByQuantity();
                    var newIntuitionDtos = response.data;
                    successCallback(newIntuitionDtos);
                };
                var endpoint = '/api/intuition/fetch/profile';
                endpoint += '/' + utilityService.extractUsernameFromPath();
                endpoint += '?start=' + profileService.fetchState.start;
                endpoint += '&quantity=' + profileService.fetchState.quantity;
                endpoint += '&isGuest=' + utilityService.isGuest();
                $http.get(endpoint).success(loadIntuitionDtosSuccessCallback);
            },
            setOutcome: function (outcome, intuitionId, successCallback) {
                var successCallbackHook = function (intuitionDto) {
                    successCallback(intuitionDto);
                };
                intuitionService.setOutcome(outcome,  intuitionId, successCallbackHook);
            },
            getProfile: function (successCallback) {
                var endpoint = '/api/profile/' + utilityService.extractUsernameFromPath();
                $http.get(endpoint).success(successCallback);
            },
            isOwnProfile: function () {
                var isOwnProfile = false;
                if (userService.isLoggedIn()) {
                    var thisUsername = userService.user.username;
                    var profileUsername = utilityService.extractUsernameFromPath();
                    if (thisUsername === profileUsername) {
                        isOwnProfile = true;
                    }
                }
                return isOwnProfile;
            }
        };

        profileService.init();

        return profileService;
    }]);

    siAppServices.service("scoreService", ['$http', function($http) {
        var scoreService = {
            getScoreHistory: function (successCallback) {
                $http.get('/api/score-history').success(successCallback);
            }
        };
        return scoreService;
    }]);
