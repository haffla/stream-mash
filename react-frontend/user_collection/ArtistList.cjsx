React = require 'react'
Artist = require './Artist'


ArtistList = React.createClass

  render: () ->
    artists = @props.data.map (artist, idx) =>
      <div key={idx} className="artist panel panel-default" style={width: (100 / @props.nrCols - 0.5) + '%'}>

          <div className="panel-heading">
            <div><i className="fa fa-music"></i> {artist.name}</div>
            <button className="btn btn-default album-list-opener" onClick={@props.onButtonClick.bind(null, artist.name, idx)}>
              <i className="fa fa-plus"></i>
            </button>
          </div>

          <div className="panel-body">
            <Artist key={artist.id} albums={artist.albums} />
          </div>

      </div>
    <div className="artistList" style={display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'flex-start'}>
        {artists}
    </div>

module.exports = ArtistList
