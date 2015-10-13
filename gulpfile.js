var gulp = require('gulp');
var exec = require('child_process').exec;

// Gulp task to generate development documentation;
gulp.task('doc', function(done) {
  console.log('Generating documentation...');
  exec('node_modules/.bin/jsdoc -R readme.md -d docs src/js/*', function(err, stdout, stderr) {
    if (err) return done(err);
    console.log('Documentation generated in "docs" directory');
    done();
  });
});

// Task and dependencies to convert ES6 to ES5 with babel;
var babel = require('babelify');
var browserify = require('browserify');
var source = require('vinyl-source-stream');

gulp.task('build', function() {
  var bundler = browserify('./src/js/VertxProtoStub.js', { debug: false }).transform(babel.configure({
    optional: ['runtime'],
    modules: 'ignore'
  }));

  function rebundle() {
    bundler.bundle()
      .on('error', function(err) {
        console.error(err);
        this.emit('end');
      })
      .pipe(source('VertxProtoStub.js'))
      .pipe(gulp.dest('./target'));
  }

  rebundle();
});
