"use strict";

    var siAppControllers = angular.module('siApp.controllers', [
        'siApp.services'
    ]);

    siAppControllers.controller('HomeController', ['$rootScope', '$scope', 'utilityService', function ($rootScope, $scope, utilityService) {
        $scope.share = {};

        var s3Prefix = "https://d3e9sldr572y3v.cloudfront.net/minified-cropped/";
        var imagesToLoad = [
            "skater_mini.jpg", "cool-girl_mini.jpg", "kid-soccer-game_mini.jpg", "skating-friends_mini.jpg", "sunset-jam-session_mini.jpg"
        ];
        var md = new MobileDetect(window.navigator.userAgent);
        if (md.mobile() == null) { // if desktop then load these extra
            imagesToLoad = imagesToLoad.concat([
                "city-street_mini.jpg",
                "blondes-whispering_mini.jpg",
                "cross_mini.jpg", "football-game_mini.jpg",
                "girl-in-sun_mini.jpg",
                "snow-boarding_mini.jpg", "teen-pool-party_mini.jpg",
                "kid-with-depression_mini.jpg", "girl-on-bike_mini.jpg",
                "usa-pony-tail-girl_mini.jpg", "girls-at-pier_mini.jpg",
                "concert-white-light_mini.jpg", "love-in-back-seat_mini.jpg",
                "mac-and-coffee_mini.jpg", "man-in-suit_mini.jpg",
                "piggy-back-couple_mini.jpg", "punk-girl_mini.jpg",
                "romance-couple_mini.jpg", "restaurant-long-table-with-people_mini.jpg",
                "bulldog_mini.jpg", "concert_mini.jpg",
                "train-station_mini.jpg", "coffee_mini.jpg"
            ]);
        }

        imagesToLoad.forEach(function (el, i) {
            imagesToLoad[i] = s3Prefix + el;
        });
        $(".image-backdrop").backstretch(
            imagesToLoad,
            {duration: 3000, fade: 1000, lazyload: true});

        // remove backstretch's setting of z-index
        $('.image-backdrop > .backstretch').css('z-index', '');

        // WOW.JS
        var wow = new WOW({
            boxClass:     'wow',      // default
            animateClass: 'animated', // default
            offset:       0,          // default
            mobile:       false,       // default
            live:         true        // default
        });
        wow.init();

        $('.hiw-captures').owlCarousel({
            items: 1,
            animateIn: 'slideInRight',
            animateOut: 'fadeOutDown',
            loop: true,
            autoplay: true,
            autoplayTimeout: 3000,
            margin: 10
        });

        // See https://cssanimation.rocks/clocks/
        function initLocalClocks() {
            // Get the local time using JS
            var date = new Date;
            var seconds = date.getSeconds();
            var minutes = date.getMinutes();
            var hours = date.getHours();

            // Create an object with each hand and it's angle in degrees
            var hands = [
                {
                    hand: 'hours',
                    angle: (hours * 30) + (minutes / 2)
                },
                {
                    hand: 'minutes',
                    angle: (minutes * 6)
                },
                {
                    hand: 'seconds',
                    angle: (seconds * 6)
                }
            ];
            // Loop through each of these hands to set their angle
            for (var j = 0; j < hands.length; j++) {
                var elements = document.querySelectorAll('.' + hands[j].hand);
                for (var k = 0; k < elements.length; k++) {
                    elements[k].style.webkitTransform = 'rotateZ('+ hands[j].angle +'deg)';
                    elements[k].style.transform = 'rotateZ('+ hands[j].angle +'deg)';
                    // If this is a minute hand, note the seconds position (to calculate minute position later)
                    if (hands[j].hand === 'minutes') {
                        elements[k].parentNode.setAttribute('data-second-angle', hands[j + 1].angle);
                    }
                }
            }
        }
        initLocalClocks();

        var outcomesPrefix = 'https://d3e9sldr572y3v.cloudfront.net/outcomes/';
        var outcomesImgs = [
            { href: 'ava-party.png', title: '"What night should the Christmas party be?"'},
            { href: 'connor-bears.png', title: '"The Bears will make it to Super Bowl 2016."'},
            { href: 'dylan-nflgame.png', title: '"The Dallas Cowboys will beat the Green Bay Packers this weekend."'},
            { href: 'emma-homecoming.png', title: '"Who will be Homecoming Queen?"'},
            { href: 'ava-prom.png', title: '"Michael will ask me to the prom."'},
            { href: 'mia-test.png', title: '"I earned a A on Professor Stevens exam."'},
            { href: 'ethan-thrones.png', title: 'Game of thrones Bran Stark will return!"'},
            { href: 'hailey-costume-unset.png', title: '"What should be my costume for Halloween?"'},
            { href: 'mia-bachelor.png', title: '"The Bachelor will choose Becca."'},
            { href: 'dylan-nflgame-unset.png', title: '"The Dallas Cowboys will beat the Green Bay Packers this weekend."'},
            { href: 'mia-car.png', title: '"My parents will get me a car for my 16th birthday."'},
            { href: 'hailey-costume.png', title: '"What should be my costume for Halloween?"'},
            { href: 'jacob-moving.png', title: '"My family will be moving at the end of the year."'},
            { href: 'madison-driving.png', title: '"I will pass my driving test on the first try."'},
            { href: 'mia-bears.png', title: '"The Bears will make it to Super Bowl 2016."'},
            { href: 'jacob-minecraft.png', title: '"Who will be the winner of the Minecraft tournament?"'}
        ];
        outcomesImgs.forEach(function (el, idx) {
            el.href = outcomesPrefix + el.href;
        });
        $('#outcomesGallery').click( function( e ) {
            e.preventDefault();
            $.swipebox(outcomesImgs, { hideBarsDelay: 5000, loopAtEnd: true } );
        });

        var pieChartOptions = {
            animate:{
                duration: 3,
                enabled: true
            },
            barColor: function (percent, arg) {
                var retval = '#660000'; // red
                if (percent >= 50) {
                    retval = '#473754'; // theme purple (green)
                } else if (percent >= 25 && percent < 50) {
                    retval = '#f0ad4e'; // yellow
                }
                return retval;
            },
            scaleColor: false,
            lineWidth: 10,
            lineCap: 'round'
        };
        $scope.pieChartOptions = pieChartOptions;
        $scope.feedback = {};
        $scope.isFeedbackSubmitted = false;
        $scope.isFeedbackSubmitting = false;
        $scope.submitFeedback = function () {
            $scope.isFeedbackSubmitting = true;
            utilityService.submitFeedback($scope.feedback, function (response) {
                $scope.isFeedbackSubmitting = false;
                $scope.isFeedbackSubmitted = true;
            });
        };


        $('.swipebox-intuition').swipebox({
            hideCloseButtonOnMobile : false
        });
    }]);

    siAppControllers.controller('SignupController', ['$scope', 'utilityService', function ($scope, utilityService) {
        $scope.share = {
            hasReferral: utilityService.hasReferral(),
            referral: utilityService.referral
        };
    }]);

    siAppControllers.controller('VerificationController', ['$scope', '$location', function ($scope, $location) {
        $scope.verificationTarget = $location.search()['t'];
    }]);

    siAppControllers.controller('LoginController', ['$scope', '$location', '$route', '$templateCache', 'utilityService', 'userService', function ($scope, $location, $route, $templateCache, utilityService, userService) {
        $scope.serverMessage = undefined;
        $scope.user = undefined;
        $scope.progressing = false;
        $scope.progressMessage = undefined;

        $scope.reloadLogin = function () {
            $route.reload();
        };

        function startLogin() {
            $scope.progressMessage = "Logging in...";
            $scope.progressing = true;
        }

        function clearProgressAndMessages() {
            $scope.progressMessage = undefined;
            $scope.progressing = false;
        }

        var loginSuccessCallback = function (response) {
            $templateCache.remove('/templates/home');
            if (response.success) {
                $location.path(response.targetUrl);
            } else {
                $scope.serverMessage = response.message;
            }
            clearProgressAndMessages();
        };

        $scope.loginWithSocialIntuition = function () {
            startLogin();
            userService.loginWithSocialIntuition($scope.user, loginSuccessCallback);
        };

        /*
         * Google init done in normal flow to not be embedded async which is cancelled by popup blockers.
         */
        var auth2 = undefined;
        gapi.load('client:auth2', function() {
            // Retrieve the singleton for the GoogleAuth library and set up the client.
            auth2 = gapi.auth2.init({
                client_id: utilityService.clientConfig.googleClientId,
                cookiepolicy: 'single_host_origin',
                scope: 'profile email',
                fetch_basic_profile: true
            });
        });
        // now normal popup in original user-initiated flow
        $scope.loginWithGoogle = function () {
            startLogin();
            auth2.signIn().then(function (authObject) {
                var googleUser = authObject.getBasicProfile();
                var user = {
                    email: googleUser.getEmail()
                };
                userService.loginWithSocialPlatform(user, loginSuccessCallback);
            }, function (error) {
                $scope.authorizationDenied = true;
                clearProgressAndMessages();
                $scope.$apply();
            });
        };

        $scope.loginWithFacebook = function () {
            startLogin();
            //FB.init({
            //    appId: utilityService.clientConfig.facebookAppId,
            //    cookie: true,
            //    xfbml: true,
            //    status: true,
            //    version: 'v2.5'
            //});
            FB.login(function(response) {
                if (response.authResponse) {
                    FB.api('/me?fields=email', function(facebookUser) {
                        var user = {
                            email: facebookUser.email
                        };
                        userService.loginWithSocialPlatform(user, loginSuccessCallback);
                    });
                } else {
                    $scope.authorizationDenied = true;
                    clearProgressAndMessages();
                    $scope.$apply();
                }
            });
        };

    }]);

    siAppControllers.controller('IntuitionController', ['$scope', '$location', '$routeParams', 'utilityService', 'intuitionService', 'userService',
                                            function ($scope, $location, $routeParams, utilityService, intuitionService, userService) {
        $scope.share = {};
        var successCallback = function (intuitionDto) {
            if (intuitionDto) {
                $scope.intuitionDto = intuitionDto;
                $scope.intuition = intuitionDto.intuition;
                $scope.intuitionLoaded = true;
            } else {
                $location.path('/404');
            }
        };
        intuitionService.getIntuitionById($routeParams.id, successCallback);

        $scope.resetIntuitionDto = function (intuitionDto) {
            successCallback(intuitionDto);
        };

        $scope.isLoggedIn = userService.isLoggedIn();
    }]);

    siAppControllers.controller('DashboardController', ['$scope', '$location', function ($scope, $location) {
        //console.log('DashboardController hit.');
    }]);

    siAppControllers.controller('ScoreHistoryController', ['$scope', 'scoreService', function ($scope, scoreService) {
        // setup and load profile
        var getScoreHistorySuccessCallback = function (response) {
            $scope.scoreDto = response.data;
        };
        scoreService.getScoreHistory(getScoreHistorySuccessCallback);
    }]);

    siAppControllers.controller('ProfileController', ['$scope', '$interval', 'profileService', 'userService', 'utilityService', 'Upload', function ($scope, $interval, profileService, userService, utilityService, Upload) {
        $scope.share = {};
        profileService.init();
        $scope.isOwnProfile = profileService.isOwnProfile();
        $scope.isGuest = utilityService.isGuest();
        $scope.profilePhoto = [];
        $scope.$watch('profilePhoto', function (newValue, oldValue) {
            upload();
        });
        function upload () {
            var profilePhoto = $scope.profilePhoto;
            if (profilePhoto instanceof File) { // need this check to comply with ngFileUpload resetting
                $scope.uploadingProfilePhoto = true;
                Upload.upload({
                    url: '/api/profile/photo',
                    file: profilePhoto
                }).progress(function (evt) {
                    //var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                    //console.log('progress: ' + progressPercentage + '% ' + evt.config.file.name);
                }).success(function (response, status, headers, config) {
                    var user = response.data;
                    userService.setUser(user);
                    $scope.profilePhotoUrl = userService.getCurrentUserImageUrl() + '?ts=' + new Date().getTime();
                });
            }
        }

        // setup profile easy pie chart options
        var pieChartOptions = {
            animate:{
                duration:3,
                enabled:true
            },
            barColor: function (percent, arg) {
                var retval = '#660000'; // red
                if (percent >= 50) {
                    retval = '#473754'; // theme purple (green)
                } else if (percent >= 25 && percent < 50) {
                    retval = '#f0ad4e'; // yellow
                }
                return retval;
            },
            scaleColor:false,
            lineWidth:15,
            lineCap:'round'
        };
        $scope.pieChartTotalOptions = pieChartOptions;
        $scope.pieChartOwnedOptions = angular.copy(pieChartOptions);
        $scope.pieChartOwnedOptions.lineWidth = 10;
        $scope.pieChartCohortOptions = angular.copy(pieChartOptions);
        $scope.pieChartCohortOptions.lineWidth = 10;
        $scope.percentTotal = 0;
        $scope.percentOwned = 0;
        $scope.percentCohort = 0;

        // setup and load profile
        var getProfileSuccessCallback = function (response) {
            var profileDto = response.data;
            var user = profileDto.userDto.user;
            $scope.profileDto = profileDto;
            $scope.user = user;
            $scope.showAddCohort = profileDto.hasSession && !profileDto.owner && !profileDto.userDto.cohort;

            // load profile photo based on perspective
            $scope.profilePhotoUrl = profileDto.owner ? userService.getCurrentUserImageUrl() : userService.getUserImageUrl(user);

            // set score variables
            var scoreDto = profileDto.scoreDto;
            $scope.totalPercent = scoreDto.totalPercent;
            $scope.ownedPercent = scoreDto.ownedPercent;
            $scope.cohortPercent = scoreDto.cohortPercent;
            $scope.scoreDto = scoreDto;

            $('.swipebox-intuition').swipebox({
                hideCloseButtonOnMobile : false
            });
        };
        profileService.getProfile(getProfileSuccessCallback);

        // setup add cohort logic
        var addCohortSuccessCallback = function (response) {
            $scope.isAddingCohort = false;
            $scope.profileDto.cohortRequestSent = true;
        };
        $scope.addCohort = function () {
            $scope.isAddingCohort = true;
            userService.addCohort($scope.user, addCohortSuccessCallback);
        };

        // shared functions
        $scope.$on('outcome.set', function (event, args) {
            profileService.getProfile(getProfileSuccessCallback);
        });
    }]);

    siAppControllers.controller('ReferralController', ['$scope', '$location', '$routeParams', 'utilityService', 'userService',
                                                function ($scope, $location, $routeParams, utilityService, userService) {
        $scope.message = 'Loading...';
        var successCallback = function (response) {
            var referral = response.data;
            if (referral.requiresSession && userService.isNotLoggedIn()) {
                $scope.message = referral.progressMessage;
                if (!referral.guestAllowed) {
                    utilityService.clearReferral();
                    $location.path('signup');
                }
                if (referral.guestAllowed) {
                    var path = referral.targetUrl;
                    if (referral.requestRegistration) {
                        path = 'signup';
                    }
                    $location.path(path);
                }
            } else {
                utilityService.clearReferral();
                $location.path(referral.targetUrl);
            }
        };
        utilityService.fetchReferral($routeParams.referralCode, successCallback);
    }]);

