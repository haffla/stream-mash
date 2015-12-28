var path = require('path');

module.exports = {
    entry: [
        './react-frontend/index'
    ],
    output: {
        path: path.join(__dirname, 'public', 'javascripts'),
        filename: 'bundle.js'
    },
    resolve: {
        extensions: ['', '.js', '.cjsx', '.coffee']
    },
    module: {
        loaders: [
            {test: /\.js$/, loaders: ['react-hot', 'babel'], include: path.join(__dirname, 'react-frontend')},
            {test: /\.cjsx$/, loaders: ['react-hot', 'coffee', 'cjsx'], include: path.join(__dirname, 'react-frontend')},
            {test: /\.coffee$/, loaders: ['react-hot', 'coffee'], include: path.join(__dirname, 'react-frontend')}
        ]
    }
};
