React = require 'react'
AlbumList = require './AlbumList'

Artist = React.createClass
  render: () ->
    <div onClick={@props.onArtistClick.bind(null, @props.idx)} className="artist panel panel-default" style={width: (100 / @props.nrCols - 0.5) + '%'}>
        <div className="panel-heading">
          <div><i className="fa fa-music"></i> {@props.artist.name}</div>
          <button className="btn btn-default album-list-opener">
            <i className="fa fa-plus"></i>
          </button>
        </div>

        <div className="panel-body">
          <AlbumList albums={@props.artist.albums}/>
        </div>
    </div>

module.exports = Artist
