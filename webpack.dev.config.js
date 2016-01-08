var path = require('path');
var webpack = require('webpack');

module.exports = {
  devtool: 'eval',
  entry: [
    'webpack-dev-server/client?http://localhost:3000',
    'webpack/hot/only-dev-server',
    './react-frontend/index'
  ],
  output: {
    path: path.join(__dirname, 'public', 'javascripts'),
    filename: 'bundle.js',
    publicPath: 'http://localhost:3000/'
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin()
  ],
  resolve: {
    extensions: ['', '.js', '.cjsx', '.coffee']
  },
  module: {
    loaders: [
      {test: /\.js$/, loaders: ['react-hot', 'babel'], include: path.join(__dirname, 'react-frontend')},
      {test: /\.cjsx$/, loaders: ['react-hot', 'coffee', 'cjsx'], include: path.join(__dirname, 'react-frontend')},
      {test: /\.coffee$/, loaders: ['coffee'], include: path.join(__dirname, 'react-frontend')}
    ]
  }
};
