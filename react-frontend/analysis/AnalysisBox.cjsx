React = require 'react'
StreamingService = require './StreamingService'
SpotifyHelper = require '../util/SpotifyHelper'
DeezerHelper = require '../util/DeezerHelper'
NapsterHelper = require '../util/NapsterHelper'

Colors = require 'material-ui/lib/styles/colors'
Tabs = require 'material-ui/lib/tabs/tabs'
Tab = require 'material-ui/lib/tabs/tab'

AnalysisBox = React.createClass

  tabStyle: {width: '90%', maxWidth: 1700, margin: 'auto', marginTop: 16}
  tabButtonStyle: {backgroundColor: Colors.grey800, color: Colors.green500}

  render: () ->
    <Tabs>
      <Tab label="Spotify" style=@tabButtonStyle>
        <div style=@tabStyle>
          <StreamingService
            name="Spotify"
            showPlayer={true}
            artistEndpoint="/spotify/artists"
            artistDetailEndpoint="/spotify/artist-detail"
            albumDetailEndpoint="/spotify/album-detail"
            helper={SpotifyHelper} />
        </div>
      </Tab>
      <Tab label="Deezer" style=@tabButtonStyle>
        <div style=@tabStyle>
          <StreamingService
            name="Deezer"
            showPlayer={false}
            artistEndpoint="/deezer/artists"
            artistDetailEndpoint="/deezer/artist-detail"
            albumDetailEndpoint="/deezer/album-detail"
            helper={DeezerHelper} />
        </div>
      </Tab>
      <Tab label="Napster" style=@tabButtonStyle>
        <div style=@tabStyle>
          <StreamingService
            name="Napster"
            showPlayer={false}
            artistEndpoint="/napster/artists"
            artistDetailEndpoint="/napster/artist-detail"
            albumDetailEndpoint="/napster/album-detail"
            helper={NapsterHelper} />
        </div>
      </Tab>
    </Tabs>

module.exports = AnalysisBox
