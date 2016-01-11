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

  render: () ->
    artists = @props.data.map (artist, idx) =>
      <Artist
        key={idx}
        artist={artist}
        nrCols={@props.nrCols}
        onArtistClick={@props.onArtistClick}
        onArtistSlideChange={@props.onArtistSlideChange}
        idx={idx} />

    <div className="artistList" style={@style}>
        {artists}
    </div>

module.exports = ArtistList
