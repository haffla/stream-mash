React = require 'react'
Helper = require '../util/Helper'
UploadDialog = require './upload/UploadDialog'
ControlBar = require './ControlBar'
ArtistDetail = require './ArtistDetail'
ArtistList = require './ArtistList'
_ = require 'lodash'

Badge = require 'material-ui/lib/badge'
CircularProgress = require 'material-ui/lib/circular-progress'
Colors = require 'material-ui/lib/styles/colors'
LinearProgress = require 'material-ui/lib/linear-progress'
RaisedButton = require 'material-ui/lib/raised-button'

TextField = require 'material-ui/lib/text-field'

ws = new WebSocket(window.streamingservice.url)
String::startsWith ?= (s) -> @slice(0, s.length) == s

ArtistBox = React.createClass
  getInitialState: () ->
    data: [], progress: 0, nrCols: 3, dialog: {open: false, type: "itunes"}, analysing: false, isAnalysed: false

  componentDidMount: () ->

    ws.onopen = () -> ws.send(window.streamingservice.name)

    ws.onmessage = (data) =>
      if data.data.startsWith "progress"
        progress = data.data.split(':')[1] * 100
        @setState {progress: progress}
      else if data.data is 'done'
        @loadFromDb()

  originalState: []

  setTheState: (data, setOriginalData = false) ->
    nr_albums = Helper.calculateNrOfAlbums(data)
    nr_artists = _.keys(data).length
    if setOriginalData
      @setState {data: data, nr_artists: nr_artists, nr_albums: nr_albums}, () ->
        @originalState = @state
    else
      @setState {data: data, nr_artists: nr_artists, nr_albums: nr_albums}


  loadFromDb: (event) ->
    callback = (data) =>
      if !data.error
        console.log(data)
        unless _.has(@state, 'selectedArtist')
          selectedArtist = data[0]
          selectedArtist.idx = 0
          @setState selectedArtist: selectedArtist
        @setTheState(data, true)
      @setState({progress: 0})
    $.get '/collection/fromdb', callback, 'json'

  filterArtists: (event) ->
    re = new RegExp(event.target.value, "i")
    newData = @originalState.data.filter (artist) ->
      artist.name.search(re) != -1
    if newData.length > 0
      nr_artists = newData.length
      nr_albums = Helper.calculateNrOfAlbums(newData)

    @setState {data: newData, nr_artists: nr_artists  || 0, nr_albums: nr_albums || 0}

  handleSlider: (e, value) ->
    @setState {nrCols: value}

  handleArtistClick: (idx) ->
    if _.isEmpty(@state.data[idx].img)
      $.ajax '/artist/image',
        type: 'GET'
        data: {artist: @state.data[idx].name}
        dataType: 'json'
        success: (data) =>
          unless data.error
            @state.data[idx].img = data.img
        error: (jqXHR, textStatus, e) ->
          console.log(e)
        complete: () =>
          @state.data[idx].idx = idx
          @setState selectedArtist: @state.data[idx]
    else
      @state.data[idx].idx = idx
      @setState selectedArtist: @state.data[idx]

  handleArtistSlide: (idx, evt, val) ->
    clearTimeout(window.delayedAjaxCall)
    @state.data[idx].rating = val
    @setState data: @state.data
    artistToChange = @state.data[idx]
    ajaxCall = () ->
      $.ajax '/artist/rating',
        type: 'POST'
        dataType: 'json'
        contentType: 'application/json; charset=utf-8'
        data: JSON.stringify artistToChange
        success: (data) ->
          console.log(data)
        error: (jqXHR, textStatus, e) ->
          console.log(e)
    window.delayedAjaxCall = setTimeout ajaxCall, 777

  closeDialog: () ->
    @setState {dialog: {open: false}}

  openDialog: (dialogType) ->
    @setState {dialog: {open: true, type: dialogType}}

  handleNewRequest: (artist,idx) ->
    @setState {selectedArtist: @state.data[idx]}

  handleStreamingServiceSelection: (event, item) ->
    window.location.href = "#{item.props.data}/login"

  handleAnalyseClick: () ->
    @setState analysing: true
    $.ajax '/user/analysis',
      type: 'POST'
      dataType: 'json'
      success: (data) =>
        if data.success
          @setState analysing: false, isAnalysed: true
      error: (jqXHR, textStatus, e) ->
        console.log(e)

  render: () ->
    <div style={width: '80%', margin: 'auto'}>
        <div className="row" style={display: 'flex', marginBottom: '25px'}>
          <div style={width: 100, height: 50, marginRight: 50}>
            {
              if !_.isEmpty(@state.data)
                if @state.analysing
                  <div>
                  <CircularProgress mode="indeterminate" size={0.5} />
                  </div>
                else
                  <div style={marginTop: 10, witdth: 100}>
                  <RaisedButton disabled={@state.isAnalysed} label="Analyse" onTouchTap={@handleAnalyseClick} />
                  </div>
            }
          </div>
          <div>
              <TextField
                underlineStyle={{borderColor:Colors.amber300}}
                hintText="Filter by Artists"
                onChange={@filterArtists} />
              <Badge badgeContent={@state.nr_artists || 0} primary={true} />
          </div>
        </div>

        <div className="row">
          <ControlBar
            disabled={_.isEmpty @state.data}
            handleSlider={@handleSlider}
            openDialog={@openDialog}
            handleStreamingServiceSelection={@handleStreamingServiceSelection} />
        </div>

        {
          if @state.selectedArtist
            <ArtistDetail
              autoCompleteSource={@state.data.map (artist) -> artist.name}
              onNewRequest={@handleNewRequest}
              onArtistSlideChange={@handleArtistSlide}
              selectedArtist={@state.selectedArtist} />
          else
            <h4>Start importing music!</h4>
        }

        <div className="row">
          <div className="row progress-container" style={marginTop: '20px', marginBottom: '20px'}>
            <LinearProgress mode="determinate" value={@state.progress} />
          </div>
          <div id="artistBox">
              <ArtistList
                data={@state.data}
                onArtistClick={@handleArtistClick}
                onArtistSlideChange={@handleArtistSlide}
                nrCols={this.state.nrCols} />
          </div>
        </div>

        <UploadDialog ws={ws} open={@state.dialog.open} type={@state.dialog.type} handleClose={@closeDialog} />
    </div>

module.exports = ArtistBox
