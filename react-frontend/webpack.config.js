var path = require('path');
var webpack = require('webpack');

module.exports = {
  devtool: 'eval',
  entry: [
    'webpack-dev-server/client?http://localhost:3000',
    'webpack/hot/only-dev-server',
    './src/index'
  ],
  output: {
    path: path.join(__dirname, '../public/javascripts'),
    filename: 'bundle.js',
    publicPath: '/static/'
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin()
  ],
  resolve: {
    extensions: ['', '.js', '.cjsx', '.coffee']
  },
  module: {
    loaders: [
      {test: /\.js$/, loaders: ['react-hot', 'babel'], include: path.join(__dirname, 'src')},
      {test: /\.cjsx$/, loaders: ['react-hot', 'coffee', 'cjsx']},
      {test: /\.coffee$/, loaders: ['react-hot', 'coffee']}
    ]
  }
};
