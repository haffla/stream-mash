React = require 'react'
ReactDOM = require 'react-dom'
injectTapEventPlugin = require 'react-tap-event-plugin'
AnalysisBox = require './analysis/AnalysisBox'

injectTapEventPlugin()

ReactDOM.render <AnalysisBox />, document.getElementById 'content'
