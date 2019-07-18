"use strict";

    var siAppDirectives = angular.module('siApp.directives', []);

    siAppDirectives.directive('feed', function () {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/feed.html',
            scope: {
                type: '=',
                share: '='
            },
            controller: function ($scope, $timeout, intuitionService, profileService) {
                var type = $scope.type;
                var share = $scope.share;
                $scope.hasIntuitions = false;
                $scope.intuitionDtos = [];
                $scope.initialLoad = true;

                function setHasIntuitions () {
                    $scope.hasIntuitions = $scope.intuitionDtos.length >= 1;
                }

                var successCallback = function (newIntuitionDtos) {
                    $scope.intuitionDtos = $scope.intuitionDtos.concat(newIntuitionDtos);
                    $scope.loadingFeed = false;
                    setHasIntuitions();
                };

                function prepareActivityFeed() {
                    $scope.loadIntuitions = function () {
                        $scope.loadingFeed = true;
                        if ($scope.initialLoad && intuitionService.hasIntuitions()) {
                            // if route change onto home and there are intuitions, then retrieve those
                            intuitionService.getIntuitions(successCallback);
                        } else {
                            // otherwise go fetch more from server
                            intuitionService.loadMoreIntuitions(successCallback);
                        }
                        $scope.initialLoad = false;
                    };
                }

                function prepareProfileFeed() {
                    $scope.loadIntuitions = function () {
                        $scope.loadingFeed = true;
                        profileService.loadMoreIntuitions(successCallback);
                        $scope.initialLoad = false;
                    };
                }

                switch (type) {
                    case 'activity':   prepareActivityFeed();    break;
                    case 'profile':    prepareProfileFeed();      break;
                    default: throw new Error("Unexpected feed type: " + type);
                }

                share.addIntuitionDto = function (intuitionDto) {
                    $scope.addIntuitionLoaded = true; // for styling
                    $timeout(function () {
                        $scope.addIntuitionLoaded = false;
                    }, 1000);

                    $timeout(function () {
                        $scope.addIntuitionLoading = false;
                    }, 3000);

                    $scope.intuitionDtos.unshift(intuitionDto); // add to top of list
                    setHasIntuitions();
                };

                $scope.removeIntuitionDto = function (intuitionDto) {
                    intuitionService.removeIntuitionDto(intuitionDto, $scope.intuitionDtos);
                    setHasIntuitions();
                };

                $scope.resetIntuitionDto = function (intuitionDto) {
                    intuitionService.resetIntuitionDto(intuitionDto, $scope.intuitionDtos);
                };

                share.setNgClass = function(expressionName, value) {
                    $scope[expressionName] = value;
                };
            }
        };
    });

    siAppDirectives.directive('intuition', function () {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/intuition.html',
            scope: {
                intuitionDto: '=',
                share: '=',
                resetIntuitionDto: '=',
                removeIntuitionDto: '='
            },
            controller: function ($scope) {
                var intuitionDto = $scope.intuitionDto;
                var intuition = intuitionDto.intuition;
                $scope.intuition = intuition;
                $scope.share = $scope.share;
            }
        };
    });

    siAppDirectives.directive('intuitionHeader', ['intuitionService', function (intuitionService) {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/intuition-header.html',
            scope: {
                intuitionDto: '=',
                removeIntuitionDto: '='
            },
            controller: function ($scope) {
                $scope.intuition = $scope.intuitionDto.intuition;
                $scope.isRemoving = false;
                $scope.hideDelete = true;

                $scope.toggleHideDelete = function () {
                    $scope.hideDelete = !$scope.hideDelete;
                };

                $scope.removeIntuition = function () {
                    $scope.isRemoving = true;
                    var successCallback = function (intuitionDto) {
                        $scope.isRemoving = false;
                        $scope.removeIntuitionDto(intuitionDto);
                    };
                    intuitionService.removeIntuition($scope.intuitionDto, successCallback);
                };
            }
        };
    }]);

    siAppDirectives.directive('prediction', ['intuitionService', function (intuitionService) {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/prediction.html',
            scope: {
                intuitionDto: '=',
                resetIntuitionDto: '='
            },
            controller: function ($scope, $filter) {
                var intuitionDto = $scope.intuitionDto;
                var intuition = intuitionDto.intuition;
                $scope.intuition = intuition;

                $scope.setChoiceLetter = function (value, predicted) {
                    if (predicted) {
                        $scope.choiceLetter = $filter('numberToLetter')(value);
                    }
                };

                var successCallback = function (intuitionDto) {
                    $scope.voteLoading = false;
                    $scope.resetIntuitionDto(intuitionDto);
                };

                $scope.voteForOutcome = function (outcome) {
                    $scope.voteLoading = true;
                    intuitionService.voteForOutcome(outcome, intuition.id, successCallback);
                };

                $scope.addOutcome = function () {
                    var addOutcomeSuccessCallback = function (intuitionDto) {
                        $scope.contributedOutcomeText = undefined;
                        successCallback(intuitionDto);
                    };
                    intuitionService.addOutcome($scope.contributedOutcomeText, intuition.id, addOutcomeSuccessCallback);
                }
            }
        };
    }]);

    siAppDirectives.directive('outcome', function () {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/outcome.html',
            scope: {
                intuitionDto: '=',
                resetIntuitionDto: '='
            },
            controller: function ($scope, $filter) {
                $scope.intuition = $scope.intuitionDto.intuition;

                $scope.setChoiceLetter = function (value, predicted) {
                    if (predicted) {
                        $scope.choiceLetter = $filter('numberToLetter')(value);
                    }
                };
            }
        };
    });

    siAppDirectives.directive('controlBar', ['intuitionService', function (intuitionService) {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/control-bar.html',
            scope: {
                intuitionDto: '=',
                resetIntuitionDto: '='
            },
            controller: function ($rootScope, $scope) {
                var intuitionDto = $scope.intuitionDto;
                $scope.intuition = intuitionDto.intuition;
                $scope.isSettingOutcome = false;
                $scope.outcomeNotSelected = true;
                $scope.allowSetOutcome = intuitionDto.active && intuitionDto.owner;

                collapseAll();

                function collapseAll() {
                    $scope.hideSetOutcome = true;
                }

                $scope.showSetOutcome = function () {
                    $scope.hideSetOutcome = !$scope.hideSetOutcome;
                };

                $scope.setOutcome = function () {
                    $scope.isSettingOutcome = true;
                    var successCallback = function (intuitionDto) {
                        $scope.isSettingOutcome = false;
                        $scope.resetIntuitionDto(intuitionDto);

                        $rootScope.$broadcast('outcome.set');
                    };
                    var outcome = $scope.outcomePredictionChoice;
                    var intuitionId = $scope.intuition.id;
                    intuitionService.setOutcome(outcome, intuitionId, successCallback);
                };

                $scope.cancelOutcome = function () {
                    $scope.outcomePredictionChoice = undefined;
                    $scope.hideSetOutcome = true;
                    $scope.outcomeNotSelected = true;
                };
            }
        };
    }]);

    siAppDirectives.directive('intuitionFooter', ['utilityService', function () {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/intuition-footer.html',
            scope: {
                intuitionDto: '=',
                share: '='
            },
            controller: function ($scope, utilityService) {
                var intuitionDto = $scope.intuitionDto;
                var intuition = intuitionDto.intuition;
                $scope.intuition = intuition;
                intuitionDto.isFacebookShared = false;
                var siteDomainOverride = utilityService.clientConfig.siteDomainOverride;

                $scope.share.shareFacebook = function (intuition) {
                    console.log('app id = ' + utilityService.clientConfig.facebookAppId);
                    FB.ui({
                        method: 'share',
                        href: siteDomainOverride + '/intuition/' + intuition.id
                    }, function(response){
                        console.log(response);
                        intuitionDto.isFacebookShared = true;
                    });
                };

                //var url = siteDomainOverride + '/intuition/' + intuition.id;
                var url = siteDomainOverride + '/share/twitter/' + intuition.id;
                intuitionDto.tweetIntent =
                    'https://twitter.com/intent/tweet?' +
                    'text=' + encodeURIComponent(intuition.intuitionText) +
                    '&url=' + encodeURIComponent(url) +
                    '&via=socintuition' +
                    '&hashtags=socialintuition,socintuition';
                //console.log(intuitionDto.tweetIntent);
            }
        };
    }]);


    siAppDirectives.directive('popover', function () {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/popover.html',
            transclude: true,
            scope: {
                template: '=',
                data: '='
            },
            controller: function ($scope) {
                $('[data-toggle="popover"]').popover({
                    html: true,
                    content: function () {
                        return $('#popover_content_wrapper').html();
                    }
                });
            }
        };
    });

    siAppDirectives.directive('cohortListPopover', ['$compile', function ($compile) {
        var compile = $compile;
        var link = function (scope, el, attr) {
            var $compile = compile;
            var scope = scope;
            $('[data-toggle="popover"]').popover({
                html: true,
                content: function () {
                    var origScope = scope;
                    return $('#' + origScope.popoverId).html();
                }
            });
        };
        return {
            restrict: 'E',
            templateUrl: 'html/partials/cohort-list-popover.html',
            transclude: true,
            scope: {
                parent: '=',
                level: '=',
                type: '=',
                commentDto: '=',
                placement: '='
            },
            controller: function ($scope, $compile, utilityService) {
                var parentDto = $scope.parent;
                var parentBase;
                switch ($scope.level) {
                    case 'intuition':       parentBase = parentDto.intuition;   break;
                    case 'outcome':         parentBase = parentDto.outcome;     break;
                    case 'outcomeVotes':    parentBase = parentDto.outcome;     break;
                }

                var typeParent;
                var type = $scope.type;
                switch (type) {
                    case 'likes':           typeParent = parentBase.likes;                  break;
                    case 'commentLikes':    typeParent = $scope.commentDto.comment.likes;   break;
                    case 'outcomeVotes':    typeParent = parentBase.outcomeVoters;          break;
                    default: throw new Error("Unrecognized scope type (e.g. like, commentLikes, outcomeVotes)");
                }
                $scope.totalCount = typeParent.length;
                $scope.typeParent = [];
                var guestCount = 0;
                typeParent.forEach(function (object) {
                    // extract user object from the object  2
                    var user = undefined;
                    if (type === 'likes' || type === 'commentLikes') {
                        user = object.user;
                    } else if (type === 'outcomeVotes') {
                        user = object;
                    }
                    // now filter depending if guest or not
                    if (!user.guest) {
                        $scope.typeParent.push(user); // only add regular users
                    } else {
                        guestCount++;
                    }
                });
                $scope.guestCount = guestCount;
                $scope.userGuestImageUrl = utilityService.clientConfig.userGuestImageUrl;

                /*
                 * BUILD IDS for DATA TOGGLE AND POPOVER
                 */
                var idSuffix = parentBase.id; // init to either intuition or outcome id
                switch (type) {
                    case 'likes':           idSuffix += '_likes';                                           break;
                    case 'commentLikes':    idSuffix += '_commentLikes_' + $scope.commentDto.comment.id;    break;
                    case 'outcomeVotes':    idSuffix += '_outcomeVotes_' + parentBase.id;                   break;
                    default: throw new Error("Unrecognized scope type (e.g. like, commentLikes, outcomeVotes");
                }
                //$scope.dataToggleId = 'data-toggle_' + idSuffix;
                $scope.popoverId = 'popover_' + idSuffix;
                if (!$scope.placement) {
                    $scope.placement = 'right';
                }

            },
            link: link
        };
    }]);

    siAppDirectives.directive('notificationPopover', function () {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/notification-popover.html',
            controller: function ($scope, $rootScope, $location, $compile, notificationService, userService) {

                var notificationsSubscriberCallback = function (notificationDtos) {
                    $scope.notificationDtos = notificationDtos;
                };
                notificationService.subscribe(notificationsSubscriberCallback);

                $scope.acceptCohort = function (notification) {
                    var callback = function (response) {
                        console.log('Cohort accepted.')
                    };
                    userService.acceptCohort(notification, callback);
                };
                $scope.ignoreCohort = function (notification) {
                    var callback = function (response) {
                        console.log('Cohort ignored.')
                    };
                    userService.ignoreCohort(notification, callback);
                };
                $scope.goToProfile = function (notification) {
                    var callback = function (response) {
                        console.log('goToProfile to: ' + notification.data)
                    };
                    notificationService.notificationHandled(notification, callback);
                    $location.path('/profile/' + notification.data.consenterUsername);
                };
                $scope.goToUrl = function (notification, url) {
                    var callback = function (response) {
                        console.log('Url gone to: ' + notification.data)
                    };
                    notificationService.notificationHandled(notification, callback);
                    $location.path(url);
                };
                $scope.addIntuition = function (notification) {
                    var callback = function (response) {
                        console.log('Add Intuition handled: ' + notification.data)
                    };
                    notificationService.notificationHandled(notification, callback);
                };
            }
        };
    });

    siAppDirectives.directive('trimCommas', function($timeout) {
        return {
            restrict : 'A',
            compile : function(){
                return {
                    post : function(scope, element, attributes){
                        scope.$evalAsync(function() {
                            var el = element;
                            var text = el.text();
                            text = text.replace(/(\r\n|\n|\r)/gm,"");
                            text = text.replace(/\s+/g, " ");
                            text = text.replace(/ \,/g, ',');
                            el.html(text);
                        });
                    }
                }
            }
        };
    });

    siAppDirectives.directive('socialBar', ['utilityService', 'socialBarService', 'userService', function (utilityService, socialBarService, userService) {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/social-bar.html',
            scope: {
                intuitionDto: '=',
                level: '=', //could be 'intuition' or 'outcome'
                resetIntuitionDto: '='
            },
            controller: function ($scope) {
                var level = $scope.level;
                var parent;
                var listingLimit = 3;
                $scope.likeNameListingLimit = listingLimit;
                $scope.commentListingLimit = listingLimit;

                function setValues(intuitionDto) {
                    $scope.intuition = intuitionDto.intuition;
                    parent = level === 'intuition' ? intuitionDto : intuitionDto.outcomeDto;
                    $scope.parent = parent;
                    if (level === 'outcome') {
                        // transfer flag from intuition level down to outcome
                        parent.canMakeSocialContributions = intuitionDto.canMakeSocialContributions;
                    }

                    $scope.hasLikes = parent.selfLikeDto !== null || parent.likeDtos.length >= 1 || parent.guestLikeDtos.length >= 1;
                    $scope.isGuest = utilityService.isGuest();
                    $scope.userGuestImageUrl = utilityService.clientConfig.userGuestImageUrl;

                    if (parent.guestLikeDtos.length >= 1) {
                        // subtract one for the general Guest count
                        $scope.likeNameListingLimit = listingLimit - 1;
                        $scope.hasGuests = true;
                    }

                }
                setValues($scope.intuitionDto);

                userService.getUser(function (user) {
                    $scope.user = user;
                });

                function buildRequest(commentId) {
                    var request = utilityService.newRequest();
                    request.intuitionId = $scope.intuitionDto.intuition.id;
                    if (commentId) { // commentId is optional
                        request.commentId = commentId;
                    }
                    return request;
                }

                var successCallback = function (intuitionDto) {
                    $scope.likeLoading = false;
                    if ($scope.resetIntuitionDto) {
                        $scope.resetIntuitionDto(intuitionDto);
                        setValues(intuitionDto);
                    }
                };

                function prepareIntuitionLevel() {
                    $scope.addLike = function () {
                        $scope.likeLoading = true;
                        var request = buildRequest();
                        request.data = {}; // an empty Like object to really be built server side
                        socialBarService.addIntuitionLike(request, successCallback);
                    };
                    $scope.removeLike = function () {
                        $scope.likeLoading = true;
                        var request = buildRequest();
                        request.data = $scope.intuitionDto.selfLikeDto.like;
                        socialBarService.removeIntuitionLike(request, successCallback);
                    };
                    $scope.addComment = function () {
                        var request = buildRequest();
                        request.data = $scope.comment;
                        var commentSuccessCallback = function (intuitionDto) {
                            successCallback(intuitionDto);
                            $scope.comment = undefined;
                        };
                        socialBarService.addIntuitionComment(request, commentSuccessCallback);
                    };
                    $scope.removeComment = function (comment) {
                        var request = buildRequest();
                        request.data = comment;
                        socialBarService.removeIntuitionComment(request, successCallback);
                    };
                    $scope.addCommentLike = function (comment) {
                        var request = buildRequest();
                        request.commentId = comment.id;
                        request.data = {}; // an empty Like object to really be built server side
                        socialBarService.addIntuitionCommentLike(request, successCallback);
                    };
                    $scope.removeCommentLike = function (comment, like) {
                        var request = buildRequest();
                        request.commentId = comment.id;
                        request.data = like;
                        socialBarService.removeIntuitionCommentLike(request, successCallback);
                    };
                }

                function prepareOutcomeLevel() {
                    $scope.addLike = function () {
                        $scope.likeLoading = true;
                        var request = buildRequest();
                        request.data = {}; // an empty Like object to really be built server side
                        socialBarService.addOutcomeLike(request, successCallback);
                    };
                    $scope.removeLike = function () {
                        $scope.likeLoading = true;
                        var request = buildRequest();
                        request.data = $scope.intuitionDto.outcomeDto.selfLikeDto.like;
                        socialBarService.removeOutcomeLike(request, successCallback);
                    };
                    $scope.addComment = function () {
                        var request = buildRequest();
                        request.data = $scope.comment;
                        var commentSuccessCallback = function (intuitionDto) {
                            successCallback(intuitionDto);
                            $scope.comment = undefined;
                        };
                        socialBarService.addOutcomeComment(request, commentSuccessCallback);
                    };
                    $scope.removeComment = function (comment) {
                        var request = buildRequest();
                        request.data = comment;
                        socialBarService.removeOutcomeComment(request, successCallback);
                    };
                    $scope.addCommentLike = function (comment) {
                        var request = buildRequest();
                        request.commentId = comment.id;
                        request.data = {}; // an empty Like object to really be built server side
                        socialBarService.addOutcomeCommentLike(request, successCallback);
                    };
                    $scope.removeCommentLike = function (comment, like) {
                        var request = buildRequest();
                        request.commentId = comment.id;
                        request.data = like;
                        socialBarService.removeOutcomeCommentLike(request, successCallback);
                    };
                }

                switch (level) {
                    case 'intuition':   prepareIntuitionLevel();    break;
                    case 'outcome':     prepareOutcomeLevel();      break;
                    default: throw new Error("Unexpected social bar level: " + level);
                }

                $scope.setFocus = function (id) {
                    document.getElementById(id).focus();
                };
                $scope.setCommentLimit = function () {
                    $scope.commentListingLimit = parent.commentDtos.length;
                };
            }
        };
    }]);

    siAppDirectives.directive('siAddIntuition', function () {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/add-intuition.html',
            scope: {
                share: '='
            },
            controller: function ($scope, $timeout, utilityService, intuitionService) {
                // general variables
                var share = $scope.share;
                $scope.intuitionAdded = false;

                function setIntuitionDefaults(newIntuitionSettings) {
                    $scope.intuition = {
                        visibility: newIntuitionSettings.defaultVisibility,
                        scoreIntuition: newIntuitionSettings.scoreIntuition,
                        predictionType: newIntuitionSettings.defaultPredictionType,
                        displayPrediction: newIntuitionSettings.displayPrediction,
                        displayCohortsPredictions: newIntuitionSettings.displayCohortsPredictions,
                        allowCohortsToContributePredictedOutcomes: newIntuitionSettings.allowCohortsToContributePredictedOutcomes,
                        allowPredictedOutcomeVoting: newIntuitionSettings.allowPredictedOutcomeVoting
                    };
                    $scope.activeWindows = newIntuitionSettings.activeWindows;
                    $scope.predictionChoicesTrueFalse = newIntuitionSettings.predictionChoicesTrueFalse;
                    $scope.predictionChoicesYesNo = newIntuitionSettings.predictionChoicesYesNo;
                    $scope.predictionChoicesMultipleChoice = [];
                    $scope.intuitionPicture = undefined;
                };

                var newIntuitionSettingsCallback = function (newIntuitionSettings) {
                    setIntuitionDefaults(newIntuitionSettings);
                };

                $scope.$watch('intuition.picture', function (newValue, oldValue) {
                });

                $scope.multipleChoiceAdd = function () {
                    var newMultipleChoice = {
                        predictionText: $scope.newMultipleChoiceText
                    };
                    $scope.predictionChoicesMultipleChoice.push(newMultipleChoice);
                    $scope.newMultipleChoiceText = undefined;
                };

                $scope.multipleChoiceRemove = function (predictionChoice) {
                    var predictionText = predictionChoice.predictionText;
                    var i = 0;
                    for (; i < $scope.predictionChoicesMultipleChoice.length; i++) {
                        var choice = $scope.predictionChoicesMultipleChoice[i];
                        if (choice.predictionText === predictionChoice.predictionText) {
                            $scope.predictionChoicesMultipleChoice.splice(i, 1);
                            break;
                        }
                    }
                };

                $scope.addIntuition = function () {
                    $scope.intuitionAddSaving = true;

                    share.setNgClass("addIntuitionLoading", true);

                    var successCallback = function (intuitionDto) {
                        $scope.showIntuitionContent = false;
                        $scope.intuitionAddSaving = false;
                        $scope.intuitionAdded = true;
                        setIntuitionDefaults(utilityService.newIntuitionSettings);
                        // alert outer feed page scope of new intuition
                        share.addIntuitionDto(intuitionDto);
                        // reset misc scope values
                        $scope.intuitionPicture = undefined;
                    };

                    var intuition = $scope.intuition;
                    switch (intuition.predictionType) {
                        case 'true-false':
                            intuition.potentialOutcomes = $scope.predictionChoicesTrueFalse;
                            break;
                        case 'yes-no':
                            intuition.potentialOutcomes = $scope.predictionChoicesYesNo;
                            break;
                        case 'multiple-choice':
                            intuition.potentialOutcomes = $scope.predictionChoicesMultipleChoice;
                            break;
                        default: throw new Error('Unknown prediction type.')
                    }

                    intuitionService.addIntuition(intuition, $scope.intuitionPicture, successCallback);
                };

                $scope.resetIntuition = function () {
                    setIntuitionDefaults(utilityService.newIntuitionSettings);
                    $scope.showIntuitionContent = false;
                };

                utilityService.requestNewIntuitionSettings(newIntuitionSettingsCallback);
            }
        };
    });

    siAppDirectives.directive('cohortSearch', function () {
        return {
            restrict: 'E',
            scope: {
                collapsed: '='
            },
            templateUrl: 'html/partials/cohort-search.html',
            transclude: true,
            controller: function ($scope, $location, searchService, userService) {
                $scope.isAddingUser = true;
                $scope.doneAddingUser = false;
                $scope.isRequestingUserLoggedIn = userService.isLoggedIn();

                $scope.getUserResults = function(name) {
                    // reset these with every query
                    $scope.isAddingUser = false;
                    $scope.doneAddingUser = false;
                    // query
                    return searchService.searchByName(name).then(function(response) {
                        var searchDto = response.data.data;
                        $scope.isRequestingUserLoggedIn = searchDto.requestingUserLoggedIn;

                        var returnResults = []; // need to filter client side due to breaking issue
                        searchDto.userResults.forEach(function (userDto) {
                            if (userDto.user.fullName.toLowerCase().indexOf(name.toLowerCase()) != -1) {
                                returnResults.push(userDto)
                            }
                        });
                        return returnResults;
                    });
                };

                $scope.userSelected = function ($item, $model, $label) {
                    var path = '/profile/' + $item.user.username;
                    $location.path(path);
                    $scope.collapsed = true;
                };

                $scope.addCohort = function (user) {
                    var callback = function (response) {
                        $scope.isAddingUser = false;
                        $scope.doneAddingUser = true;
                    };
                    userService.addCohort(user, cohort);
                };
            }
        };
    });

    siAppDirectives.directive('siCheckUsernameAvailability', function($resource) {
        return {
            require: 'ngModel',
            link: function(scope, elm, attrs, ctrl) {
                ctrl.$parsers.unshift(function(username) {
                    if (username && username.length >= 2) {
                        var checkUsernameResource = $resource('/api/search/username/:username');
                        checkUsernameResource.get({username: username}, function (response) {
                            ctrl.$setValidity('notAvailable', response.success);
                        });
                    }
                    return username;
                });
            }
        };
    });

    siAppDirectives.directive('siCheckEmailAvailability', function($resource) {
        return {
            require: 'ngModel',
            link: function(scope, elm, attrs, ctrl) {
                ctrl.$parsers.unshift(function(email) {
                    if (email && email.length >= 5) {
                        var checkEmailResource = $resource('/api/search/email/:email');
                        checkEmailResource.get({email: email}, function (response) {
                            ctrl.$setValidity('notAvailable', response.success);
                        });
                    }
                    return email;
                });
            }
        };
    });

    siAppDirectives.directive('imageonload', function() {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                element.bind('load', function() {
                    scope.uploadingProfilePhoto = false;
                    scope.$apply();
                });
            }
        };
    });


    siAppDirectives.directive('signupSocialMedia', ['userService', 'utilityService', function (userService, utilityService) {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/signup-social-media.html',
            scope: {
                share: '='
            },
            controller: function ($scope, $route) {
                $scope.user = undefined;
                $scope.socialMediaShowChoices = true;
                $scope.doneWithSignup = false;
                $scope.usernameInput = false;
                $scope.usernameNotAvailable = false;
                $scope.emailInput = false;
                $scope.progressing = false;
                $scope.progressMessage = undefined;
                $scope.message = undefined;

                $scope.reloadSignup = function () {
                    $route.reload();
                };

                function startSocialSignup() {
                    $scope.socialMediaShowChoices = false;
                    $scope.progressMessage = "Importing basic information...";
                    $scope.progressing = true;
                }

                function clearProgressAndMessages() {
                    $scope.progressing = false;
                    $scope.progressMessage = undefined;
                    $scope.message = undefined;
                }

                /*
                 * Google init done in normal flow to not be embedded async which is cancelled by popup blockers.
                 */
                var auth2 = undefined;
                gapi.load('client:auth2', function(){
                    // Retrieve the singleton for the GoogleAuth library and set up the client.
                    auth2 = gapi.auth2.init({
                        client_id: utilityService.clientConfig.googleClientId,
                        cookiepolicy: 'single_host_origin',
                        scope: 'profile email',
                        fetch_basic_profile: true
                    });
                });
                // now normal popup in original user-initiated flow
                $scope.registerWithGoogle = function () {
                    startSocialSignup();
                    auth2.signIn().then(function (authObject) {
                        var googleUser = authObject.getBasicProfile();
                        var email = googleUser.getEmail();
                        $scope.email = email;
                        $scope.username = email.substring(0, email.indexOf('@'));
                        $scope.user = {
                            firstName: googleUser.getGivenName(),
                            lastName: googleUser.getFamilyName(),
                            fullName: googleUser.getName(),
                            password: googleUser.getId(),
                            registrationSource: 'google'
                        };
                        // now check email availability to check against re-register
                        userService.checkEmailAvailability(email, function (emailResponse) {
                            if (emailResponse.success) {
                                // if good, now display username input div
                                $scope.usernameInput = true;
                            } else {
                                $scope.emailNotAvailable = true;
                            }
                            clearProgressAndMessages();
                        });
                    }, function (error) {
                        $scope.authorizationDenied = true;
                        clearProgressAndMessages();
                        $scope.$apply();
                    });
                };

                $scope.registerWithFacebook = function () {
                    startSocialSignup();
                    //FB.init({
                    //    appId: utilityService.clientConfig.facebookAppId,
                    //    cookie: true,
                    //    xfbml: true,
                    //    version: 'v2.5'
                    //});
                    FB.login(function(response) {
                        if (response.authResponse) {
                            FB.api('/me?fields=email,name,first_name,last_name,gender,timezone', function(facebookUser) {
                                var email = facebookUser.email;
                                $scope.user = {
                                    firstName: facebookUser.first_name,
                                    lastName: facebookUser.last_name,
                                    fullName: facebookUser.name,
                                    password: facebookUser.id,
                                    registrationSource: 'facebook',
                                    gender: facebookUser.gender,
                                    timezone: facebookUser.timezone
                                };

                                if (email !== undefined) {
                                    $scope.email = email;
                                    $scope.username = email.substring(0, email.indexOf('@'));
                                    // now check email availability to check against re-register
                                    userService.checkEmailAvailability(email, function (emailResponse) {
                                        if (emailResponse.success) {
                                            // if good, now display username input div
                                            $scope.usernameInput = true;
                                        } else {
                                            $scope.emailNotAvailable = true;
                                        }
                                    });
                                }

                                if (email === undefined) {
                                    $scope.usernameInput = true;
                                    $scope.emailInput = true;
                                }

                                clearProgressAndMessages();
                                $scope.$apply();
                            });
                        } else {
                            $scope.authorizationDenied = true;
                            clearProgressAndMessages();
                            $scope.$apply();
                        }
                    });
                };

                $scope.submitRegistrationWithSocialPlatform = function () {
                    $scope.usernameInput = false; // done with username input
                    $scope.emailInput = false;
                    $scope.progressMessage = "Signing up...";
                    $scope.progressing = true;
                    var user = $scope.user;
                    user.username = $scope.username;
                    user.email = $scope.email;
                    var callback = function (response) {
                        if (response.success) {
                            $scope.progressMessage = "Logging in...";
                            user = response.data;
                            userService.loginWithSocialPlatform(user, function (response) {
                                if (response.success) {
                                    $scope.user = response.data;
                                    if (utilityService.hasReferral()) {
                                        // registration came through promo or referral
                                        //$scope.share.referral = utilityService.referral;
                                        utilityService.clearReferral();
                                        $scope.share.hasReferral = false;
                                        $scope.share.showReferral = true;
                                    } else {
                                        // standard registration through front door
                                        $scope.doneWithSignup = true;
                                    }
                                } else {
                                    $scope.errorDuringRegistration = true;
                                }
                                clearProgressAndMessages();
                            });
                        } else {
                            clearProgressAndMessages();
                            $scope.errorDuringRegistration = true;
                        }
                    };
                    userService.signupWithSocialPlatform(user, callback);
                };
            }
        };
    }]);

    siAppDirectives.directive('signupSocialIntuition', ['userService', 'utilityService', function (userService, utilityService) {
        return {
            restrict: 'E',
            templateUrl: 'html/partials/signup-social-intuition.html',
            scope: {
                share: '='
            },
            controller: function ($scope, $route) {
                $scope.progressing = false;
                $scope.progressMessage = undefined;
                $scope.doneWithSignup = false;

                function clearProgressAndMessages() {
                    $scope.progressing = false;
                    $scope.progressMessage = undefined;
                }

                $scope.registerWithSocialIntuition = function () {
                    $scope.progressing = true;
                    $scope.progressMessage = "Signing up...";
                    var callback = function (response) {
                        if (response.success) {
                            $scope.progressMessage = "Logging in...";
                            var user = response.data;
                            userService.loginWithSocialIntuition(user, function (response) {
                                if (response.success) {
                                    $scope.user = response.data;
                                    if (utilityService.hasReferral()) {
                                        // registration came through promo or referral
                                        //$scope.share.referral = utilityService.referral;
                                        utilityService.clearReferral();
                                        $scope.share.hasReferral = false;
                                        $scope.share.showReferral = true;
                                    } else {
                                        // standard registration through front door
                                        $scope.doneWithSignup = true;
                                    }
                                } else {
                                    $scope.errorDuringRegistration = true;
                                }
                                clearProgressAndMessages();
                            });
                        } else {
                            clearProgressAndMessages();
                            $scope.errorDuringRegistration = true;
                        }
                    };
                    userService.signupWithSocialIntuition($scope.user, callback);
                };
            }
        };
    }]);
