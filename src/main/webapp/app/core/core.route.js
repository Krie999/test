(function() {
  'use strict';

  angular
    .module('app.core')
    .run(run);

  var otherwise = '/404';

  /* @ngInject */
  function run(routerHelper) {
    routerHelper.configureStates(getStates(), otherwise);
  }

  function getStates() {
    return [
      {
        state: '404',
        config: {
          templateUrl: 'app/core/404.html',
          title: '404',
          url: otherwise
        }
      }
    ];
  }

})();