var gulp = require('gulp');
var webpack = require('webpack');
var gutil = require('gutil');
var rename = require('gulp-rename');
var uglify = require('gulp-uglify');

/* use webpack to compile all code into one file and put it in the public/javascripts folder */
gulp.task("webpack", function() {
    webpack(require('./webpack.build.config'), function(error, stats) {
        if(error) {
            throw new gutil.PluginError("webpack", error);
        }
        gutil.log("[webpack]", stats.toString());
    });
});

var JS_FOLDER = 'public/javascripts';

gulp.task('uglify', ['webpack'], function() {
  return gulp.src(JS_FOLDER + '/bundle.js')
    .pipe(uglify())
    .pipe(rename({ extname: '.min.js' }))
    .pipe(gulp.dest(JS_FOLDER));
});

gulp.task('build', ['webpack', 'uglify']);