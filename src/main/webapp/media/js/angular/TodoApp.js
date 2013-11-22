var TodoApp = angular.module("TodoApp", ["ngResource", "ui.bootstrap"]);

// TodoApp.factory('Todo', function($resource) {
  // return $resource('/api/todo/:id', { id : '@id' }, { create : { method : 'PUT' } });
// });

TodoApp.directive('toggle', function() {
  return function(scope, elem, attrs) {
    scope.$on('event:toggle', function() {
      elem.slideToggle();
    });
  };
});

TodoApp.directive('ngConfirm', function(PopupService) {
  return {
    restrict: 'E',
    link: function postLink(scope, element, attrs) {
      // Could have custom or boostrap modal options here
      var popupOptions = {};
      element.bind("click", function()
      {
        PopupService.confirm(attrs["title"], attrs["actionText"],
          attrs["actionButtonText"], attrs["actionFunction"],
          attrs["cancelButtonText"], attrs["cancelFunction"],
          scope, popupOptions);
      });
    }
  };
});


TodoApp.controller('ListCtrl', ['$scope', '$location', function($scope, $location) {

  $scope.todos = {};
  $scope.chosenTodo = {};
  $scope.showDeleteConfirm = false;

  $scope.fetch = function(item) {
    window.backend.load(item).then(function(data) {
      $scope.$apply(function() {
        if (data !== undefined) {
          $scope.todos = data;
        } else {
          $scope.todos = {};
        }
      });
    });
  };

  $scope.save  = function () {
    $scope.item.done = false;
    window.backend.save($scope.item).then(function(item) {
      $scope.$apply(function() {
        if (_.find($scope.todos, function(todo){return todo.id == item.id}) !== undefined) {
          $scope.todos = _.map($scope.todos, function(todo){
            if (todo.id == item.id) { return item; } else { return todo; }
          });
        } else {
          $scope.todos.push(item);
        }
      });
    });
    $scope.item = {};
    $scope.toggle();
  };

  $scope.removeItem = function () {
    var itemId = $scope.chosenTodo.id;
    window.backend.remove({id : itemId}).then(function() {
      $scope.$apply(function() {
        $("#item_" + itemId).fadeOut();
        $scope.todos = _.filter($scope.todos, function(todo){ return todo.id != itemId; });
      });
    });
    $scope.closeRemoveConfirm();
  };

  $scope.numTotal = function() {
    return $scope.todos.length;
  };

  $scope.numOpen = function() {
    return _.filter($scope.todos, function(todo) {return !todo.done;}).length;
  };

  $scope.openRemoveConfirm = function() {
    $scope.chosenTodo = this.todo;
    $scope.showDeleteConfirm = true;
  };

  $scope.closeRemoveConfirm = function () {
    $scope.chosenTodo = {};
    $scope.closeMsg = 'I was closed at: ' + new Date();
    $scope.showDeleteConfirm = false;
  };

  $scope.toggle = function() {
    $scope.$broadcast('event:toggle');
  };

  $scope.addItem = function() {
    $scope.item = {};
    $scope.toggle();
  };

  $scope.editItem = function() {
    var itemId = this.todo.id;
    // clone the object
    $scope.item = jQuery.extend(true, {}, _.find($scope.todos, function(todo){ return todo.id == itemId; }));
    $scope.toggle();
  };

  $scope.fetch();
}]);


