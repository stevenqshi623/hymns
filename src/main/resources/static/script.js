var app = angular.module('HymnApp', []);

app.filter('replaceChar', function() {
                            return function(x) {
                              return x.replace('$', ' ')
                            };
                          });

app.controller('HymnCtrl', function($scope, $http) {
  $scope.hideTheSearchTool=false
  $http.get("/bookNames")
  .then(function(response) {
      $scope.bookNames = response.data;
  });
  $scope.selectedBookToHymns = {};
  $scope.$watch('selectedBook', function (newVal, oldVal) {
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
  $scope.$watch('selectedHymn', function (newVal, oldVal) {
    if ($scope.selectedHymn) {
      var req = {
        method: 'GET',
        url: 'http://localhost:7777/hymn',
        params: {bookName: $scope.selectedBook, hymnName: $scope.selectedHymn}
      }
      $http(req)
      .then(function(response) {
        $scope.hymn = response.data;
      });
    }
  });

  $scope.selectedHymnList = []
  $scope.selectedBookAndHymnList = []

  var hymnIdsToHymnNames = function (hymnIdList) {
      $scope.selectedBookAndHymnList = []
      angular.forEach($scope.selectedHymnList, function(value){
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
          $scope.selectedBookAndHymnList.push([bookName, response.data[0]]);
        }, function(response) {
          $scope.error = response.statusText;
        });
      })
    }

  $scope.submitSelectedHymns = function() {
    let newList = []
    angular.forEach($scope.selectedBookToHymns, function(value, key) {
      $scope.selectedBookToHymns[key] = value.replace(' ', '')
      let ids = value.split(',')
      angular.forEach(ids, function(id){
        id = Number(id).toString()
        if (id) {
          newList.push(key+'$'+id)
        }
      })
    })
    angular.forEach($scope.selectedHymnList, function(value) {
      if (newList.indexOf(value) == -1) {
        $scope.selectedHymnList.splice($scope.selectedHymnList.indexOf(value), 1)
      }
    })
    angular.forEach(newList, function(value) {
      if ($scope.selectedHymnList.indexOf(value) == -1) {
        $scope.selectedHymnList.push(value)
      }
    })
    hymnIdsToHymnNames()
  }

  var move = function (origin, destination) {
    let temp = $scope.selectedBookAndHymnList[destination];
    $scope.selectedBookAndHymnList[destination] = $scope.selectedBookAndHymnList[origin];
    $scope.selectedBookAndHymnList[origin] = temp;
  };

  $scope.moveUp = function (index, first) {
    if (!first) {
        move(index, index - 1);
    }
  };

  $scope.moveDown = function (index, last) {
    if (!last) {
      move(index, index + 1);
    }
  };

  $scope.removeSelectedHymn = function(index) {
    let deletedHymn = $scope.selectedBookAndHymnList[index]
    // remove the hymn from selectedBookToHymns map
    let bookName = deletedHymn[0]
    let id = deletedHymn[1].split(' ')[0]
    let idsString = $scope.selectedBookToHymns[bookName]
    let ids = idsString.split(',')
    ids.splice(ids.indexOf(id), 1)
    $scope.selectedBookToHymns[bookName] = ids.join(',')
    // remove the hymn from selectedBookAndHymnList
    $scope.selectedBookAndHymnList.splice(index, 1)
  }

  $scope.selectBook = function(bookName) {
    $scope.selectedBook = bookName;
  }

  $scope.selectHymn = function(hymnName) {
    $scope.selectedHymn = hymnName;
  }


//  $scope.$watch('selectedBookToHymns', function (newVal, oldVal) {
//    $scope.test = "test"
//  });
});