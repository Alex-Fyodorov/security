angular.module('sessionsFront', []).controller('sessionController', function ($scope, $http) {
    const contextPath = 'http://localhost:8337/api/v1/sessions';

// Загрузка списка сессий
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

// Подготовка к загрузке списка сессий
    $scope.prepareToLoad = function () {
        if ($scope.loadSessions.method == "all") {
            delete $scope.loadSessions.method;
        }
        if ($scope.loadSessions.is_active == "all") {
            delete $scope.loadSessions.is_active;
        }
        const date = new Date($scope.minLoginTime);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const time = date.toLocaleTimeString();
        $scope.loadSessions.min_login_time = `${year}-${month}-${day}T${time}`;
        $scope.loadSessions();
    }

// Форматирование времени для таблицы
    $scope.timeFormat = function (inTime) {
        const date = new Date(inTime);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const time = date.toLocaleTimeString();
        return `${time} / ${day}.${month}.${year}`;
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
        $scope.minLoginTime = null;
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