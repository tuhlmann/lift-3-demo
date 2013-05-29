function WpSuggestionsCtrl($scope, $http) {
  $scope.suggestions = [];
  $scope.wpQuery = "";

/*
  [{suggestion:'learn angular', link:'http://www.scaladays.org'}, {...}]
 */

  var serviceUrl = "/search/";

  $scope.getSuggestions = function() {
    var url = serviceUrl + $scope.wpQuery;
    $http.get(url).success(function (data) {
      $scope.suggestions = data;
    });
    $scope.wpQuery = "";
  };

}

WpSuggestionsCtrl.$inject = ['$scope', '$http'];