angular.module("app.core").run(["$templateCache", function($templateCache) {$templateCache.put("app/core/404.html","<md-content>\r\n  <md-card>\r\n    <md-card-title>\r\n      404 - Temporary\r\n    </md-card-title>\r\n    <md-card-content>\r\n      Oops, looks like the page couldn\'t be found\r\n    </md-card-content>\r\n  </md-card>\r\n</md-content>");
$templateCache.put("app/dashboard/dashboard.html","<md-content>\r\n  <md-card>\r\n    <md-card-title>{{vm.title}}</md-card-title>\r\n    <md-card-content>\r\n      Test content this is super cool\r\n    </md-card-content>\r\n  </md-card>\r\n</md-content>");
$templateCache.put("app/dashboard/dingdong.html","Boo this is cool");}]);