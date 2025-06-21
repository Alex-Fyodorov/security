angular.module('front').controller('redisController', function ($scope, $http) {
    const contextPath = 'http://localhost:8193/android-emulator/api/v1/session';

// Загрузка списка сессий
    $scope.loadSessions = function () {
        $http.get(contextPath + '/active')
            .then(function(response) {
                console.log(response.data);
                $scope.SessionsList = response.data;
            });
    };

// Форматирование времени для таблицы
    $scope.timeFormat = function (inTime) {
        if (inTime == null) return '';
        const date = new Date(inTime);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const time = date.toLocaleTimeString();
        return `${time} / ${day}.${month}.${year}`;
    }

// Закрыть сессию
    $scope.closeSession = function (userId, deviceInfo) {
        $http({
            url: contextPath + '/logout',
            method: 'get',
            params: {
                user_id: userId,
                device_info: deviceInfo
            }
        })
            .then(function successCallback(response) {
                console.log(response.data);
                $scope.loadSessions();
            }
            , function errorCallback(response) {
                console.log(response.data);
                alert(response.data.statusCode + "\n" + response.data.message)
            });
    };

    $scope.loadSessions();
});