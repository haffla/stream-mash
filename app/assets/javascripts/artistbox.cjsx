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


  render: () ->
    <div className="container">
        <div title="Drop your iTunes Library file here" id="dropzone" onDragOver={@preventDef}
             onDrop={@drop} onDragEnter={@dragEnter} onDragLeave={@dragLeave}>
            <p>iTunes Library</p>
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
