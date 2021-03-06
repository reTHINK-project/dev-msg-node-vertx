// Karma configuration
// Generated on Wed Sep 16 2015 12:17:06 GMT+0100 (WEST)

module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '',

    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['mocha', 'browserify'],

    // list of files / patterns to load in the browser
    files: [
      'src/js/client/**/*.js',
      'test/**/*.spec.js'
    ],

    // list of files to exclude
    exclude: [
      'test/**/VertxCluster.spec.js',
      'test/**/GlobalRegistryConnector.spec.js',
      'test/**/Metrics.spec.js'
    ],

    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      'src/js/client/**/*.js': ['browserify'],
      'test/**/*.spec.js': ['browserify']
    },

    browserify: {
      transform: ['babelify']
    },

    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['mocha'],

    // web server port
    port: 9876,

    // How long will Karma wait for a message from a browser before disconnecting from it (in ms).
    browserNoActivityTimeout: 110000,

    // Whether or not Karma or any browsers should raise an error when an invalid SSL certificate is found.
    proxyValidateSSL: false,

    // enable / disable colors in the output (reporters and logs)
    colors: true,

    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,

    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: ['ChromeTravis'],

    customLaunchers: {
      ChromeTravis: {
        base: 'Chrome',
        flags: [
         '--disable-web-security',
         '--ignore-certificate-errors'
       ]
      }
    },

    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: true
  });

};
