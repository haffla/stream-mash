React = require 'react'
Artist = require './Artist'


ArtistList = React.createClass

  style: {
    display: 'flex'
    flexWrap: 'wrap'
    justifyContent: 'space-between'
    alignItems: 'flex-start'
    height: 400
    overflowY: 'auto'
  }

  handle: (evt) ->
    console.log(evt)

  render: () ->
    artists = @props.data.map (artist, idx) =>
      <Artist
        key={idx}
        onHover={@handle}
        artist={artist}
        nrCols={@props.nrCols}
        onArtistClick={@props.onArtistClick}
        onArtistSlideChange={@props.onArtistSlideChange}
        idx={idx} />

    <div style={@style}>
        {artists}
    </div>

module.exports = ArtistList
