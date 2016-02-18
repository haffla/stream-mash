React = require 'react'
Helper = require '../util/Helper'

StatsTable = require './StatsTable'

Avatar = require 'material-ui/lib/avatar'
Colors = require 'material-ui/lib/styles/colors'
List = require 'material-ui/lib/lists/list'
ListItem = require 'material-ui/lib/lists/list-item'
Paper = require 'material-ui/lib/paper'

LeftView = React.createClass
  render: () ->
    artists = @props.artists.map (artist, idx) =>
      initials = Helper.getInitials artist.name
      color = if artist.name == @props.selectedArtist then Colors.amber500 else 'white'
      <ListItem
        style={backgroundColor: color}
        key={idx}
        primaryText={artist.name}
        onTouchTap={@props.handleArtistClick}
        leftAvatar={<Avatar>{initials}</Avatar>} />
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
         {artists}
        </List>
      </div>


module.exports = LeftView
