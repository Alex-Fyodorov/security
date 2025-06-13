angular.module('sessionsFront', []).controller('sessionController', function ($scope, $http) {
    const contextPath = 'http://localhost:8337/api/v1/sessions';

 //Загрузка списка сессий
    $scope.loadSessions = function () {
        $http({
            url: contextPath,
            method: 'get',
            params: {
                page: $scope.loadSessions ? $scope.loadSessions.page : 1,
                sort: $scope.loadSessions ? $scope.loadSessions.sort : null,
                user_id: $scope.loadSessions ? $scope.loadSessions.user_id : null,
                min_login_time: $scope.loadSessions ? $scope.loadSessions.min_login_time : null,
                method: $scope.loadSessions ? $scope.loadSessions.method : null,
                is_active: $scope.loadSessions ? $scope.loadSessions.is_active : null
            }
        }).then(function(response) {
            console.log(response.data);
            $scope.SessionsList = response.data.content;
            $scope.totalPages = response.data.totalPages;
        });
    };


    $scope.loadSessions2 = function () {
//        let str = $scope.loadSessions.min_login_time;
//        str = str.substring(0, str.length - 1);
//        console.log('str1 = ' + str);
//        $scope.loadSessions.min_login_time = str;
//        console.log('str2 = ' + $scope.loadSessions.min_login_time);
        $scope.loadSessions();
    }



// Переключение страниц
    $scope.page = function (p) {
    $scope.loadSessions.page = p;
    $scope.loadSessions();
    };

// Обнуление фильтров
    $scope.resetFilter = function () {
        $scope.loadSessions.user_id = null;
        $scope.loadSessions.min_login_time = null;
        $scope.loadSessions.method = null;
        $scope.loadSessions.is_active = null;
        $scope.loadSessions();
    };

// Сортировка
    $scope.sort = function (s) {
        $scope.loadSessions.sort = s;
        $scope.loadSessions();
    };

// Закрыть сессию
    $scope.closeSession = function (sessionId) {
        $http.get(contextPath + '/logout/' + sessionId)
            .then(function successCallback(response) {
                console.log(response.data);
                $scope.loadSessions();
            }
            , function errorCallback(response) {
                console.log(response.data);
                alert(response.data.statusCode + "\n" + response.data.message)
            });
    };

// Создание новой сесии
    $scope.createSession = function () {
        $http.post(contextPath, $scope.newSession)
            .then(function successCallback(response) {
                console.log(response.data);
                $scope.loadSessions();
            }
            , function errorCallback(response) {
                console.log(response.data);
                alert(response.data.statusCode + "\n" + response.data.message)
            });
    };

// Обнуление формы
    $scope.resetNewSession = function () {
        $scope.newSession.userId = null;
        $scope.newSession.deviceInfo = null;
        $scope.newSession.method = null;
        $scope.newSession.ipAddress = null;
    };

    $scope.loadSessions();
});