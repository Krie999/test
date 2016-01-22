module.exports = function() {
  //Change this for new apps
  var appName = 'gae-template.war';
  var build = './build/';
  var client = './src/main/webapp/';
  var clientApp = client + 'app/';
  var exploded = build + 'exploded-app/';
  var report = './report/';
  var root = './';
  var specRunnerFile = 'specs.html';
  var assets = client + 'assets/';
  var wiredep = require('wiredep');
  var bower = {
    json: require('./bower.json'),
    directory: './src/main/webapp/bower_components/'
  };
  var bowerFiles = wiredep({directory: bower.directory, devDependencies: true}).js;
  var nodeModules = 'node_modules';

  var config = {
    /**
     * File paths
     */
    allJs: [
      './src/main/webapp/**/*.js',
      './*.js',
      '!' + bower.directory + '**/*.*'
    ],
    assets: assets,
    build: build + 'libs/' + appName,
    client: client,
    css: assets + '*.css',
    fonts: bower.directory + 'font-awesome/fonts/**/*.*',
    explodedApp: exploded + 'app/',
    explodedAssets: exploded + 'assets/',
    html: client + '**/*.html',
    htmlTemplates: clientApp + '**/*.html',
    images: client + 'images/**/*.*',
    index: client + 'index.html',
    js: [
      clientApp + '**/*.module.js',
      clientApp + '**/*.js',
      '!' + clientApp + '**/*.spec.js'
    ],
    jsOrder: [
      '**/app.module.js',
      '**/*.module.js',
      '**/*.js'
    ],
    report: report,
    root: root,
    sass: './helpers/sass/**/*.scss',
    source: 'src/main/webapps/',

    /**
     * Optimized files
     */
    optimized: {
      app: 'app.js',
      lib: 'lib.js'
    },

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
      nodeModules + '/mocha/mocha.js',
      nodeModules + '/chai/chai.js',
      nodeModules + '/sinon-chai/lib/sinon-chai.js'
    ],
    specHelpers: [client + 'test-helpers/*.js'],
    specs: [clientApp + '**/*.spec.js'],
    defaultPort: '8888'
  };

  config.getWiredepDefaultOptions = function() {
    return {
      bowerJson: config.bower.json,
      directory: config.bower.directory
    };
  };

  config.karma = getKarmaOptions();

  return config;

  ///////////////////////////////////

  function getKarmaOptions() {
    var options = {
      files: [].concat(
        bowerFiles,
        config.specHelpers,
        clientApp + '**/*.module.js',
        clientApp + '**/*.js',
        assets + config.templateCache.file/*,*/
        //config.serverIntegrationSpecs
      ),
      exclude: [],
      coverage: {
        dir: report + 'coverage',
        reporters: [
          //reporters not supporting the `file` property
          {type: 'html', subdir: 'report-html'},
          {type: 'lcov', subdir: 'report-lcov'},
          {type: 'text-summary'} //, subdir: '.', file: 'text-summary.txt'}
        ]
      },
      preprocessors: {}
    };
    options.preprocessors[clientApp + '**/!(*.spec)+(.js)'] = ['coverage'];
    return options;
  }
};