var path = require('path');

module.exports = {
    entry: [
        './src/index'
    ],
    output: {
        path: path.join(__dirname, '../public/javascripts'),
        filename: 'bundle.js'
    },
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
