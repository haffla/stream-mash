React = require 'react'
Helper = require '../util/Helper'
ItunesUpload = require './ItunesUpload'
_ = require 'lodash'

LinearProgress = require 'material-ui/lib/linear-progress'
RaisedButton = require 'material-ui/lib/raised-button'
TextField = require 'material-ui/lib/text-field'
Badge = require 'material-ui/lib/badge'
Slider = require 'material-ui/lib/slider'
Toolbar = require 'material-ui/lib/toolbar/toolbar';
ToolbarGroup = require 'material-ui/lib/toolbar/toolbar-group';
ToolbarSeparator = require 'material-ui/lib/toolbar/toolbar-separator';
List = require 'material-ui/lib/lists/list';
ListItem = require 'material-ui/lib/lists/list-item';
IconMenu = require 'material-ui/lib/menus/icon-menu';
MenuItem = require 'material-ui/lib/menus/menu-item';

Colors = require 'material-ui/lib/styles/colors'

Album = require './Album'
Artist = require './Artist'
AlbumList = require './AlbumList'
ArtistList = require './ArtistList'

ws = new WebSocket(window.streamingservice.url)
String::startsWith ?= (s) -> @slice(0, s.length) == s

ArtistBox = React.createClass
  getInitialState: () ->
    {data: [], progress: 0, nrCols: 3}

  componentDidMount: () ->

    ws.onopen = () -> ws.send(window.streamingservice.name)

    ws.onmessage = (data) =>
      if data.data.startsWith "progress"
        progress = data.data.split(':')[1] * 100
        @setState({progress: progress})
      else if data.data is 'done'
        @loadFromDb()

  originalState: []

  setTheState: (data, setOriginalData = false) ->
    nr_albums = Helper.calculateNrOfAlbums(data)
    nr_artists = _.keys(data).length
    if setOriginalData
      @setState({data: data, nr_artists: nr_artists, nr_albums: nr_albums, selectedArtist: data[0]}, () ->
        @originalState = @state
      )
    else
      @setState({data: data, nr_artists: nr_artists, nr_albums: nr_albums, selectedArtist: data[0]})


  loadFromDb: (event) ->
    callback = (data) =>
      if !data.error
        console.log(data)
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

  handleSlider: (e, value) ->
    @setState({nrCols: value})

  handleArtistClick: (idx) ->
    @setState({selectedArtist: @state.data[idx]})

  render: () ->
    <div style={width: '80%', margin: 'auto'}>
        <div className="row" style={display: 'flex', justifyContent: 'space-between', marginBottom: '25px'}>
            <div>
                <TextField
                  underlineStyle={{borderColor:Colors.amber300}}
                  hintText="Filter by Artists"
                  onChange={@filterArtists} />
                <Badge badgeContent={@state.nr_artists || 0} primary={true} />
            </div>

            <div>
              <RaisedButton data-target="#fileuploadmodal" data-toggle="modal" label="Import Music from Itunes XML File" primary={true} />
              <ItunesUpload ws={ws} />
            </div>
        </div>

        <div className="row">
          <Toolbar>
            <ToolbarGroup style={width: '25%'}>
              <Slider
                description="Number of columns"
                name="colSlider"
                defaultValue={3}
                step={1}
                min={1}
                max={5}
                onChange={@handleSlider} />
            </ToolbarGroup>
          </Toolbar>
        </div>

        {
          if @state.selectedArtist

            <div className="row" style={display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '25px'}>

              <List insetSubheader={true} subheader="Selected Artist" style={width: '33%'}>
                <ListItem primaryText={@state.selectedArtist.name} />
              </List>

              <List insetSubheader={true} subheader={@state.selectedArtist.name + "'s Albums"} style={width: '33%'}>
                {
                  @state.selectedArtist.albums.map (alb, idx) ->
                    <ListItem key={idx} primaryText={alb.name} />
                }
              </List>

              <List insetSubheader={true} subheader={@state.selectedArtist.name + "'s Tracks"} style={width: '33%'}>
                {
                  tracks = @state.selectedArtist.albums.map (alb) -> alb.tracks
                  _.flatten(tracks).map (track, idx) ->
                    <ListItem key={idx} primaryText={track} />
                }
              </List>

            </div>

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
                nrCols={this.state.nrCols} />
          </div>
        </div>
    </div>

module.exports = ArtistBox
