module.exports = function () {
  var client = './src/main/webapp/';
  var clientApp = client + 'app/';
  var report = './report/';
  var specRunnerFile = 'specs.html';
  var temp = './.tmp/';
  var wiredep = require('wiredep');
  var bowerFiles = wiredep({devDependencies: true}).js;
  var bower = {
    json: require('./bower.json'),
    directory: './bower_components/'
  };
  var nodeModules = 'node_modules';

  var config = {
    /**
     * File paths
     */
    allJs: [
      './src/main/webapp/**/*.js',
      './*.js'
    ],
    build: './build/exploded-app/',
    client: client,
    fonts: bower.directory + 'font-awesome/fonts/**/*.*',
    htmlTemplates: clientApp + '**/*.html',
    images: client + 'images/**/*.*',
    index: client + 'index.html',
    js: [
      clientApp + '**/module.js',
      clientApp + '**/*.js',
      '!' + clientApp + '**/*.spec.js'
    ],
    jsOrder : [
      '**/app.module.js',
      '**/*.module.js',
      '**/*.js'
    ],
    report: report,
    sass: client + 'styles/styles.scss',
    temp: temp,
    /**
     * Plato
     */
    plato: {
      js: clientApp + '**/*.js'
    },

    /**
     * TemplateCache
     */
    templateCache: {
      file: 'templates.js',
      options: {
        module: 'app.core',
        root: 'app/',
        standalone: false
      }
    },

    /**
     * Bower and NPM files
     */
    bower: bower,
    packages: [
      './package.json',
      './bower.json'
    ],

    /**
     * specs.html, our HTML spec runner
     */
    specRunner: client + specRunnerFile,
    specRunnerFile: specRunnerFile,

    /**
     * The sequence of the injections into specs.html:
     *  1 testlibraries
     *      mocha setup
     *  2 bower
     *  3 js
     *  4 spechelpers
     *  5 specs
     *  6 templates
     */
    testlibraries: [
      nodeModules + '/jasmine/lib/jasmine.js',
    ],
    specHelpers: [client + 'test-helpers/*.js'],
    specs: [clientApp + '**/*.spec.js'],
    defaultPort: '8001'
  };

  config.getWiredepDefaultOptions = function () {
    return {
      bowerJson: config.bower.json,
      directory: config.bower.directory,
      ignorePath: config.bower.ignorePath
    };
  };

  return config;
};