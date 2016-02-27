path = require 'path'
loaders = require './webpack-loaders'
reactFrontend = 'react-frontend'

module.exports =
    node: fs: 'empty'
    entry:
      artistbox: ['./' + reactFrontend + '/artistbox']
      analysis: ['./' + reactFrontend + '/analysis']
      visualize: ['./' + reactFrontend + '/visualize']
    output:
      path: path.join __dirname, reactFrontend, 'dist'
      filename: '[name].js'
    resolve: extensions: ['', '.js', '.cjsx', '.coffee']
    module: loaders: loaders
