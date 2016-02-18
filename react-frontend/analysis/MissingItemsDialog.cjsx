React = require 'react'

Dialog = require 'material-ui/lib/dialog'

MissingItemsDialog = React.createClass

  render: () ->
    <Dialog
      title={@props.title}
      modal={false}
      open={@props.open}
      onRequestClose={@props.onRequestClose}>
      <div style={overflowY: 'auto', maxHeight: 500}>
        <table className="table">
          <tbody>
            {@props.items}
          </tbody>
        </table>
      </div>
    </Dialog>


module.exports = MissingItemsDialog
