React = require 'react'
Artist = require './Artist'


ArtistList = React.createClass

  style: {
    display: 'flex'
    flexWrap: 'wrap'
    justifyContent: 'space-between'
    alignItems: 'flex-start'
    maxHeight: 600
    overflowY: 'auto'
  }

  render: () ->
    artists = @props.data.map (artist, idx) =>
      <Artist
        key={idx}
        artist={artist}
        nrCols={@props.nrCols}
        onArtistClick={@props.onArtistClick}
        onArtistSlideChange={@props.onArtistSlideChange}
        idx={idx} />

    <div style={@style}>
        {artists}
    </div>

module.exports = ArtistList
