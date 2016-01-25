var path = require('path');

var reactFrontend = 'react-frontend';

module.exports = {
    entry: {
      artistbox: ['./' + reactFrontend + '/artistbox'],
      analysis: ['./' + reactFrontend + '/analysis'],
      visualize: ['./' + reactFrontend + '/visualize'],
    },
    output: {
        path: path.join(__dirname, reactFrontend, 'dist'),
        filename: '[name].js'
    },
    resolve: {
        extensions: ['', '.js', '.cjsx', '.coffee']
    },
    module: {
        loaders: [
            {test: /\.js$/, loaders: ['react-hot', 'babel'], include: path.join(__dirname, reactFrontend)},
            {test: /\.cjsx$/, loaders: ['react-hot', 'coffee', 'cjsx'], include: path.join(__dirname, reactFrontend)},
            {test: /\.coffee$/, loaders: ['react-hot', 'coffee'], include: path.join(__dirname, reactFrontend)}
        ]
    }
};
