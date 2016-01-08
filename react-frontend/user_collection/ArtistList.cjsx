React = require 'react'
Artist = require './Artist'


ArtistList = React.createClass
  render: () ->
    artists = @props.data.map (artist, idx) =>
      <div key={idx} className="artist panel panel-default">

          <div className="panel-heading">
            <div><i className="fa fa-music"></i> {artist.name}</div>
            <button className="btn btn-default album-list-opener" onTouchTap={@props.onButtonClick.bind(null, artist.name, idx)}>
              <i className="fa fa-plus"></i>
            </button>
          </div>

          <div className="panel-body">
            <Artist key={artist.id} albums={artist.albums} />
          </div>

      </div>
    <div className="artistList">
        {artists}
    </div>

module.exports = ArtistList
