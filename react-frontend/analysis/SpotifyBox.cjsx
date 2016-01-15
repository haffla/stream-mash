React = require 'react'
Helper = require '../util/Helper'

Avatar = require 'material-ui/lib/avatar'
Colors = require 'material-ui/lib/styles/colors'
List = require 'material-ui/lib/lists/list'
ListItem = require 'material-ui/lib/lists/list-item'
FontIcon = require 'material-ui/lib/font-icon'

SpotifyBox = React.createClass

  getInitialState: () ->
    spotifyArtists: [], selectedArtist: {albums: []}

  componentDidMount: () ->
    $.ajax '/spotify/artists',
      type: 'GET'
      dataType: 'json'
      success: (data) =>
        @setState spotifyArtists: data.artists, selectedArtist: data.artists[0]
      error: (jqXHR, textStatus, e) ->
        console.log(e)

  handleArtistClick: (idx) ->
    @setState selectedArtist: @state.spotifyArtists[idx]

  render: () ->
    artists = @state.spotifyArtists.map (artist, idx) =>
      initials = Helper.getInitials artist.name
      color = if artist.name == @state.selectedArtist.name then Colors.amber500 else 'white'
      <ListItem
        style={backgroundColor: color}
        key={idx}
        primaryText={artist.name}
        onTouchTap={@handleArtistClick.bind(null, idx)}
        leftAvatar={<Avatar>{initials}</Avatar>}/>

    selectedAlbums = @state.selectedArtist.albums.map (album, idx) ->
      icon = if album.inCollection then "check_box" else "check_box_outline_blank"
      <ListItem
        key={idx}
        primaryText={album.name}
        rightAvatar={<FontIcon className="material-icons" >{icon}</FontIcon>}
        />

    <div style={display: 'flex', justifyContent: 'space-between'}>
      <div style={width: '25%'}>
       <List subheader="Spotify Artist">
        {artists}
       </List>
     </div>
     <div style={width: '25%'}>
        <List subheader={@state.selectedArtist.name + "'s " + "Albums on Spotify"}>
         {selectedAlbums}
        </List>
      </div>
    </div>


module.exports = SpotifyBox
