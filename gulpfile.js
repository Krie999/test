// jscs:disable maximumNumberOfLines
/*
 * TODO: Wiredep with stubs?
 * TODO: Server integration specs?
 * TODO: Try out gulp-watch plugin
 */
var args = require('yargs').argv;
var browserSync = require('browser-sync');
var config = require('./gulp.config')();
var del = require('del');
var glob = require('glob');
var gulp = require('gulp');
var path = require('path');
var _ = require('lodash');
var $ = require('gulp-load-plugins')({lazy: true});

var colors = $.util.colors;
var port = process.env.port || config.defaultPort;

/**
 * yargs variables can be passed in to alter the behavior, when present.
 * Example: gulp serve_dev
 *
 * --verbose  : Various tasks will produce more output to the console.
 * --nosync   : Don't launch the browser with browser-sync when serving code.
 * --startServers: Will start servers for midway tests on the test task.
 */

/**
 * List the available gulp tasks
 */
gulp.task('help', $.taskListing);
gulp.task('default', ['help']);

/**
 * Vet the code and create coverate report
 *
 * @return {Stream}
 */
gulp.task('vet', function() {
  log('Analyzing source with JSHint and JSCS');

  return gulp
    .src(config.allJs)
    .pipe($.if(args.verbose, $.print()))
    .pipe($.jshint())
    .pipe($.jshint.reporter('jshint-stylish'), {verbose: true})
    .pipe($.jshint.reporter('fail'))
    .pipe($.jscs())
    .pipe($.jscsStylish())
    .pipe($.jscs.reporter('fail'));
});

/**
 * Create a visualizer report
 */
gulp.task('plato', function(done) {
  log('Analyzing source with Plato');
  log('Browse to /report/plato/index.html to see Plato results');

  startPlatoVisualizer(done);
});

/**
 * Compile sass to css
 *
 * @return {Stream}
 */
gulp.task('styles', ['clean-styles'], function() {
  log('Compiling SCSS -> CSS');

  return gulp
    .src(config.sass)
    .pipe($.sass().on('error', $.sass.logError))
    .pipe($.autoprefixer({browsers: ['last 2 version', '> 5%']}))
    .pipe(gulp.dest(config.assets));
});

/**
 * Copy fonts
 *
 * @return {Stream}
 */
gulp.task('fonts', ['clean_fonts'], function() {
  log('Copying fonts');

  return gulp
    .src(config.fonts)
    .pipe(gulp.dest(config.build + 'fonts'));
});

/**
 * Compress images
 *
 * @return {Stream}
 */
gulp.task('images', ['clean_images'], function() {
  log('Compressing and copying images');

  return gulp
    .src(config.images)
    .pipe($.imagemin({optimizationLevel: 4}))
    .pipe(gulp.dest(config.build + 'images'));
});

gulp.task('watch_sass', function() {
  gulp.watch([config.sass], ['styles']);
});

/**
 * Create $templateCache from the html templates
 *
 * @return {Stream}
 */
gulp.task('templatecache', ['clean_code'], function() {
  log('Creating an AngularJS $templateCache');

  return gulp
    .src(config.htmlTemplates)
    .pipe($.if(args.verbose, $.bytediff.start()))
    .pipe($.htmlmin({removeEmptyAttributes: true}))
    .pipe($.if(args.verbose, $.bytediff.stop(bytediffFormatter)))
    .pipe($.angularTemplatecache(
      config.templateCache.file,
      config.templateCache.options
    ))
    .pipe(gulp.dest(config.assets));
});

/**
 * Wire-up the bower dependencies
 *
 * @return {Stream}
 */
gulp.task('wiredep', function() {
  log('Wiring the bower dependencies into index.html');

  var wiredep = require('wiredep').stream;
  var options = config.getWiredepDefaultOptions();

  // Only include stubs if flag is enabled
  //var js = args.stubs ? [].concat(config.js, config.stubsjs) : config.js;

  return gulp
    .src(config.index)
    .pipe(wiredep(options))
    .pipe(inject(config.js, '', config.jsOrder))
    .pipe(gulp.dest(config.client));
});

/**
 * Remove all fonts from the build folder
 *
 * @param  {Function} done - callback when complete
 */
gulp.task('clean_fonts', function(done) {
  clean(config.build + 'fonts/**/*.*', done);
});

/**
 * Remove all images from the build folder
 *
 * @param  {Function} done - callback when complete
 */
gulp.task('clean_images', function(done) {
  clean(config.build + 'images/**/*.*', done);
});

/**
 * Remove all styles from the build and assets folders
 *
 * @param  {Function} done - callback when complete
 */
gulp.task('clean-styles', function(done) {
  var files = [].concat(
    config.assets + '**/*.css',
    config.build + 'styles/**/*.css'
  );
  clean(files, done);
});

/**
 * Remove all js and html from the build and assets folders
 *
 * @param  {Function} done - callback when complete
 */
gulp.task('clean_code', function(done) {
  var files = [].concat(
    config.assets + '**/*.js',
    config.build + 'js/**/*.js',
    config.build + '**/*.html'
  );
  clean(files, done);
});

/**
 * Remove all files from the build, assets, and reports folders
 *
 * @param  {Function} done - callback when complete
 */
gulp.task('clean', function(done) {
  var delconfig = [].concat(config.build, config.assets, config.report);
  log('Cleaning: ' + colors.blue(delconfig));
  del(delconfig, done);
});

/**
 * Build everything
 * This is separate so we can run tests on
 * optimize before handling image or fonts
 */
gulp.task('build', ['optimize', 'images', 'fonts'], function() {
  log('Building everything');

  var msg = {
    title: 'gulp build',
    subtitle: 'Deployed to the build folder',
    message: 'Running \'gulp serve_build\''
  };
  del(config.assets);
  log(msg);
  notify(msg);
});

/**
 * Optimize all files, move to a build folder,
 * and inject them into the new index.html
 *
 * @return {Stream}
 */
gulp.task('optimize', ['inject', 'test'], function() {
  log('Optimizing the js, css, and html');

  // Filters are named for the gulp-useref path
  var cssFilter = $.filter('**/*.css');
  var jsAppFilter = $.filter('**/' + config.optimized.app);
  var jsLibFilter = $.filter('**/' + config.optimized.lib);

  return gulp
    .src(config.index)
    .pipe($.plumber())
    // Gather all assets from the html with useref
    .pipe($.useref({searchPath: config.client}))
    // Get the css
    .pipe(cssFilter)
    .pipe($.cssnano())
    .pipe(cssFilter.restore())
    // Get the custom javascript
    .pipe(jsAppFilter)
    .pipe($.ngAnnotate({add: true}))
    .pipe($.uglify())
    .pipe(getHeader())
    .pipe(jsAppFilter.restore())
    // Get the vendor javascript
    .pipe(jsLibFilter)
    .pipe($.uglify())
    .pipe(jsLibFilter.restore())
    .pipe(gulp.dest(config.build));
});

gulp.task('inject', ['wiredep', 'styles', 'templatecache'], function() {
  log('Wire up css into the html, after files are ready');
  var templateCache = config.assets + config.templateCache.file;

  return gulp
    .src(config.index)
    .pipe(inject(config.css))
    .pipe(inject(templateCache, 'templates'))
    .pipe(gulp.dest(config.client));
});

/**
 * Run specs once and exit
 * To start servers and run midway specs as well:
 * gulp test --startServers
 */
gulp.task('test', ['vet', 'templatecache'], function(done) {
  startTests(true, done);
});

/**
 * Bump the version
 * --type=pre will bump the prerelease version *.*.*-x
 * --type=patch or no flag will bump the patch version *.*.x
 * --type=minor will bump the minor version *.x.*
 * --type=major will bump the major version x.*.*
 * --version=1.2.3 will bump to a specific version and ignore other flags
 */
gulp.task('bump', function() {
  var msg = 'Bumping versions';
  var type = args.type;
  var version = args.ver;
  var options = {};
  if (version) {
    options.version = version;
    msg += ' to ' + version;
  } else {
    options.type = type;
    msg += ' for a ' + type;
  }
  log(msg);

  return gulp
    .src(config.packages)
    .pipe($.print())
    .pipe($.bump(options))
    .pipe(gulp.dest(config.root));
});

/**
 * Run specs and wait.
 * Watch for file changes and re-run tests on each change
 * To start servers and run midway specs as well:
 *    gulp autotest --startServers
 */
gulp.task('autotest', function(done) {
  startTests(false, done);
});

/**
 * serve the dev environment
 *
 */
gulp.task('serve_dev', ['inject'], function() {
  serve(true);
});

/**
 * serve the build environment
 */
gulp.task('serve_build', ['build'], function() {
  serve(false);
});

/**
 * Optimize the code and re-load browserSync
 */
gulp.task('browserSyncReload', ['optimize'], browserSync.reload);

/**
 * Copy js files to the exploded app dir
 */
gulp.task('copyJs', function() {
  log('Copying js files to exploded directory');

  return gulp.src(config.js)
             .pipe($.changed(config.explodedApp))
             .pipe(gulp.dest(config.explodedApp));
});

/**
 * Copy assets to the exploded app dir
 */
gulp.task('copyAssets', ['styles', 'templatecache'], function() {
  log('Copying assets to exploded directory');

  return gulp.src(config.assets + '**/*.*')
             .pipe($.changed(config.explodedAssets))
             .pipe(gulp.dest(config.explodedAssets));
});

/**
 * Copy index.html to the exploded dir
 */
gulp.task('copyIndex', function() {
  log('Copying index file to exploded directory');

  return gulp.src(config.index)
             .pipe($.changed(config.exploded))
             .pipe(gulp.dest(config.exploded));
});

///////////////////////////////////

/**
 * Formatter for bytediff to display the size changes after processing
 *
 * @param  {Object} data - byte data
 * @return {String}      Difference in bytes, formatted
 */
function bytediffFormatter(data) {
  var difference = data.savings > 0 ? ' smaller.' : ' larger.';
  return data.fileName + ' went from ' +
    (data.startSize / 1000).toFixed(2) + ' kB to ' +
    (data.endSize / 1000).toFixed(2) + ' kB and is ' +
    formatPercent(1 - data.percent, 2) + '%' + difference;
}

/**
 * Delete all files in a given path
 *
 * @param  {Array}   path - array of paths to delete
 * @param  {Function} done - callback when complete
 */
function clean(path, done) {
  log('Cleaning: ' + colors.blue(path));
  del(path, done);
}

/**
 * When files change, log it
 *
 * @param  {Object} event - event that fired
 */
function changeEvent(event) {
  var srcPattern = new RegExp('/.*(?=/' + config.source + ')/');
  log('File ' + event.path.replace(srcPattern, '') + ' ' + event.type);
}

/**
 * Inject files in a sorted sequence at a specified inject label
 *
 * @param   {Array} src   glob pattern for source files
 * @param   {String} label   The label name
 * @param   {Array} order   glob pattern for sort order of the files
 *
 * @returns {Stream}   The stream
 */
function inject(src, label, order) {
  var options = {read: false, relative: true};
  if (label) {
    options.name = 'inject:' + label;
  }

  return $.inject(orderSrc(src, order), options);
}

/**
 * Order a stream
 *
 * @param   {Stream} src   The gulp.src stream
 * @param   {Array} order Glob array pattern
 * @returns {Stream} The ordered stream
 */
function orderSrc(src, order) {
  //order = order || ['**/*'];
  return gulp
    .src(src, {read: false})
    .pipe($.if(order, $.order(order)));
}

/**
 * Format a number as a percentage
 *
 * @param  {Number} num Number to format as a percent
 * @param  {Number} precision Precision of the decimal
 * @return {String} Formatted perentage
 */
function formatPercent(num, precision) {
  return (num * 100).toFixed(precision);
}

/**
 * Format and return the header for files
 *
 * @return {String} Formatted file header
 */
function getHeader() {
  var pkg = require('./package.json');
  var template = [
    '/**',
    ' * <%= pkg.name %> - <%= pkg.description %>',
    ' * @authors <%= pkg.authors %>',
    ' * @version v<%= pkg.version %>',
    ' * @link <%= pkg.homepage %>',
    ' * @license <%= pkg.license %>',
    ' */',
    ''
  ].join('\n');
  return $.header(template, {
    pkg: pkg
  });
}

/**
 * Log a message or series of messages using chalk's blue color.
 * Can pass in a string, object or array.
 */
function log(msg) {
  if (typeof msg === 'object') {
    for (var item in Object.keys(msg)) {
      $.util.log(colors.blue(msg[item]));
    }
  } else {
    $.util.log(colors.blue(msg));
  }
}

/**
 * Start BrowserSync
 */
function startBrowserSync(isDev, specRunner) {
  if (browserSync.active) {
    return;
  }

  log('Starting BrowserSync on port ' + port);

  // If build: watches the files, builds, and restarts browser-sync.
  // If dev: watches sass, compiles it to css, browser-sync handles reload
  if (isDev) {
    gulp.watch([config.sass, config.js, config.html, config.index], ['copyJs', 'copyAssets', 'copyIndex'])
        .on('change', changeEvent);
  } else {
    gulp.watch([config.sass, config.js, config.html], ['browserSyncReload'])
        .on('change', changeEvent);
  }

  var options = {
    proxy: 'localhost:' + port,
    port: 3000,
    files: isDev ? [
      config.explodedApp + '**/*.js',
      config.explodedAssets + '**/*.*'
    ] : [],
    ghostMode: { // these are the defaults t,f,t,t
      clicks: true,
      location: false,
      forms: true,
      scroll: true
    },
    injectChanges: true,
    logFileChanges: true,
    logLevel: 'info',
    logPrefix: 'browsersync',
    notify: true,
    reloadDelay: 0 //1000
  };
  if (specRunner) {
    options.startPath = config.specRunnerFile;
  }

  browserSync(options);
}

/**
 * Start Plato inspector and visualizer
 */
function startPlatoVisualizer(done) {
  log('Running Plato');

  var files = glob.sync(config.plato.js);
  var excludeFiles = /.*\.spec\.js/;
  var plato = require('plato');

  var options = {
    title: 'Plato Inspections Report',
    exclude: excludeFiles
  };
  var outputDir = config.report + '/plato';

  plato.inspect(files, outputDir, options, platoCompleted);

  function platoCompleted(report) {
    var overview = plato.getOverviewReport(report);
    if (args.verbose) {
      log(overview.summary);
    }
    if (done) {
      done();
    }
  }
}

/**
 * Start the tests using karma.
 *
 * @param  {boolean} singleRun - True means run once and end (CI), or keep running (dev)
 * @param  {Function} done - Callback to fire when karma is done
 */
function startTests(singleRun, done) {
  //var child;
  var excludeFiles = [];
  //var fork = require('child_process').fork;
  var Server = require('karma').Server;
  var karma = new Server({
    configFile: __dirname + '/karma.conf.js',
    exclude: excludeFiles,
    singleRun: !!singleRun
  }, karmaCompleted);

  //var serverSpecs = config.serverIntegrationSpecs;

  //if (args.startServers) {
  //  log('Starting servers');
  //  var savedEnv = process.env;
  //  savedEnv.NODE_ENV = 'dev';
  //  savedEnv.PORT = 8888;
  //  child = fork(config.nodeServer);
  //} else {
  //  if (serverSpecs && serverSpecs.length) {
  //    excludeFiles = serverSpecs;
  //  }
  //}
  karma.start();

  ////////////////

  function karmaCompleted(karmaResult) {
    log('Karma completed');
    //if (child) {
    //  log('shutting down the child process');
    //  child.kill();
    //}
    if (karmaResult === 1) {
      done('karma: tests failed with code ' + karmaResult);
    } else {
      done();
    }
  }
}

function notify(options) {
  var notifier = require('node-notifier');
  var notifyOptions = {
    sound: 'Bottle',
    contentImage: path.join(__dirname, 'gulp.png'),
    icon: path.join(__dirname, 'gulp.png')
  };
  _.assign(notifyOptions, options);
  notifier.notify(notifyOptions);
}

/**
 * @param  {Boolean} isDev - dev or build mode
 * @param  {Boolean} specRunner - server spec runner html
 */
function serve(isDev, specRunner) {
  startBrowserSync(isDev, specRunner);
}