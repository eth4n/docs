
'use strict';

/**
 * Settings if-this-then-that rule edition page controller.
 */
angular.module('docs').controller('SettingsIftttEdit', function($scope, $dialog, $state, $stateParams, Restangular, $translate, $q) {

  /**
   * Returns true if in edit mode (false in add mode).
   */
  $scope.isEdit = function () {
    return $stateParams.id;
  };

  /**
   * Update the current workflow.
   */
  $scope.edit = function () {
    var promise = null;

    var rule = angular.copy($scope.rule);
    rule.rule = JSON.stringify({
      conditions: $scope.conditions,
      actions: $scope.actions
    });


    if ($scope.isEdit()) {
      promise = Restangular
        .one('ifttt/rules', $stateParams.id)
        .post('', rule);
    } else {
      promise = Restangular
        .one('ifttt/rules')
        .customPUT( rule);
    }

    promise.then(function () {
      $scope.loadRules();
      $state.go('settings.ifttt');
    });
  };

  /**
   * Delete the current rule.
   */
  $scope.remove = function () {
    var title = $translate.instant('settings.ifttt.edit.delete_ifttt_title');
    var msg = $translate.instant('settings.ifttt.edit.delete_ifttt_message');
    var btns = [
      { result:'cancel', label: $translate.instant('cancel') },
      { result:'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }
    ];

    $dialog.messageBox(title, msg, btns, function (result) {
      if (result === 'ok') {
        Restangular.one('ifttt/rules', $stateParams.id).remove().then(function () {
          $scope.loadRules();
          $state.go('settings.ifttt');
        }, function() {
          $state.go('settings.ifttt');
        });
      }
    });
  };

  $scope.removeCondition = function (condition) {
    $scope.conditions.splice($scope.conditions.indexOf(condition), 1);
  };
  $scope.removeAction = function (action) {
    $scope.actions.splice($scope.actions.indexOf(action), 1);
  };

  $scope.addCondition = function () {
    /* TODO: There is currently only one condition to be added. Remove this if there are additional implementations */
    $scope.conditions.push( { condition: 'DocumentProperty', data: { 'property': 'DOCUMENT_TAGS', 'comparator': 'CONTAINS'} });
  };
  $scope.addAction = function () {
    $scope.actions.push( { action: 'AddTag', data: { tag: '', method: 'POST'} });
  };


  $scope.updateComparators = function (condition) {
    if (condition.data.property === 'DOCUMENT_TAGS') {
      condition.data.comparator = "CONTAINS";
    } else if (condition.data.property === 'FILE_CONTENT') {
      condition.data.comparator = "MATCHES";
    } else if (condition.data.property === 'DOCUMENT_META_BOOLEAN') {
      condition.data.comparator = "IS_TRUE";
    }
  };

  $scope.setDefaultWebhookMethod = function (action) {
    if (action.action === 'CallWebhook') {
      action.data.method = "POST";
    }
  };

  /**
   * In edit mode, load the current rule.
   */
  if ($scope.isEdit()) {
    Restangular.one('ifttt/rules', $stateParams.id).get().then(function (data) {
      $scope.rule = data;
      var parsed = JSON.parse($scope.rule.rule);
      $scope.conditions = parsed.conditions;
      $scope.actions = parsed.actions
    });
  } else {
    $scope.rule = {
      name: ''
    };
    $scope.conditions = [];
        $scope.actions = [];
    $scope.addCondition();
    $scope.addAction();
  }
});
