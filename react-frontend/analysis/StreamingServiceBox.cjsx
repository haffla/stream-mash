React = require 'react'
Helper = require '../util/Helper'
_ = require 'lodash'

LeftView = require './LeftView'
MidView = require './MidView'

Avatar = require 'material-ui/lib/avatar'
Colors = require 'material-ui/lib/styles/colors'
FontIcon = require 'material-ui/lib/font-icon'
List = require 'material-ui/lib/lists/list'
ListItem = require 'material-ui/lib/lists/list-item'

StreamingServiceBox = React.createClass

  getInitialState: () ->
    artists: [], selectedArtist: {albums: []}

  componentDidMount: () ->
    $.ajax @props.artistEndpoint,
      type: 'GET'
      dataType: 'json'
      success: (data) =>
        unless _.isEmpty(data.artists)
          @setState artists: data.artists, selectedArtist: data.artists[0]
        else
          @setState artists: [], selectedArtist: {albums: []}
      error: (jqXHR, textStatus, e) ->
        console.log(e)

  handleArtistClick: (idx) ->
    if _.isEmpty(@state.artists[idx].img)
      $.ajax @props.artistDetailEndpoint,
        type: 'GET'
        dataType: 'json'
        data: {'id': @state.artists[idx].id}
        success: (data) =>
          img = @props.helper.getImage data, 'big'
          @state.artists[idx].img = img
        error: (jqXHR, textStatus, e) ->
          console.log(e)
        complete: () =>
          @setState selectedArtist: @state.artists[idx], selectedAlbum: {}
    else
      @setState selectedArtist: @state.artists[idx], selectedAlbum: {}

  handleAlbumClick: (idx) ->
    unless _.has(@state.selectedArtist.albums[idx], 'img')
      $.ajax @props.albumDetailEndpoint,
        type: 'GET'
        data: {'id': @state.selectedArtist.albums[idx].id}
        dataType: 'json'
        success: (data) =>
          img = @props.helper.getImage data
          @state.selectedArtist.albums[idx].img = img
          @state.selectedArtist.albums[idx].tracks = data.tracks
          @setState selectedAlbum: @state.selectedArtist.albums[idx]
        error: (jqXHR, textStatus, e) ->
          console.log(e)
    else
      @setState selectedAlbum: @state.selectedArtist.albums[idx]

  render: () ->
    artists = @state.artists.map (artist, idx) =>
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
      isSelected = _.has(@state, 'selectedAlbum') && album.name == @state.selectedAlbum.name
      color = if isSelected then Colors.amber500 else 'white'
      <ListItem
        key={idx}
        style={backgroundColor: color}
        onTouchTap={@handleAlbumClick.bind(null, idx)}
        primaryText={album.name}
        rightAvatar={<FontIcon color="#455a64" className="material-icons" >{icon}</FontIcon>}
        />

    if artists.length > 0
      <div style={display: 'flex', justifyContent: 'space-between'}>
         <LeftView
            name={@props.name}
            artists={artists}
            nrArtists={@state.artists.length}
            nrAlbumsTotal={Helper.calculateNrOfAlbums(@state.artists)}
            nrAlbumsInUserCollection={Helper.calculateNrOfAlbumsInCollection(@state.artists)} />

         <MidView
          showPlayer={@props.showPlayer}
          selectedAlbum={@state.selectedAlbum}
          selectedArtist={@state.selectedArtist}
          name={@props.name}
          />

         {#Right View}
         <div style={width: '33%'}>
            <List subheader={@state.selectedArtist.name + "'s " + "Albums on " + @props.name}>
             {selectedAlbums}
            </List>
         </div>
      </div>
    else
      <p>Nothing here yet.</p>


module.exports = StreamingServiceBox
