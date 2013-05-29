function ActorsCtrl($scope, $http) {
  $scope.messages = [];
  $scope.whatISaid = "";

  /*
   * A simple way to pass in data from outside into a controller:
   * Trigger an event on $(document) and receive the signal here.
   * Another way would be to create a directive and pass it in...
   */
  $(document).on("new-chat-msg", function (e) {
    for(var i=1; i < arguments.length; i++) {
      $scope.messages.push( arguments[i] );
    }
    $scope.$digest(); // to force apply
  });

  $scope.blabber = function() {
    console.log($scope.whatISaid);
    if ($scope.whatISaid.trim().length > 0) {
      window.actorsBridge.send($scope.whatISaid);
      $scope.messages.push({from: "Me", text: $scope.whatISaid});
      $scope.whatISaid = "";
    }
  };

}

ActorsCtrl.$inject = ['$scope', '$http'];