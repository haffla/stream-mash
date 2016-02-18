React = require 'react'
AlbumDetailView = require './AlbumDetailView'
ArtistDetailView = require './ArtistDetailView'

MidView = React.createClass

  render: () ->
    if @props.selectedAlbum and !_.isEmpty(@props.selectedAlbum)
      <AlbumDetailView
       showPlayer={@props.showPlayer}
       selectedAlbum={@props.selectedAlbum}
       />
    else if @props.selectedArtist and !_.isEmpty(@props.selectedArtist)
      <ArtistDetailView
       name={@props.name}
       selectedArtist={@props.selectedArtist} />


module.exports = MidView
