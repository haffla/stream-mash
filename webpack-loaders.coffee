path = require 'path'

module.exports = [
  {
    test: /\.js$/
    loaders: ['react-hot', 'babel']
    include: path.join __dirname, 'react-frontend'
  },
  {
    test: /\.cjsx$/
    loaders: ['react-hot', 'coffee', 'cjsx']
    include: path.join __dirname, 'react-frontend'
  },
  {
    test: /\.coffee$/
    loaders: ['coffee']
    include: path.join __dirname, 'react-frontend'
  }
]
