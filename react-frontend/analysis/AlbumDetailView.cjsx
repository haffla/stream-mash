React = require 'react'

Card = require 'material-ui/lib/card/card'
CardMedia = require 'material-ui/lib/card/card-media'
CardTitle = require 'material-ui/lib/card/card-title'
FontIcon = require 'material-ui/lib/font-icon'
List = require 'material-ui/lib/lists/list'
ListItem = require 'material-ui/lib/lists/list-item'

AlbumDetailView = React.createClass

  render: () ->
    nrOfTracksInUsersCollection = _.size(@props.selectedAlbum.tracks.filter (track) -> track.inCollection)
    <div style={width: '40%'}>
     <Card>
       <CardTitle title={@props.selectedAlbum.name} />
       <CardMedia>
         <img src={@props.selectedAlbum.img}/>
       </CardMedia>
       <CardTitle title={_.size(@props.selectedAlbum.tracks) + " Tracks"} subtitle={"In your Collection: " + nrOfTracksInUsersCollection} />
       <div>
         <List>
           {
             @props.selectedAlbum.tracks.map (tr,idx) ->
               icon = if tr.inCollection then "check_box" else "check_box_outline_blank"
               <ListItem className="trackListItem"
                 key={"track" + idx}
                 style={height: 40, cursor: 'auto'}
                 primaryText={tr.name}
                 rightAvatar={<FontIcon color="#455a64" className="material-icons" >{icon}</FontIcon>}
               />
           }
         </List>
       </div>
       {
         if @props.showPlayer
           <div style={display: 'flex', justifyContent: 'space-around', margin: 10}>
             <iframe src={"https://embed.spotify.com/?uri=spotify:album:#{@props.selectedAlbum.id}"} style={width: "300px", height: '80px'} frameBorder="0" allowTransparency="true"></iframe>
           </div>
       }
     </Card>
    </div>

module.exports = AlbumDetailView
