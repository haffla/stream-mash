path = require 'path'
webpack = require 'webpack'
loaders = require './webpack-loaders'
publicPath = 'http://localhost:3000/'

module.exports =
  devtool: 'eval'
  node:
    fs: 'empty'
  entry:
    artistbox: [
      'webpack-dev-server/client?' + publicPath,
      'webpack/hot/only-dev-server',
      './react-frontend/artistbox'
    ]
    analysis: [
      'webpack-dev-server/client?' + publicPath,
      'webpack/hot/only-dev-server',
      './react-frontend/analysis'
    ]
    visualize: [
      'webpack-dev-server/client?' + publicPath,
      'webpack/hot/only-dev-server',
      './react-frontend/visualize'
    ]
  output:
    path: path.join(__dirname, 'public', 'javascripts')
    filename: '[name].js'
    publicPath: publicPath
  plugins: [new webpack.HotModuleReplacementPlugin()]
  resolve: extensions: ['', '.js', '.cjsx', '.coffee']
  module: loaders: loaders
