var gulp = require('gulp');
var webpack = require('webpack');
var gutil = require('gutil');
var rename = require('gulp-rename');
var uglify = require('gulp-uglify');

/* use webpack to compile all code into one file */
gulp.task("webpack", function() {
    webpack(require('./webpack.build.config'), function(error, stats) {
        if(error) {
            throw new gutil.PluginError("webpack", error);
        }
    });
});

var JS_FOLDER = 'public/javascripts';
var DIST = 'react-frontend/dist';

/* uglify javascript and output to public/javascripts folder */
gulp.task('uglify', ['webpack'], function() {
  return gulp.src('./' + DIST + '/bundle.js')
    .pipe(uglify())
    .pipe(rename({ extname: '.min.js' }))
    .pipe(gulp.dest('./' + JS_FOLDER));
});

gulp.task('build', ['webpack', 'uglify']);
