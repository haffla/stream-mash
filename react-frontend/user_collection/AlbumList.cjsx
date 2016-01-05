React = require 'react'
Album = require './Album'

AlbumList = React.createClass
  render: () ->
    albums = @props.albums.map (album, idx) ->
      userHas = if album.userHas then "hasAlbum" else ""
      <Album key={idx} name={album.name} userHas={userHas}/>

    <div className="albumList">
        {albums}
    </div>

module.exports = AlbumList
