React = require 'react'
StreamingServiceBox = require './StreamingServiceBox'

Colors = require 'material-ui/lib/styles/colors'
Tabs = require 'material-ui/lib/tabs/tabs'
Tab = require 'material-ui/lib/tabs/tab'

AnalysisBox = React.createClass

  tabStyle: {width: '90%', maxWidth: 1700, margin: 'auto', marginTop: 16}

  render: () ->
    <Tabs>
      <Tab label="Spotify" style={backgroundColor: Colors.green700}>
        <div style=@tabStyle>
          <StreamingServiceBox
            name="Spotify"
            showPlayer={true}
            artistEndpoint="/spotify/artists"
            artistDetailEndpoint="/spotify/artist-detail"
            albumDetailEndpoint="/spotify/album-detail" />
        </div>
      </Tab>
      <Tab label="Deezer" style={backgroundColor: Colors.grey800}>
        <div style=@tabStyle>
          <StreamingServiceBox
            name="Deezer"
            showPlayer={false}
            artistEndpoint="/spotify/artists"
            artistDetailEndpoint="/spotify/artist-detail"
            albumDetailEndpoint="/spotify/album-detail" />
        </div>
      </Tab>
    </Tabs>

module.exports = AnalysisBox
