(function () {
  'use strict';

  angular
    .module('app.core')
    .constant('toastr', toastr)
    .constant('_', lodash);
})();