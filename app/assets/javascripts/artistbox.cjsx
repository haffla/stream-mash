MainComponent = React.createClass
  getInitialState: () ->
    {data: []}

  componentDidMount: () ->
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

  loadFromDb: (event) ->
    callback = (data) =>
      keys = Object.keys(data)
      if keys.length > 0
        $('#artistList').removeClass('hidden')
      formattedData = keys.map (key) ->
        albums = data[key].map (name) ->
          {name: name}
        {name: key, albums: albums}
      @setState({data: formattedData})

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
          formattedData = Object.keys(data).map (key) ->
            albums = data[key].map (name) ->
              {name: name}
            {name: key, albums: albums}
          $('#artistList').removeClass('hidden')
          $('#dropzone').removeClass('dropped hover')
          @setState({data: formattedData})
        error: (jqXHR, status, error) =>
          console.log('Error: '  + error + '\n' + 'Status: ' + status)
    else
      return window.alert "Nono!! Only XML"

  isMac: () ->
    if navigator.platform.match(/(Mac|iPhone|iPod|iPad)/i) then true else false

  isWindows: () ->
    if navigator.platform.match(/Win/i) then true else false

  render: () ->
    sentence = "The iTunes Music Library file is typically located under "
    <div className="container">
        <div title="Drop your iTunes Library file here" id="dropzone" onDragOver={@preventDef}
             onDrop={@drop} onDragEnter={@dragEnter} onDragLeave={@dragLeave}>
        </div>
        <div className="drop-instruction centered">
          {
            if @isMac()
              <h4>{sentence} <br/>/Users/[username]/Music/iTunes/iTunes Music Library.xml<br/>Drop it on the iTunes Logo!</h4>
            else if @isWindows()
              <h4>{sentence} <br/>C:\Users\[username]\Music\iTunes\iTunes Music Library.xml<br/>Drop it on the iTunes Logo!</h4>
            else
              <h4>You don&apos;t seem to be neither a Mac nor a Windows user.<br/>Drop your iTunes Music Library.xml above!</h4>
          }
        </div>
        <div>
          <form className="form-prevent-default" onSubmit={@loadFromDb}>
            <button className="btn btn-danger" type="submit">Load from DB</button>
          </form>
        </div>
        <ArtistBox data={@state.data} />
    </div>

ArtistBox = React.createClass

  render: () ->
    <div className="hidden" id="artistList">
        <h2>Listing Artists</h2>
        <ArtistList data={@props.data} />
    </div>

ArtistList = React.createClass
  render: () ->
    artists = this.props.data.map (artist) ->
      <div className="artist">
          <h3>{artist.name}</h3>
          <Artist albums={artist.albums}/>
      </div>

    <div className="artistList">
        {artists}
    </div>

Artist = React.createClass
  render: () ->
    <div className="albumList">
        <AlbumList albums={@props.albums}/>
    </div>

AlbumList = React.createClass
  render: () ->
    albums = @props.albums.map (album) ->
      <Album name={album.name} />

    <div className="albumList">
        {albums}
    </div>

Album = React.createClass
  render: () ->
    <div className="album">
        <span>{@props.name}</span>
    </div>

React.render <MainComponent />, document.getElementById('content')
