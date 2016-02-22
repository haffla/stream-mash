React = require 'react'
Helper = require '../util/Helper'
_ = require 'lodash'

LeftView = require './LeftView'
MidView = require './MidView'
RightView = require './RightView'
MissingItemsDialog = require './MissingItemsDialog'

LinearProgress = require 'material-ui/lib/linear-progress'
RaisedButton = require 'material-ui/lib/raised-button'

StreamingService = React.createClass

  getInitialState: () ->
    artists: [], selectedArtist: {albums: []}, selectedAlbum: {}, missingItemsDialog: {open: false, title: 'Missing Albums', items: []}, loaded: false

  getServiceLinkForArtistId: (id, service) ->
    if _.isEmpty(id)
      content = '-'
    else
      if service is 'Spotify'
        link = 'https://play.spotify.com/artist/' + id
      else if service is 'Deezer'
        link = 'http://www.deezer.com/artist/' + id
      else if service is 'Napster'
        link = 'http://app.napster.com/artist/' + id
      content = <a href={link} target="_blank">{service}</a>
    <span>{content}</span>

  componentDidMount: () ->
    $.ajax @props.artistEndpoint,
      type: 'GET'
      dataType: 'json'
      success: (result) =>
        console.log result
        unless _.isEmpty(result.data.artists)
          missingAlbums = result.data.stats.albumsOnlyInUserCollection.map (alb,idx) ->
            <tr key={idx}>
              <td>{alb.album}</td><td>{alb.artist.name}</td>
            </tr>
          missingArtists = result.data.stats.absentArtists.map (art,idx) =>
            <tr key={idx}>
              <td>{art.name}</td>
              <td>{@getServiceLinkForArtistId(art.spotifyId, "Spotify")}</td>
              <td>{@getServiceLinkForArtistId(art.deezerId, "Deezer")}</td>
              <td>{@getServiceLinkForArtistId(art.napsterId, "Napster")}</td>
            </tr>
          @setState {
                      artists: result.data.artists
                      selectedArtist: result.data.artists[0]
                      nrArtists: result.data.stats.nrArtists
                      nrAlbums: result.data.stats.nrAlbums
                      nrUserAlbums: result.data.stats.nrUserAlbums
                      absentArtists: missingArtists
                      missingAlbums: missingAlbums
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
        data: {'id': @state.selectedArtist.albums[idx].id, 'name': @state.selectedArtist.albums[idx].name}
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

  closeMissingItemsDialog: () ->
    @setState missingItemsDialog: {open: false}

  openMissingItemsDialog: (type) ->
    if type is 'album'
      open = @state.missingAlbums.length > 0
      title = 'Missing Albums'
      items = @state.missingAlbums
    else
      open = @state.absentArtists.length > 0
      title = 'Missing Artists'
      items = @state.absentArtists
    @setState missingItemsDialog: {open: open, title: title, items: items}

  render: () ->
    if @state.artists.length > 0
      <div style={display: 'flex', justifyContent: 'space-between'}>
        <MissingItemsDialog
          title={@state.missingItemsDialog.title}
          open={@state.missingItemsDialog.open}
          onRequestClose={@closeMissingItemsDialog}
          items={@state.missingItemsDialog.items} />
        <LeftView
          name={@props.name}
          artists={@state.artists}
          nrArtists={@state.artists.length}
          nrAlbumsTotal={@state.nrAlbums}
          selectedArtist={@state.selectedArtist.name}
          handleArtistClick={@handleArtistClick}
          nrAbsentArtist={@state.absentArtists.length}
          openMissingItemsDialog={@openMissingItemsDialog}
          nrAlbumsInUserCollection={@state.nrUserAlbums}
          nrMissingAlbums={@state.missingAlbums.length} />

        <MidView
          showPlayer={@props.showPlayer}
          selectedAlbum={@state.selectedAlbum}
          selectedArtist={@state.selectedArtist}
          name={@props.name}
          />

        <RightView
          artist={@state.selectedArtist.name}
          handleAlbumClick={@handleAlbumClick}
          selectedAlbum={@state.selectedAlbum.name}
          name={@props.name}
          selectedAlbums={@state.selectedArtist.albums} />
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
