React = require 'react'
Helper = require '../util/Helper'
ItunesUpload = require './ItunesUpload'
_ = require 'lodash'
ProgressBar = require 'progressbar.js'

Album = require './Album'
Artist = require './Artist'
AlbumList = require './AlbumList'
ArtistList = require './ArtistList'

ws = new WebSocket(window.streamingservice.url)
String::startsWith ?= (s) -> @slice(0, s.length) == s
line = new ProgressBar.Line('.spacer', {
  color: '#FCB03C'
  })


ArtistBox = React.createClass
  getInitialState: () ->
    {data: []}

  componentDidMount: () ->

    ws.onopen = () ->
      ws.send(window.streamingservice.name)

    ws.onmessage = (data) =>
      if data.data.startsWith "progress"
        line.animate(data.data.split(':')[1])
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
      line.animate(0)
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
              <div className="collection-stats">
                <p>Artists: {@state.nr_artists}</p>
                <p>Albums: {@state.nr_albums}</p>
              </div>
              <div>
                <div className="input-group">
                  <span className="input-group-addon" id="basic-addon1"><i className="fa fa-music"></i></span>
                  <input type="text" className="form-control" onKeyUp={@filterArtists} placeholder="Filter by artist" aria-describedby="basic-addon1" />
                </div>
              </div>
            </div>

            <div className="col-md-5 col-md-offset-3 col-sm-6 col-xs-6">
              <button className="btn btn-primary" data-target="#fileuploadmodal" role="button" className="btn btn-large btn-primary" data-toggle="modal">
                Import Music from Itunes XML File
              </button>
              <ItunesUpload ws={ws} />
            </div>
        </div>

        <div className="row">
          <div id="artistBox">
              <ArtistList data={@state.data} onButtonClick={@showAlbumList} />
          </div>
        </div>
    </div>

module.exports = ArtistBox
