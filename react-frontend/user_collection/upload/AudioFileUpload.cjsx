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

  render: () ->
    styles = {
      width: 200
      height: 200
    }
    <div id="audio-drop">
        <div style={styles} onDragOver={Helper.preventDef} onDrop={@drop} onDragEnter={@dragEnter} onDragLeave={@dragLeave}
          title="Drop your iTunes Library file here"></div>
    </div>

module.exports = AudioFileUpload
