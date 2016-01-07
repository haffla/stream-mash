React = require 'react'
Helper = require '../util/Helper'
ItunesUpload = require './ItunesUpload'
_ = require 'lodash'

LinearProgress = require 'material-ui/lib/linear-progress'
RaisedButton = require 'material-ui/lib/raised-button'
TextField = require 'material-ui/lib/text-field'
Badge = require 'material-ui/lib/badge'

Album = require './Album'
Artist = require './Artist'
AlbumList = require './AlbumList'
ArtistList = require './ArtistList'

ws = new WebSocket(window.streamingservice.url)
String::startsWith ?= (s) -> @slice(0, s.length) == s

ArtistBox = React.createClass
  getInitialState: () ->
    {data: [], progress: 0}

  componentDidMount: () ->

    ws.onopen = () ->
      ws.send(window.streamingservice.name)

    ws.onmessage = (data) =>
      if data.data.startsWith "progress"
        progress = data.data.split(':')[1] * 100
        console.log(progress)
        @setState({progress: progress})
      else if data.data is 'done'
        @loadFromDb()

  originalState: []

  setTheState: (data, setOriginalData = false) ->
    nr_albums = Helper.calculateNrOfAlbums(data)
    nr_artists = _.keys(data).length
    if setOriginalData
      @setState({data: data, nr_artists: nr_artists, nr_albums: nr_albums}, () ->
        @originalState = @state
      )
    else
      @setState({data: data, nr_artists: nr_artists, nr_albums: nr_albums})


  loadFromDb: (event) ->
    callback = (data) =>
      if !data.error
        @setTheState(data, true)
      if window.itunes.openmodal is 'yes'
        $('#fileuploadmodal').modal('show')
      @setState({progress: 0})
    $.get '/collection/fromdb', callback, 'json'

  filterArtists: (event) ->
    re = new RegExp(event.target.value, "i")
    newData = @originalState.data.filter (artist) ->
      artist.name.search(re) != -1
    if newData.length > 0
      nr_artists = newData.length
      nr_albums = Helper.calculateNrOfAlbums(newData)

    @setState({data: newData, nr_artists: nr_artists  || 0, nr_albums: nr_albums || 0})

  showAlbumList: (artist, idx, event) ->
    #TODO: move all spotify api logic to server
    apiCallback = (data) =>
      unless data.error
        unless @state.data[idx].fetched
          spotifyApiCallback = (albums) =>
            spotifyAlbums = albums.items.map (album) ->
              {name: album.name}
            existingAlbums = @state.data[idx].albums
            namesOfExistingAlbums = existingAlbums.map (album) ->
              album.name
            result = _.union(existingAlbums,spotifyAlbums)
            result = result.map (album) ->
              userHas = if album.name in namesOfExistingAlbums then true else false
              {name: album.name, userHas: userHas}
            result = _.uniq(result, 'name')
            _.set(@state.data[idx], 'albums', result)
            _.set(@state.data[idx], 'fetched', true)
            @setTheState(@state.data)
          # maybe also concider album_type=single
          $.get "https://api.spotify.com/v1/artists/#{data.spotify_id}/albums?album_type=album", spotifyApiCallback, 'json'
      else
        # Artist does not exist on Spotify
    $.get '/spotify/spotifyid', {artist: artist}, apiCallback, 'json'
    $(event.target).parents('.panel-heading').siblings('.panel-body').slideToggle()


  render: () ->
    <div className="container">
        <div className="row">
            <div className="col-md-4 col-sm-6 col-xs-6">
              <div>
                <TextField hintText="Filter by Artists" onChange={@filterArtists} />
                <Badge badgeContent={@state.nr_artists || 0} primary={true} />
              </div>
            </div>

            <div className="col-md-4 col-sm-6 col-xs-6">
              <RaisedButton data-target="#fileuploadmodal" data-toggle="modal" label="Import Music from Itunes XML File" primary={true} />
              <ItunesUpload ws={ws} />
            </div>
        </div>

        <div className="row progress-container" style={marginTop: '20px'}>
          <LinearProgress mode="determinate" value={@state.progress} />
        </div>

        <div className="row">
          <div id="artistBox">
              <ArtistList data={@state.data} onButtonClick={@showAlbumList} />
          </div>
        </div>
    </div>

module.exports = ArtistBox
