'use strict';

/**
 * Settings If-this-then-that page controller.
 */
angular.module('docs').controller('SettingsIfttt', function($scope, $state, Restangular) {
  /**
   * Load rules from server.
   */
  $scope.loadRules = function() {
    Restangular.one('ifttt/rules').get().then(function(data) {
      $scope.rules = data.rules;
    });
  };

  $scope.loadRules();

  /**
   * Edit a If-this-then-that rule.
   */
  $scope.editRule = function(user) {
    $state.go('settings.ifttt.edit', { id: user.id });
  };
});
