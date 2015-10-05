MainComponent = React.createClass
  getInitialState: () ->
    {data: []}

  componentDidMount: () ->
    $('#artistBox').removeClass('hidden')
    @setState({data: [{name: "Tiago", albums: [{name: "Affe"}]}, {name: "Lappen", albums: [{name: "Hund"}]}]})

  preventDef: (event) ->
    event.stopPropagation()
    event.preventDefault()
    $('#dropzone').addClass('hover')

  dragEnter: (event) ->
    @preventDef(event)

  dragLeave: (event) ->
    @preventDef(event)
    $('#dropzone').removeClass('hover')

  originalData: []

  setStateAndOriginalData: (state) ->
    @setState(state)
    @originalData = state

  loadFromDb: (event) ->
    callback = (data) =>
      nr_albums = 0
      keys = Object.keys(data)
      if keys.length > 0
        $('#artistBox').removeClass('hidden')
      formattedData = keys.map (key) ->
        albums = data[key].map (name) ->
          nr_albums++
          {name: name}
        {name: key, albums: albums}
      @setStateAndOriginalData({data: formattedData, nr_artists:keys.length, nr_albums: nr_albums})

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
          $('#artistBox').removeClass('hidden')
          $('#dropzone').removeClass('dropped hover')
          @setState({data: formattedData})
        error: (jqXHR, status, error) =>
          console.log('Error: '  + error + '\n' + 'Status: ' + status)
    else
      return window.alert "Nono! Only iTunes Library XML files are allowed"

  isMac: () ->
    if navigator.platform.match(/(Mac|iPhone|iPod|iPad)/i) then true else false

  isWindows: () ->
    if navigator.platform.match(/Win/i) then true else false

  restoreOldState: () ->
    @setState(@originalData)

  filter: (event) ->
    if event.keyCode == 13
      re = new RegExp(event.target.value, "i")
      newData = @state.data.filter (artist) ->
        artist.name.search(re) != -1
      nr_artists = Object.keys(newData).length
      @setState({data: newData, nr_artists: nr_artists})

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
                  <span onClick={@restoreOldState} className="input-group-addon" id="basic-addon1">@</span>
                  <input type="text" className="form-control" onKeyUp={@filter} placeholder="Artist" aria-describedby="basic-addon1" />
                </div>
              </div>
            </div>

        </div>

        <div className="row">
          <ArtistBox data={@state.data} />
        </div>

    </div>

ArtistBox = React.createClass
  showAlbumList: (event) ->
    $(event.target).parents('.panel-heading').siblings('.panel-body').slideToggle()

  render: () ->
    <div className="hidden" id="artistBox">
        <ArtistList data={@props.data} onButtonClick={@showAlbumList}/>
    </div>

ArtistList = React.createClass
  render: () ->
    artists = this.props.data.map (artist) =>
      <div className="artist panel panel-default">

          <div className="panel-heading">
            <div><i className="fa fa-music"></i> {artist.name}</div>
            <button className="btn btn-default album-list-opener" onClick={@props.onButtonClick}>
              <i className="fa fa-plus"></i>
            </button>
          </div>

          <div className="panel-body">
            <Artist key={artist.id} albums={artist.albums}/>
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
      <Album key={album.id} name={album.name} />

    <div className="albumList">
        {albums}
    </div>

Album = React.createClass
  render: () ->
    <div className="album">
        <a className="prevent-default" target="_blank" href="#">{@props.name}</a>
    </div>

React.render <MainComponent />, document.getElementById('content')
