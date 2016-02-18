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
           <div>
             <p>{nrAlbumsOnSpotify + " Albums on " + @props.name}</p>
             <p>{"In Your Collection: " + nrAlbumsInUserCollection}</p>
           </div>
           } />
     </Card>
    </div>

module.exports = ArtistDetailView
