gulp = require 'gulp'
gutil = require 'gutil'
webpack = require 'webpack'
rename = require 'gulp-rename'
uglify = require 'gulp-uglify'
config = require './webpack.build.config'

JS_FOLDER = 'public/javascripts'
DIST = 'react-frontend/dist'
scripts = [
    './' + DIST + '/artistbox.js',
    './' + DIST + '/visualize.js',
    './' + DIST + '/analysis.js'
]

# use webpack to compile all code into one file
gulp.task 'webpack', () ->
    webpack require('./webpack.build.config'), (error, stats) ->
        if error
          throw new gutil.PluginError 'webpack', error
        gutil.log '[webpack]', stats.toString()

# uglify javascript and output to public/javascripts folder
gulp.task 'uglify', () ->
  gulp.src scripts
    .pipe uglify()
    .pipe rename { extname: '.min.js' }
    .pipe gulp.dest('./' + JS_FOLDER)
