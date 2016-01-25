(function() {
  'use strict';

  var core = angular.module('app.core');

  core.config(toastrConfig);

  /* @ngInject */
  function toastrConfig(toastr) {
    toastr.options.timeOut = 4000;
    toastr.options.positionClass = 'toast-bottom-right';
  }

  //Replace 'Fourcast' and 'Template Project' with your own prefix
  var config = {
    appErrorPrefix: 'Fourcast Error',
    appTitle: 'Template Project'
  };

  core.value('config', config);
  
  core.config(configure);

  /* @ngInject */
  function configure($logProvider, routerHelperProvider, exceptionHandlerProvider) {
    $logProvider.debugEnabled(true);
    routerHelperProvider.configure(config.appErrorPrefix);
    exceptionHandlerProvider.configure(config.appErrorPrefix);
  }

})();