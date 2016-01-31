React = require 'react'

Colors = require 'material-ui/lib/styles/colors'
Dialog = require 'material-ui/lib/dialog'
FlatButton = require 'material-ui/lib/flat-button'

AnalysisDoneDialog = React.createClass

  render: () ->
    color = if @props.success then Colors.green400 else Colors.red400
    message =  if @props.success then <p style={color: color}>The services were successfully analysed.</p>
    else <p style={color: color}>One or more operations were not successful, please try again.</p>

    actions = [
      <FlatButton
        key={"canceldialogbutton"}
        label={if @props.success then "stay here" else "close"}
        secondary={true}
        onTouchTap={@props.handleClose} />
    ]

    if @props.success
      actions.push(
        <FlatButton
          key={"gotooverviewpagebutton"}
          label="go to overview page"
          primary={true}
          onTouchTap={() -> window.location.href = '/overview'} />
      )

      actions.push(
        <FlatButton
          key={"gotovisubutton"}
          label="see results visualised"
          primary={true}
          onTouchTap={() -> window.location.href = '/visualize'} />
      )

    <Dialog
      actions={actions}
      modal={false}
      open={@props.open}>
      <div style={textAlign: 'center'}>
        <h3 style={color: color}>It is done!</h3>
        {message}
      </div>
    </Dialog>

module.exports = AnalysisDoneDialog
