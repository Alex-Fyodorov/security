angular.module('front').controller('emulatorController', function ($scope, $http) {
    const contextPath = 'http://localhost:8193/android-emulator/api/v1/biometric';

    $scope.registration = function () {
        $http.post(contextPath + '/registration', $scope.registrationRequest)
            .then(function successCallback(response) {
                console.log(response.data);
                alert(response.data.otp);
            }
            , function errorCallback(response) {
                $scope.registrationRequest = null;
                console.log(response.data);
                alert(response.data.statusCode + "\n" + response.data.message)
            });
    }

    $scope.smsCode = function () {
        $http.post(contextPath + '/sms', $scope.registrationRequest)
            .then(function successCallback(response) {
                $scope.registrationRequest = null;
                console.log(response.data);
                alert(
                    "id: " + response.data.id + "\n" +
                    "userId: " + response.data.userId + "\n" +
                    "last used: " + response.data.lastUsed + "\n" +
                    "devices: " + response.data.devices.length
                );
            }
            , function errorCallback(response) {
                $scope.registrationRequest = null;
                console.log(response.data);
                alert(response.data.statusCode + "\n" + response.data.message)
            });
    }

//    $scope.smsCode = function () {
//        $http({
//            url:contextPath + '/sms',
//            method: 'get',
//            params: {
//                sms: $scope.smsResponse
//            }
//        }).then(function (response) {
//            $scope.smsResponse = null;
//        });
//    }

    $scope.auth = function () {
        $http.post(contextPath + '/auth', $scope.authRequest)
            .then(function successCallback(response) {
                $scope.authRequest = null;
                console.log(response.data);
                alert("Токен: " + response.data.jwt);
            }
            , function errorCallback(response) {
                $scope.authRequest = null;
                console.log(response.data);
                alert(response.data.statusCode + "\n" + response.data.message)
            });
    }

    $scope.checkUser = function () {
        $http.get(contextPath + '/check/' + $scope.userId)
            .then(function successCallback(response) {
                $scope.userId = null;
                console.log(response.data);
                alert(
                    "id: " + response.data.id + "\n" +
                    "userId: " + response.data.userId + "\n" +
                    "last used: " + response.data.lastUsed + "\n" +
                    "devices: " + response.data.devices.length
                );
            }
            , function errorCallback(response) {
                $scope.userId = null;
                console.log(response.data);
                alert(response.data.statusCode + "\n" + response.data.message)
            });
    }

    $scope.changeDeviceStatus = function () {
        $http.post(contextPath + '/device_status', $scope.changeStatus)
            .then(function successCallback(response) {
                $scope.changeStatus = null;
                console.log(response.data);
                alert(
                    "id: " + response.data.id + "\n" +
                    "accountId: " + response.data.accountId + "\n" +
                    "device info: " + response.data.deviceInfo + "\n" +
                    "biometric enabled: " + response.data.biometricEnabled
                );
            }
            , function errorCallback(response) {
                $scope.changeStatus = null;
                console.log(response.data);
                alert(response.data.statusCode + "\n" + response.data.message)
            });
    }
});