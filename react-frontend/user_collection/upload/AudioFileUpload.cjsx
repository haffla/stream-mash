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
    formData = new FormData()
    for file in files
      formData.append 'files[]', file, file.name
    @uploader.upload formData

  dragEnter: (event) ->
    Helper.preventDef(event)

  dragLeave: (event) ->
    Helper.preventDef(event)

  inputStyles: {
    position: 'absolute'
    top: 0
    right: 0
    minWidth: '100%'
    minHeight: '100%'
    fontSize: 100
    textAlign: 'right'
    filter: 'alpha(opacity=0)'
    opacity: 0
    outline: 'none'
    background: 'white'
    cursor: 'inherit'
    display: 'block'
  }

  render: () ->
    <div id="audio-drop">
        <div onDragOver={Helper.preventDef} onDrop={@drop} onDragEnter={@dragEnter} onDragLeave={@dragLeave}
        title="Drop your iTunes Library file here" id="dropzone"></div>
        <span className="btn btn-default" style={position: 'relative', overflow: 'hidden'}>
            Browse <input style={@inputStyles} type="file" id="audio-files" accept="audio/*" multiple />
        </span>
        <button id="audio-submit" className="btn btn-default">Upload</button>
        <span id="dropzone-indicator">You can drag and drop files here!</span>
    </div>

module.exports = AudioFileUpload
