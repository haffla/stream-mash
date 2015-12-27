var gulp = require('gulp');
var webpack = require('webpack');
var gutil = require('gutil');

gulp.task("build", function() {
    webpack(require('./webpack.build.config'), function(err, stats) {
        if(err) throw new gutil.PluginError("webpack", err);
        gutil.log("[webpack]", stats.toString({
            // output options
        }));
    });
});