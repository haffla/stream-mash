React = require 'react'

TagReader = require '../../util/TagReader'
ItunesUpload = require './ItunesUpload'
AudioFileUpload = require './AudioFileUpload'
Dialog = require 'material-ui/lib/dialog'
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
    @setState uploadButtonDisabled: false

  uploadFiles: () ->
    fileInput = $('#normalFileDialog')
    files = fileInput[0].files
    unless _.isEmpty(files)
      route = if @props.type is 'itunes' then '/itunes' else '/fileupload'
      uploader = new Uploader(route)
      if @props.type is 'itunes'
        formData = new FormData()
        formData.append('file', files[0])
        uploader.upload formData
      else
        TagReader.readFiles(files).then (data) =>
          filtered = data.filter TagReader.filterData
          if filtered.length > 0
            successCallback = () =>
              @props.ws.send('audio')
              @props.handleClose()
            uploader.upload(
                JSON.stringify(filtered)
                'application/json'
                successCallback
              )
      @setState uploadButtonDisabled: true
    else
      @setState uploadButtonDisabled: true
      alert("Please select at least one file to upload!")


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
            <ItunesUpload handleClose={@props.handleClose} ws={@props.ws} />
          else if @props.type is 'audio'
            <AudioFileUpload handleClose={@props.handleClose} ws={@props.ws} />
        }
      </Dialog>
    </div>

module.exports = UploadDialog
