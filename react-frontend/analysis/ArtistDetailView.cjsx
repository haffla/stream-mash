React = require 'react'

Card = require 'material-ui/lib/card/card'
CardMedia = require 'material-ui/lib/card/card-media'
CardTitle = require 'material-ui/lib/card/card-title'

ArtistDetailView = React.createClass

  render: () ->
    nrAlbumsOnSpotify = _.size(@props.selectedArtist.albums)
    nrAlbumsInUserCollection = _.size(@props.selectedArtist.albums.filter (alb) -> alb.inCollection)
    <div style={width: '40%'}>
     <Card>
       <CardMedia>
         <img src={@props.selectedArtist.img}/>
       </CardMedia>
       <CardTitle
         title={@props.selectedArtist.name}
         subtitle={
           nrAlbumsOnSpotify + " Albums on " + @props.name + " of which you have " + nrAlbumsInUserCollection + " in your Collection"
           } />
     </Card>
    </div>

module.exports = ArtistDetailView
