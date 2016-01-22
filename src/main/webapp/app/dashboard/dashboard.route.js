(function() {
  'use strict';

  angular
    .module('app.dashboard')
    .run(run);

  /* @ngInject */
  function run(routerHelper) {
    routerHelper.configureStates(getStates());
  }

  function getStates() {
    return [
      {
        state: 'dashboard',
        config: {
          url: '/',
          templateUrl: 'app/dashboard/dashboard.html',
          controller: 'DashboardController',
          controllerAs: 'vm',
          title: 'dashboard'
        }
      }
    ];
  }

})();