# JUST A HELPER FOR COMMON TASKS ----------------

class Helper
  @calculateNrOfAlbums: (data) ->
    _.values(data).reduce (x,y) ->
      x + y.albums.length
    , 0

  @formatData: (data) ->
    keys = _.keys(data)
    keys.map (key) ->
      albums = data[key].map (name) ->
        {name: name}
      {name: key, albums: albums}

# REACT CLASSES ---------------------------

MainComponent = React.createClass
  getInitialState: () ->
    {data: []}

  componentDidMount: () ->
    $('#artistBox').removeClass('hidden')
    @setState({data: []})

  preventDef: (event) ->
    event.stopPropagation()
    event.preventDefault()
    $('#dropzone').addClass('hover')

  dragEnter: (event) ->
    @preventDef(event)

  dragLeave: (event) ->
    @preventDef(event)
    $('#dropzone').removeClass('hover')

  originalState: []

  setTheState: (data, setOriginalData = false) ->
    unless nr_artists && nr_albums
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
        if _.size(data) > 0
          $('#artistBox').removeClass('hidden')
        formattedData = Helper.formatData(data)
        @setTheState(formattedData, true)
      else
        window.alert(data.error)
    $.get '/itunes/fromdb', callback, 'json'

  drop: (event) ->
    @preventDef(event)
    unless window.File && window.FileList && window.FileReader
      return window.alert "Your browser does not support the File API"
    file = event.dataTransfer.files[0]
    if file.type is 'text/xml' && file.name.match(/^iTunes (Music )?Library/)
      formData = new FormData()
      formData.append 'file', file
      $('#dropzone').addClass('dropped')
      $.ajax
        url: '/itunes'
        type: 'POST'
        data: formData
        dataType: 'json'
        cache: false
        contentType: false
        processData: false
        success: (data) =>
          if !data.error
            formattedData = Helper.formatData(data)
            $('#artistBox').removeClass('hidden')
            $('#dropzone').removeClass('dropped hover')
            @setTheState(formattedData, true)
          else
            window.alert("We could not read the file.")
        error: (jqXHR, status, error) =>
          console.log('Error: '  + error + '\n' + 'Status: ' + status)
    else
      return window.alert "Nono! Only iTunes Library XML files are allowed"

  isMac: () ->
    if navigator.platform.match(/(Mac|iPhone|iPod|iPad)/i) then true else false

  isWindows: () ->
    if navigator.platform.match(/Win/i) then true else false

  addArrayLengths: (prev,curr) ->
    prev.length + curr.length

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
    $.get '/itunes/spotifyid', {artist: artist}, apiCallback, 'json'
    $(event.target).parents('.panel-heading').siblings('.panel-body').slideToggle()


  render: () ->
    sentence = "The iTunes Music Library file is typically located under "
    dropSentence = "Drop it on the iTunes Logo!"
    <div className="container">

        <div className="row">

            <div className="col-lg-8 col-md-8 col-sm-12">
              <div title="Drop your iTunes Library file here" id="dropzone" onDragOver={@preventDef}
                   onDrop={@drop} onDragEnter={@dragEnter} onDragLeave={@dragLeave}>
              </div>
              <div className="drop-instruction centered">
                {
                  if @isMac()
                    <h4>{sentence} <br/>/Users/[username]/Music/iTunes/iTunes Music Library.xml<br/>{dropSentence}</h4>
                  else if @isWindows()
                    <h4>{sentence} <br/>C:\Users\[username]\Music\iTunes\iTunes Music Library.xml<br/>{dropSentence}</h4>
                  else
                    <h4>You don&apos;t seem to be neither a Mac nor a Windows user.<br/>Drop your iTunes Music Library.xml above!</h4>
                }
              </div>
              <hr/>
              <div className="centered">
                <form className="form-prevent-default" onSubmit={@loadFromDb}>
                  <button className="btn btn-danger" type="submit">Or Load from DB</button>
                </form>
              </div>
            </div>

            <div className="col-lg-4 col-md-4">
              <div className="collection-stats">
                <p>Artists: {@state.nr_artists}</p>
                <p>Albums: {@state.nr_albums}</p>
              </div>
              <div>
                <div className="input-group">
                  <span className="input-group-addon" id="basic-addon1"><i className="fa fa-undo"></i></span>
                  <input type="text" className="form-control" onKeyUp={@filterArtists} placeholder="Artist" aria-describedby="basic-addon1" />
                </div>
              </div>
            </div>

        </div>

        <div className="row">
          <ArtistBox data={@state.data} onButtonClick={@showAlbumList} />
        </div>

    </div>

ArtistBox = React.createClass
  render: () ->
    <div className="hidden" id="artistBox">
        <ArtistList data={@props.data} onButtonClick={@props.onButtonClick}/>
    </div>

ArtistList = React.createClass
  render: () ->
    artists = this.props.data.map (artist, idx) =>
      <div className="artist panel panel-default">

          <div className="panel-heading">
            <div><i className="fa fa-music"></i> {artist.name}</div>
            <button className="btn btn-default album-list-opener" onClick={@props.onButtonClick.bind(@, artist.name, idx)}>
              <i className="fa fa-plus"></i>
            </button>
          </div>

          <div className="panel-body">
            <Artist key={artist.id} albums={artist.albums} />
          </div>

      </div>
    <div className="artistList">
        {artists}
    </div>

Artist = React.createClass
  render: () ->
    <AlbumList albums={@props.albums}/>

AlbumList = React.createClass
  render: () ->
    albums = @props.albums.map (album) ->
      userHas = if album.userHas then "hasAlbum" else ""
      <Album key={album.id} name={album.name} userHas={userHas}/>

    <div className="albumList">
        {albums}
    </div>

Album = React.createClass
  render: () ->
    classes = "album " + @props.userHas
    <div className={classes}>
        <a className="prevent-default" target="_blank" href="#">{@props.name}</a>
    </div>

React.render <MainComponent />, document.getElementById('content')
