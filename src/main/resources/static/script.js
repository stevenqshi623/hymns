var app = angular.module('HymnApp', []);

app.filter('myFormat', function() {
  return function(x) {
    return x.replace(/\$/g, ' ');
  };
});

app.controller('HymnCtrl', function($scope, $window, $http) {
  document.getElementById("defaultOpen").click();
  let bookDefault = "Please Select A Book"
  let hymnDefault = "Please Select A Hymn"

  $scope.selectedBookOrDefault = bookDefault
  $scope.selectedHymnOrDefault = hymnDefault

  $scope.bookNameToAllHymns = {}
  $scope.bookNames = []
  $http.get("/getBookNameToAllHymns")
  .then(function(response) {
      $scope.bookNameToAllHymns = response.data;
      angular.forEach($scope.bookNameToAllHymns, function(value, key) {
        $scope.bookNames.push(key) 
      })
  });

  $scope.selectBook = function(bookName) {
    $scope.selectedBook = bookName;
  }

  $scope.selectHymn = function(hymnName) {
    $scope.selectedHymn = hymnName;
  }

  $scope.hymnNames = [];
  $scope.selectedHymnLyric = false;
  $scope.$watch('selectedBook', function () {
    if ($scope.selectedBook) {
      $scope.selectedBookOrDefault = $scope.selectedBook;
      $scope.selectedHymnOrDefault = hymnDefault;
      $scope.selectedHymnLyric = false;
      $scope.selectedHymn = false;
      $scope.hymnNames = $scope.bookNameToAllHymns[$scope.selectedBook];
    }
    else {
      $scope.selectedBookOrDefault = bookDefault;
    }
  });

  $scope.$watch('selectedHymn', function () {
    if ($scope.selectedHymn) {
      $scope.selectedHymnOrDefault = $scope.selectedHymn;
      var req = {
        method: 'GET',
        url: '/getLyrics',
        params: {bookNameAndHymnNames: [$scope.selectedBook + '$' + $scope.selectedHymn], includeChineseVersion: [true], includeEnglishVersion: [true]},
      };
      $http(req)
      .then(function(response) {
        $scope.selectedHymnLyric = response.data[0];
      });
    }
    else {
      $scope.selectedHymnOrDefault = hymnDefault;
    }
  });

  $scope.bookToSelectedHymnIds = {};
  $scope.bookAndSelectedHymnList = [];
  $scope.includeChineseLyrics = {};
  $scope.includeEnglishLyrics = {};

  $scope.submitSelectedHymns = function() {
    let newList = [];
    let error = "";
    angular.forEach($scope.bookToSelectedHymnIds, function(value, bookName) {
      $scope.bookToSelectedHymnIds[bookName] = value.replace(' ', '');
      if (value.length > 0) {
        let ids = value.split(',');
        if (ids.length > 0) {
          angular.forEach(ids, function(id){
            if (id.length > 0 && Number(id) > 0) {
              id = Number(id).toString();
              let hymnNames = $scope.bookNameToAllHymns[bookName];
              let findHymn = false;
              angular.forEach(hymnNames, function(hymnName) {
                if (hymnName.startsWith(id + '$')) {
                  newList.push(bookName + '$' + hymnName);
                  findHymn = true;
                }
              });
              if (!findHymn) {
                error += bookName + ": ";
                error += id + "\n";
              }
            }
          })
        }
      }
    })

    if (error.length > 0) {
      $window.alert("The following hymns are not available yet. So, they are ignored.\n\n" + error);
    }

    angular.forEach($scope.bookAndSelectedHymnList, function(value) {
      if (newList.indexOf(value) == -1) {
        $scope.bookAndSelectedHymnList.splice($scope.bookAndSelectedHymnList.indexOf(value), 1);
        delete $scope.includeChineseLyrics[value];
        delete $scope.includeEnglishLyrics[value];
      }
    })
    angular.forEach(newList, function(value) {
      if ($scope.bookAndSelectedHymnList.indexOf(value) == -1) {
        $scope.bookAndSelectedHymnList.push(value)
        $scope.includeChineseLyrics[value] = true;
        $scope.includeEnglishLyrics[value] = true;
      }
    })
  }

  var move = function (origin, destination) {
    let temp = $scope.bookAndSelectedHymnList[destination];
    $scope.bookAndSelectedHymnList[destination] = $scope.bookAndSelectedHymnList[origin];
    $scope.bookAndSelectedHymnList[origin] = temp;
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
    let deletedHymn = $scope.bookAndSelectedHymnList[index]
    // remove the hymn from bookToHymnIds map
    let nameAndId = deletedHymn.split('$')
    let bookName = nameAndId[0]
    let id = nameAndId[1]
    let idsString = $scope.bookToSelectedHymnIds[bookName]
    let ids = idsString.split(',')
    ids.splice(ids.indexOf(id), 1)
    $scope.bookToSelectedHymnIds[bookName] = ids.join(',')
    // remove the hymn from bookAndHymnIds
    $scope.bookAndSelectedHymnList.splice(index, 1)
    delete $scope.includeChineseLyrics[deletedHymn];
    delete $scope.includeEnglishLyrics[deletedHymn];
  }

  $scope.selectedHymnLyrics = [];
  $scope.showLyrics = function() {
    $scope.selectedHymnLyrics = [];
    let includeChineseLyricsList = [];
    let includeEnglishLyricsList = [];
    angular.forEach($scope.bookAndSelectedHymnList, function(bookAndSelectedHymn){
      includeChineseLyricsList.push($scope.includeChineseLyrics[bookAndSelectedHymn]);
      includeEnglishLyricsList.push($scope.includeEnglishLyrics[bookAndSelectedHymn]);
    });
    var req = {
      method: 'GET',
      url: '/getLyrics',
      params: {bookNameAndHymnNames: $scope.bookAndSelectedHymnList,
        includeChineseVersion: includeChineseLyricsList,
        includeEnglishVersion: includeEnglishLyricsList}
    }
    $http(req)
    .then(function(response) {
      $scope.selectedHymnLyrics = response.data;
    });
  }

  $scope.showSlides = function() {
    let includeChineseLyricsList = [];
    let includeEnglishLyricsList = [];
    angular.forEach($scope.bookAndSelectedHymnList, function(bookAndSelectedHymn){
      includeChineseLyricsList.push($scope.includeChineseLyrics[bookAndSelectedHymn]);
      includeEnglishLyricsList.push($scope.includeEnglishLyrics[bookAndSelectedHymn]);
    });
    var req = {
      method: 'GET',
      url: '/showSlides',
      params: {bookNameAndHymnNames: $scope.bookAndSelectedHymnList,
              includeChineseVersion: includeChineseLyricsList,
              includeEnglishVersion: includeEnglishLyricsList}
    }
    $http(req)
    .then(function(response) {
      let tempStr = response.data[0];
      let myWindow = $window.open('', '_blank');
      myWindow.document.write(
//      https://github.com/hakimel/reveal.js
        "<!doctype html>" +
        "<html>" +
        "<head>" +
        "    <meta charset='utf-8'>" +
        "    <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>" +
        "    <title>Hymns</title>" +
        "    <link rel='stylesheet' href='reveal/css/reset.css'>" +
        "    <link rel='stylesheet' href='reveal/css/reveal.css'>" +
        "    <link rel='stylesheet' href='reveal/css/theme/white.css'>" +
        "    <link rel='stylesheet' href='reveal/lib/css/monokai.css'>" +
        "    <script src='https://ajax.googleapis.com/ajax/libs/angularjs/1.6.9/angular.min.js'></script>" +
        "    <script src='script.js'></script>" +
        "</head>" +
        "<body ng-app='HymnApp' ng-controller='HymnCtrl'>" +
        "<div class='reveal'>" +
        "    <div class='slides'>" +
        "        <section>" +
        tempStr +
        "        </section>" +
        "    </div>" +
        "</div>" +
        "<script src='reveal/js/reveal.js'></script>" +
        "<script>" +
        "			Reveal.initialize({" +
        "				width: '100%'," +
        "	            height: '100%'," +
        "	            margin: 0," +
        "	            minScale: 1," +
        "	            maxScale: 1" +
        "			});" +
        "</script>" +
        "</body>" +
        "</html>"
      );
    });
  }

  $scope.downloadSlides = function() {
    let includeChineseLyricsList = [];
    let includeEnglishLyricsList = [];
    angular.forEach($scope.bookAndSelectedHymnList, function(bookAndSelectedHymn){
      includeChineseLyricsList.push($scope.includeChineseLyrics[bookAndSelectedHymn]);
      includeEnglishLyricsList.push($scope.includeEnglishLyrics[bookAndSelectedHymn]);
    });
    var req = {
      method: 'GET',
      url: '/downloadSlides',
      params: {bookNameAndHymnNames: $scope.bookAndSelectedHymnList,
        includeChineseVersion: includeChineseLyricsList,
        includeEnglishVersion: includeEnglishLyricsList},
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

function openTab(evt, tabName) {
  var i, tabcontent, tablinks;
  tabcontent = document.getElementsByClassName("tabcontent");
  for (i = 0; i < tabcontent.length; i++) {
    tabcontent[i].style.display = "none";
  }
  tablinks = document.getElementsByClassName("tablinks");
  for (i = 0; i < tablinks.length; i++) {
    tablinks[i].className = tablinks[i].className.replace(" active", "");
  }
  document.getElementById(tabName).style.display = "block";
  evt.currentTarget.className += " active";
}