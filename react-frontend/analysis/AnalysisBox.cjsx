React = require 'react'
SpotifyBox = require './SpotifyBox'

AnalysisBox = React.createClass
  render: () ->
    <div style={width: '80%', margin: 'auto'}>
      <SpotifyBox />
    </div>

module.exports = AnalysisBox
