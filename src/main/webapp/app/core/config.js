(function () {
  'use strict';

  var core = angular.module('app.core');

  core.config(toastrConfig);

  /* @ngInject */
  function toastrConfig (toastr) {
    toastr.options.timeout = 4000;
    toastr.options.positionClass = 'toast-bottom-right';
  }
  
  var config = {
    appErrorPrefix: '[<%== appName %> Error]',
    appTitle: '<%= appName %>'
  };
  
  core.value('config', config);
  
  core.config(configure);

  /* @ngInject */
  function configure ($logProvider, routerHelperProvider, exceptionHandlerProvider) {
    if($logProvider.debugEnabled) {
      $logProvider.debugEnabled(true);
    }
    routerHelperProvider.configure(config.appErrorPrefix);
    exceptionHandlerProvider.configure(config.appErrorPrefix);
  }

})();