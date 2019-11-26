var app = angular.module('HymnApp', []);

app.controller('HymnCtrl', function($scope, $window, $http) {
  $scope.hideTheSearchTool=false
  $http.get("/bookNames")
  .then(function(response) {
      $scope.bookNames = response.data;
  });
  $scope.bookToHymnIds = {};
  $scope.$watch('selectedBook', function () {
    if ($scope.selectedBook) {
      var req = {
          method: 'GET',
          url: '/hymnNames',
          params: {bookName: $scope.selectedBook}
      }
      $http(req)
      .then(function(response) {
            $scope.hymnNames = response.data;
      }, function(response) {
        $scope.error = response.statusText;
      });
    }
  });
  $scope.$watch('selectedHymn', function () {
    if ($scope.selectedHymn) {
      var req = {
        method: 'GET',
        url: '/hymn',
        params: {bookName: $scope.selectedBook, hymnName: $scope.selectedHymn}
      }
      $http(req)
      .then(function(response) {
        $scope.hymn = response.data;
      });
    }
  });

  $scope.bookAndHymnIds = []
  var mapHymnIdsToHymnNames = function (hymnIdList) {
    $scope.hymnIdsToHymnNames = {}
    angular.forEach($scope.bookAndHymnIds, function(value){
      let nameAndId = value.split('$')
      let bookName = nameAndId[0]
      let id = nameAndId[1]
      let req = {
        method: 'GET',
        url: '/bookNameAndHymnIdToName',
        params: {bookName: bookName, hymnId: id}
      }
      $http(req)
      .then(function(response) {
        $scope.hymnIdsToHymnNames[value] = [bookName, response.data[0]];
      }, function(response) {
        $scope.error = response.statusText;
      });
    })
  }

  $scope.submitSelectedHymns = function() {
    let newList = []
    angular.forEach($scope.bookToHymnIds, function(value, key) {
      $scope.bookToHymnIds[key] = value.replace(' ', '')
      let ids = value.split(',')
      angular.forEach(ids, function(id){
        id = Number(id).toString()
        if (id) {
          newList.push(key+'$'+id)
        }
      })
    })
    angular.forEach($scope.bookAndHymnIds, function(value) {
      if (newList.indexOf(value) == -1) {
        $scope.bookAndHymnIds.splice($scope.bookAndHymnIds.indexOf(value), 1)
      }
    })
    angular.forEach(newList, function(value) {
      if ($scope.bookAndHymnIds.indexOf(value) == -1) {
        $scope.bookAndHymnIds.push(value)
      }
    })
    mapHymnIdsToHymnNames()
  }

  var move = function (origin, destination) {
    let temp = $scope.bookAndHymnIds[destination];
    $scope.bookAndHymnIds[destination] = $scope.bookAndHymnIds[origin];
    $scope.bookAndHymnIds[origin] = temp;
  };

  $scope.moveUp = function (index, first) {
    if (!first) {
        move(index, index - 1);
        mapHymnIdsToHymnNames()
    }
  };

  $scope.moveDown = function (index, last) {
    if (!last) {
      move(index, index + 1);
      mapHymnIdsToHymnNames()
    }
  };

  $scope.removeSelectedHymn = function(index) {
    let deletedHymn = $scope.bookAndHymnIds[index]
    // remove the hymn from bookToHymnIds map
    let bookName = deletedHymn[0]
    let id = deletedHymn[1].split(' ')[0]
    let idsString = $scope.bookToHymnIds[bookName]
    let ids = idsString.split(',')
    ids.splice(ids.indexOf(id), 1)
    $scope.bookToHymnIds[bookName] = ids.join(',')
    // remove the hymn from bookAndHymnIds
    $scope.bookAndHymnIds.splice(index, 1)
    mapHymnIdsToHymnNames()
  }

  $scope.selectBook = function(bookName) {
    $scope.selectedBook = bookName;
  }

  $scope.selectHymn = function(hymnName) {
    $scope.selectedHymn = hymnName;
  }

  $scope.buildHymnResult = function() {
    $scope.hymnResults = {}
    angular.forEach($scope.hymnIdsToHymnNames, function(value, key){
//      $window.alert(value)
      var req = {
        method: 'GET',
        url: '/hymn',
        params: {bookName: value[0], hymnName: value[1]}
      }
      $http(req)
      .then(function(response) {
        $scope.hymnResults[key] = response.data;
      });
    })
  }

  $scope.buildPowerPoint = function() {
    var req = {
      method: 'GET',
      url: '/getPowerPoint',
      params: {bookAndHymnIds: $scope.bookAndHymnIds},
      responseType: 'arraybuffer'
    }
    $http(req)
    .then(function(response) {
      let blob = new Blob([response.data])
      let a = document.createElement('a');
      a.href = URL.createObjectURL(new Blob([response.data]));
//      a.download = "Hymns-" + Date.now() +".pptx";
      a.download = "Hymns.pptx";
      a.click();
    });
  }
});