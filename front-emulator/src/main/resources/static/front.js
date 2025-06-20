var marketApp = angular.module('front', ['ngRoute']);

marketApp.config(function($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl : 'welcome/welcome.html',
            controller  : 'welcomeController'
        })
        .when('/android', {
            templateUrl: 'android/android.html',
            controller: 'emulatorController'
        })
        .when('/session', {
            templateUrl: 'session_tracing_DB/sessions.html',
            controller: 'sessionController'
        })
        .when('/redis', {
            templateUrl: 'session_tracing_Redis/redis.html',
            controller: 'redisController'
        })
        .otherwise({
            redirectTo: '/'
        });
});

marketApp.controller('frontController', function ($scope, $http) {
    const contextPathCore = 'http://localhost:5555/core/api/v1';
    const contextPathCart = 'http://localhost:5555/cart/api/v1/current-cart';


});