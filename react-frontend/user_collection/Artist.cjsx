React = require 'react'
AlbumList = require './AlbumList'

Artist = React.createClass
  render: () ->
    <AlbumList albums={@props.albums}/>

module.exports = Artist
