React = require 'react'
ReactDOM = require 'react-dom'
ChartsBox = require './visualize/ChartsBox.cjsx'
injectTapEventPlugin = require 'react-tap-event-plugin'

injectTapEventPlugin()

ReactDOM.render <ChartsBox />, document.getElementById 'content'
