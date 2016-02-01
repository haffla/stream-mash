_ = require 'lodash'

React = require 'react'
Helper = require '../../util/Helper'
Uploader = require '../../util/Uploader'

AudioFileUpload = React.createClass

  componentWillMount: () ->
    jQuery.event.props.push 'dataTransfer'

  uploader: new Uploader('/fileupload')

  drop: (event) ->
    Helper.preventDef(event)
    unless typeof Promise is not 'undefined'
      files = event.target.files || event.dataTransfer.files
      @readFiles(files).then (data) =>
        filtered = data.filter @filterData
        if filtered.length > 0
          successCallback = () =>
            @props.ws.send('audio')
            @props.handleClose()
          @uploader.upload(
              JSON.stringify(filtered)
              'application/json'
              successCallback
            )
    else
      alert 'Sorry. Your browser does not support Javascript Promises which come into play when we read your files.'

  filterData: (obj) ->
    !(_.isUndefined(obj.artist) or _.isUndefined(obj.title) or _.isUndefined(obj.album))

  dragEnter: (event) ->
    Helper.preventDef(event)

  readFiles: (files) ->
    promises = (idx for idx in [0...files.length]).map (i) =>
      file = files[i]
      if file.type.match(/audio\/*/)
        @readFile(files[i])
      else
        {}
    Promise.all(promises)

  readFile: (file) ->
    new Promise (resolve) ->
      window.jsmediatags.read(file, {
        onSuccess: (tag) ->
          resolve {
            artist: tag.tags.artist,
            album: tag.tags.album || 'UNKNOWNALBUM',
            title: tag.tags.title
          }
        onError: (error) ->
          console.log(error)
        }, false)

  dragLeave: (event) ->
    Helper.preventDef(event)

  render: () ->
    styles = {
      width: '100%'
      display: 'flex'
      justifyContent: 'center'
      paddingTop: 80
      height: 220
      border: '5px dashed #777'
      borderRadius: 10
    }
    <div id="audio-drop">
        <div style={styles} onDragOver={Helper.preventDef} onDrop={@drop} onDragEnter={@dragEnter} onDragLeave={@dragLeave}
          title="Drop your Audio Files here">
          <div>
            <h3>Drop your Audio Files here!</h3>
          </div>
        </div>
    </div>

module.exports = AudioFileUpload
