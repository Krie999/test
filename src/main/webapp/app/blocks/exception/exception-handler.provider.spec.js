/* jshint -W117, -W030 */
describe('blocks.exception', function() {
  var exceptionHandlerProvider;
  var mocks = {
    errorMessage: 'fake error',
    prefix: '[TEST]'
  };

  beforeEach(function() {
    bard.appModule('blocks.exception', function(_exceptionHandlerProvider_) {
      exceptionHandlerProvider = _exceptionHandlerProvider_;
    });
    bard.inject('$rootScope');
  });

  bard.verifyNoOutstandingHttpRequests();

  describe('exceptionHandlerProvider', function() {
    it('should have exceptionHandlerProvider defined', function() {
      expect(exceptionHandlerProvider).to.be.defined;
    });

    it('should have configuration defined', function() {
      expect(exceptionHandlerProvider.config).to.be.defined;
    });

    it('should have configuration defined', function() {
      expect(exceptionHandlerProvider.configure).to.be.defined;
    });

    describe('with appErrorPrefix', function() {
      beforeEach(function() {
        exceptionHandlerProvider.configure(mocks.prefix);
      });

      it('should have appErrorPrefix defined', function() {
        expect(exceptionHandlerProvider.$get().config.appErrorPrefix).to.be.defined;
      });

      it('should have appErrorPrefix set properly', function() {
        expect(exceptionHandlerProvider.$get().config.appErrorPrefix)
          .to.equal(mocks.prefix);
      });

      it('should throw an error when forced', function() {
        expect(functionThatWillThrow).to.throw();
      });

      it('manual error is handled by decorator', function() {
        var exception;
        exceptionHandlerProvider.configure(mocks.prefix);
        try {
          $rootScope.$apply(functionThatWillThrow);
        } catch (ex) {
          exception = ex;
        }
        expect(exception.message).to.equal(mocks.prefix + mocks.errorMessage);
      });
    });

  });

  function functionThatWillThrow() {
    throw new Error(mocks.errorMessage);
  }

});