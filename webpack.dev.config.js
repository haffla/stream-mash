var path = require('path');
var webpack = require('webpack');
publicPath = 'http://localhost:3000/';


module.exports = {
  devtool: 'eval',
  node: {
    fs: 'empty'
  },
  entry: {
    artistbox: [
      'webpack-dev-server/client?' + publicPath,
      'webpack/hot/only-dev-server',
      './react-frontend/artistbox'
    ],
    analysis: [
      'webpack-dev-server/client?' + publicPath,
      'webpack/hot/only-dev-server',
      './react-frontend/analysis'
    ],
    visualize: [
      'webpack-dev-server/client?' + publicPath,
      'webpack/hot/only-dev-server',
      './react-frontend/visualize'
    ]
  },
  output: {
    path: path.join(__dirname, 'public', 'javascripts'),
    filename: '[name].js',
    publicPath: publicPath
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin()
  ],
  resolve: {
    extensions: ['', '.js', '.cjsx', '.coffee']
  },
  module: {
    loaders: [
      {
        test: /\.js$/,
        loaders: ['react-hot', 'babel'],
        include: path.join(__dirname, 'react-frontend')
      },
      {
        test: /\.cjsx$/,
        loaders: ['react-hot', 'coffee', 'cjsx'],
        include: path.join(__dirname, 'react-frontend')
      },
      {
        test: /\.coffee$/,
        loaders: ['coffee'],
        include: path.join(__dirname, 'react-frontend')
      }
    ]
  }
};
