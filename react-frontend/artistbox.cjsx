React = require 'react'
ReactDOM = require 'react-dom'
ArtistBox = require './user_collection/ArtistBox.cjsx'
injectTapEventPlugin = require 'react-tap-event-plugin'

injectTapEventPlugin()

ReactDOM.render <ArtistBox />, document.getElementById 'content'
