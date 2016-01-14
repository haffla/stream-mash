var gulp = require('gulp');
var gutil = require('gutil');
var webpack = require('webpack');
var rename = require('gulp-rename');
var uglify = require('gulp-uglify');
var config = require('./webpack.build.config');

/* use webpack to compile all code into one file */
gulp.task("webpack", function() {
    webpack(require('./webpack.build.config'), function(error, stats) {
        if(error) {
            throw new gutil.PluginError("webpack", error);
        }
        gutil.log("[webpack]", stats.toString());
    });
});

var JS_FOLDER = 'public/javascripts';
var DIST = 'react-frontend/dist';
var scripts = [
    './' + DIST + '/artistbox.js',
    './' + DIST + '/analysis.js'
];

/* uglify javascript and output to public/javascripts folder */
gulp.task('uglify', function() {
  return gulp.src(scripts)
    .pipe(uglify())
    .pipe(rename({ extname: '.min.js' }))
    .pipe(gulp.dest('./' + JS_FOLDER));
});
