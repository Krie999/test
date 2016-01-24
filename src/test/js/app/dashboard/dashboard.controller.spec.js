/* jshint -W117, -W030 */
describe('DashboardController', function() {
  var controller;

  beforeEach(function() {
    bard.appModule('app.dashboard');
    bard.inject('$controller', '$rootScope');
  });

  beforeEach(function() {
    controller = $controller('DashboardController');
  });

  bard.verifyNoOutstandingHttpRequests();

  describe('Dashboard controller', function() {
    it('should be created successfully', function() {
      expect(controller).to.be.defined;
    });
  });
});