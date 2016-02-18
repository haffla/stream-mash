React = require 'react'
Helper = require '../util/Helper'
_ = require 'lodash'

LeftView = require './LeftView'
MidView = require './MidView'

Avatar = require 'material-ui/lib/avatar'
Colors = require 'material-ui/lib/styles/colors'
Dialog = require 'material-ui/lib/dialog'
FontIcon = require 'material-ui/lib/font-icon'
LinearProgress = require 'material-ui/lib/linear-progress'
List = require 'material-ui/lib/lists/list'
ListItem = require 'material-ui/lib/lists/list-item'
RaisedButton = require 'material-ui/lib/raised-button'

StreamingService = React.createClass

  getInitialState: () ->
    artists: [], selectedArtist: {albums: []}, missingAlbumsDialogOpen: false, loaded: false

  componentDidMount: () ->
    $.ajax @props.artistEndpoint,
      type: 'GET'
      dataType: 'json'
      success: (result) =>
        console.log result
        unless _.isEmpty(result.data.artists)
          @setState {
                      artists: result.data.artists
                      selectedArtist: result.data.artists[0]
                      nrArtists: result.data.stats.nrArtists
                      nrAlbums: result.data.stats.nrAlbums
                      nrUserAlbums: result.data.stats.nrUserAlbums
                      albumsOnlyInUserCollection: result.data.stats.albumsOnlyInUserCollection
                      loaded: true
                    }
        else
          @setState artists: [], selectedArtist: {albums: []}, loaded: true
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

  closeMissingAlbumsDialog: () ->
    @setState missingAlbumsDialogOpen: false

  openMissingAlbumsDialog: () ->
    open = @state.albumsOnlyInUserCollection.length > 0
    @setState missingAlbumsDialogOpen: open

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
        missingAlbumsDialogOpen={@state.missingAlbumsDialogOpen}
        style={backgroundColor: color}
        onTouchTap={@handleAlbumClick.bind(null, idx)}
        primaryText={album.name}
        rightAvatar={<FontIcon color="#455a64" className="material-icons" >{icon}</FontIcon>}
        />

    if artists.length > 0
      missingAlbums = @state.albumsOnlyInUserCollection.map (alb,idx) ->
        <tr key={idx}>
          <td>{alb.album}</td><td>{alb.artist.name}</td>
        </tr>
      <div style={display: 'flex', justifyContent: 'space-between'}>
        <Dialog
          title="Missing Albums"
          modal={false}
          open={@state.missingAlbumsDialogOpen}
          onRequestClose={@closeMissingAlbumsDialog}>
          <div style={overflowY: 'auto', maxHeight: 500}>
            <table className="table">
              <tbody>
                {missingAlbums}
              </tbody>
            </table>
          </div>
        </Dialog>
         <LeftView
            name={@props.name}
            artists={artists}
            nrArtists={@state.artists.length}
            nrAlbumsTotal={@state.nrAlbums}
            openMissingAlbumsDialog={@openMissingAlbumsDialog}
            nrAlbumsInUserCollection={@state.nrUserAlbums}
            albumsOnlyInUserCollection={@state.albumsOnlyInUserCollection} />

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
      if @state.loaded
        <div className="centered">
          <h4>Nothing here yet. You need to import music and trigger the analysis</h4>
          <RaisedButton onTouchTap={() -> window.location.href='/collection'} label="Go to Import" primary={true} />
        </div>
      else
        <LinearProgress mode="indeterminate"/>


module.exports = StreamingService
