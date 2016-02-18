React = require 'react'

StatsTable = require './StatsTable'

List = require 'material-ui/lib/lists/list'
Paper = require 'material-ui/lib/paper'

LeftView = React.createClass
  render: () ->
    children =
      <div>
       <h4>{@props.name}</h4>
       <StatsTable
         nrArtists={@props.nrArtists}
         openMissingItemsDialog={@props.openMissingItemsDialog}
         nrAbsentArtist={@props.nrAbsentArtist}
         nrMissingAlbums={@props.nrMissingAlbums}
         nrAlbumsTotal={@props.nrAlbumsTotal}
         nrAlbumsInUserCollection={@props.nrAlbumsInUserCollection} />
      </div>
    <div style={width: '25%'}>
      <Paper style={width: '100%', marginBottom: 10, padding: 10} zDepth={0} children={children} />
        <List subheader={@props.name + " Artists"}>
         {@props.artists}
        </List>
      </div>


module.exports = LeftView
