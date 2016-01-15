React = require 'react'
Helper = require '../util/Helper'
_ = require 'lodash'

Avatar = require 'material-ui/lib/avatar'
Card = require 'material-ui/lib/card/card'
CardHeader = require 'material-ui/lib/card/card-header'
CardMedia = require 'material-ui/lib/card/card-media'
CardTitle = require 'material-ui/lib/card/card-title'
Colors = require 'material-ui/lib/styles/colors'
List = require 'material-ui/lib/lists/list'
ListItem = require 'material-ui/lib/lists/list-item'
FontIcon = require 'material-ui/lib/font-icon'

SpotifyBox = React.createClass

  albumEndpoint: "https://api.spotify.com/v1/albums/"

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
    @setState selectedArtist: @state.spotifyArtists[idx], album: {}

  handleAlbumClick: (idx) ->
    unless _.has(@state.selectedArtist.albums[idx], 'img')
      url = @albumEndpoint + @state.selectedArtist.albums[idx].id
      $.ajax url,
        type: 'GET'
        dataType: 'json'
        success: (data) =>
          @state.selectedArtist.albums[idx].img = data.images[0].url
          @setState album: @state.selectedArtist.albums[idx]
        error: (jqXHR, textStatus, e) ->
          console.log(e)
    else
      @setState album: @state.selectedArtist.albums[idx]

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

    selectedAlbums = @state.selectedArtist.albums.map (album, idx) =>
      icon = if album.inCollection then "check_box" else "check_box_outline_blank"
      isSelected = _.has(@state, 'album') && album.name == @state.album.name
      color = if isSelected then Colors.amber500 else 'white'
      <ListItem
        key={idx}
        style={backgroundColor: color}
        onTouchTap={@handleAlbumClick.bind(null, idx)}
        primaryText={album.name}
        rightAvatar={<FontIcon className="material-icons" >{icon}</FontIcon>}
        />


    <div style={display: 'flex', justifyContent: 'space-between'}>
      <div style={width: '27%'}>
       <List subheader="Spotify Artist">
        {artists}
       </List>
     </div>

     {
       if _.has(@state, 'album') && !_.isEmpty(@state.album)
         <div style={width: '40%'}>
          <Card>
            <CardHeader
              title={@state.album.name}
              subtitle={@state.selectedArtist.name}
              avatar="http://lorempixel.com/100/100/nature/"/>
            <CardMedia>
              <img src={@state.album.img}/>
            </CardMedia>
            <CardTitle title="Naja" subtitle="OK" />
          </Card>
         </div>
     }

     <div style={width: '27%'}>
        <List subheader={@state.selectedArtist.name + "'s " + "Albums on Spotify"}>
         {selectedAlbums}
        </List>
      </div>

    </div>


module.exports = SpotifyBox
