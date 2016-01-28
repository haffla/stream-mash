_ = require 'lodash'
jsmediatags = require 'jsmediatags'

React = require 'react'
Helper = require '../../util/Helper'
Uploader = require '../../util/Uploader'

AudioFileUpload = React.createClass

  componentWillMount: () ->
    jQuery.event.props.push 'dataTransfer'

  uploader: new Uploader('/fileupload')

  drop: (event) ->
    Helper.preventDef(event)
    files = event.target.files || event.dataTransfer.files
    @readFiles(files).then (data) =>
      @uploader.upload(JSON.stringify(data), 'application/json')

  dragEnter: (event) ->
    Helper.preventDef(event)

  readFiles: (files) ->
    promises = (idx for idx in [0...files.length]).map (i) =>
      @readFile(files[i])
    Promise.all(promises)

  readFile: (file) ->
    new Promise (resolve) ->
      jsmediatags.read(file, {
        onSuccess: (tag) ->
          resolve {
            artist: tag.tags.artist,
            album: tag.tags.album,
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
      height: 220
      border: '1px dashed #777'
    }
    <div id="audio-drop">
        <div style={styles} onDragOver={Helper.preventDef} onDrop={@drop} onDragEnter={@dragEnter} onDragLeave={@dragLeave}
          title="Drop your iTunes Library file here"></div>
    </div>

module.exports = AudioFileUpload
