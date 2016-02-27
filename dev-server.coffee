webpack = require 'webpack'
WebpackDevServer = require 'webpack-dev-server'
config = require './webpack.dev.config'
port = 3000

new WebpackDevServer( webpack(config),
      publicPath: config.output.publicPath
      hot: true
      historyApiFallback: true
  ).listen port, 'localhost', (err) ->
      if err
        console.log(err)
      console.log('Listening at localhost on port ' + port)
