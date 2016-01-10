React = require 'react'
ItunesUpload = require './ItunesUpload'
AudioFileUpload = require './AudioFileUpload'
Dialog = require 'material-ui/lib/dialog';
FlatButton = require 'material-ui/lib/flat-button'
Uploader = require '../../util/Uploader'

UploadDialog = React.createClass

  getInitialState: () -> uploadButtonDisabled: true

  showStandardFileDialog: () ->
    fileInput = $('#normalFileDialog')
    if @props.type is 'audio'
      fileInput.attr('multiple', 'multiple')
      fileInput.attr('accept', 'audio/*')
    else if @props.type is 'itunes'
      fileInput.removeAttr('multiple')
      fileInput.attr('accept', 'text/xml')
    fileInput.trigger('click')
    @setState({uploadButtonDisabled: false})

  uploadFiles: () ->
    fileInput = $('#normalFileDialog')
    files = fileInput[0].files
    formData = new FormData()
    route = if @props.type is '/itunes' then '/itunes' else '/fileupload'
    if @props.type is 'itunes'
      formData.append('file', files[0])
      uploader = new Uploader('/itunes')
    else
      uploader = new Uploader('/fileupload')
      for file in files
        formData.append 'files[]', file, file.name
    uploader.upload formData
    @setState({uploadButtonDisabled: true})

  render: () ->
    actions = [
      <FlatButton
        key={"cancelButton"}
        label="Cancel"
        secondary={true}
        onTouchTap={@props.handleClose} />,
      <FlatButton
        key={"normalFileDialog"}
        label="Show standard file dialog"
        primary={true}
        onTouchTap={@showStandardFileDialog} />,
      <FlatButton
        key={"uploadButton"}
        label="Upload"
        primary={true}
        disabled={@state.uploadButtonDisabled}
        onTouchTap={@uploadFiles} />
    ]

    <div>
      <input type="file" style={visibility: 'hidden'} id="normalFileDialog"/>
      <Dialog
        actions={actions}
        modal={true}
        open={@props.open}>
        {
          if @props.type is 'itunes'
            <ItunesUpload ws={@props.ws} />
          else if @props.type is 'audio'
            <AudioFileUpload />
        }
      </Dialog>
    </div>

module.exports = UploadDialog
