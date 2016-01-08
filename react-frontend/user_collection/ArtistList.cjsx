React = require 'react'
Artist = require './Artist'


ArtistList = React.createClass

  render: () ->
    artists = @props.data.map (artist, idx) =>
      <Artist key={idx} artist={artist} nrCols={@props.nrCols} onButtonClick={@props.onButtonClick} idx={idx} />

    <div className="artistList" style={display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'flex-start'}>
        {artists}
    </div>

module.exports = ArtistList
