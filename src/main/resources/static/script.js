var app = angular.module('HymnApp', []);

app.controller('HymnCtrl', function($scope, $window, $http) {
  $scope.hideLyrics = true
  let bookDefault = "Please Select A Book"
  let hymnDefault = "Please Select A Hymn"
  $scope.selectedBookOrDefault = bookDefault
  $scope.selectedHymnOrDefault = hymnDefault
  document.getElementById("defaultOpen").click();

  $http.get("/bookNames")
  .then(function(response) {
      $scope.bookNames = response.data;
  });
  $scope.bookToHymnIds = {};
  $scope.$watch('selectedBook', function () {
    if ($scope.selectedBook) {
      $scope.selectedBookOrDefault = $scope.selectedBook;
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
    else {
      $scope.selectedBookOrDefault = bookDefault;
    }
  });
  $scope.$watch('selectedHymn', function () {
    if ($scope.selectedHymn) {
      $scope.selectedHymnOrDefault = $scope.selectedHymn;
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
    else {
      $scope.selectedHymnOrDefault = hymnDefault;
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
    let nameAndId = deletedHymn.split('$')
    let bookName = nameAndId[0]
    let id = nameAndId[1]
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
    $scope.hideLyrics = false;
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

  $scope.buildPresentation = function() {
    var req = {
      method: 'GET',
      url: '/getPresentation',
      params: {bookAndHymnIds: $scope.bookAndHymnIds},
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
        "    <script>" +
        "		var link = document.createElement('link');" +
        "		link.rel = 'stylesheet';" +
        "		link.type = 'text/css';" +
        "		link.href = window.location.search.match(/print-pdf/gi) ? 'reveal/css/print/pdf.css' : 'reveal/css/print/paper.css';" +
        "		document.getElementsByTagName('head')[0].appendChild(link);" +
        "    </script>" +
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
        "	            maxScale: 1," +
        "				dependencies: [" +
        "					{ src: 'reveal/plugin/markdown/marked.js' }," +
        "					{ src: 'reveal/plugin/markdown/markdown.js' }," +
        "					{ src: 'reveal/plugin/notes/notes.js', async: true }," +
        "					{ src: 'reveal/plugin/highlight/highlight.js', async: true }" +
        "				]" +
        "			});" +
        "</script>" +
        "</body>" +
        "</html>"
      );
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