React = require 'react'
ItunesUpload = require './ItunesUpload'
AudioFileUpload = require './AudioFileUpload'
Dialog = require 'material-ui/lib/dialog';
FlatButton = require 'material-ui/lib/flat-button'


UploadDialog = React.createClass

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
        disabled={true}
        onTouchTap={@props.handleClose} />,
    ]

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

module.exports = UploadDialog
