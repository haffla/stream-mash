React = require 'react'
Helper = require '../util/Helper'
_ = require 'lodash'

Avatar = require 'material-ui/lib/avatar'
Card = require 'material-ui/lib/card/card'
CardHeader = require 'material-ui/lib/card/card-header'
CardMedia = require 'material-ui/lib/card/card-media'
CardText = require 'material-ui/lib/card/card-text'
CardTitle = require 'material-ui/lib/card/card-title'
Colors = require 'material-ui/lib/styles/colors'
FontIcon = require 'material-ui/lib/font-icon'
List = require 'material-ui/lib/lists/list'
ListItem = require 'material-ui/lib/lists/list-item'
Paper = require 'material-ui/lib/paper'


SpotifyBox = React.createClass

  getInitialState: () ->
    spotifyArtists: [], selectedArtist: {albums: []}

  componentDidMount: () ->
    $.ajax '/spotify/artists',
      type: 'GET'
      dataType: 'json'
      success: (data) =>
        @setState spotifyArtists: data.artists, selectedArtist: data.artists[0]
      error: (jqXHR, textStatus, e) ->
        console.log(e)

  handleArtistClick: (idx) ->
    if _.isEmpty(@state.spotifyArtists[idx].img)
      $.ajax '/spotify/artist-detail',
        type: 'GET'
        dataType: 'json'
        data: {'spId': @state.spotifyArtists[idx].id}
        success: (data) =>
          console.log(data)
          img = Helper.getBestSpotifyImage data.images, 'big'
          @state.spotifyArtists[idx].img = img
        error: (jqXHR, textStatus, e) ->
          console.log(e)
        complete: () =>
          @setState selectedArtist: @state.spotifyArtists[idx], selectedAlbum: {}
    else
      @setState selectedArtist: @state.spotifyArtists[idx], selectedAlbum: {}

  handleAlbumClick: (idx) ->
    unless _.has(@state.selectedArtist.albums[idx], 'img')
      $.ajax '/spotify/album-detail',
        type: 'GET'
        data: {'spId': @state.selectedArtist.albums[idx].id}
        dataType: 'json'
        success: (data) =>
          img = Helper.getBestSpotifyImage(data.images)
          @state.selectedArtist.albums[idx].img = img
          @state.selectedArtist.albums[idx].tracks = data.tracks
          @setState selectedAlbum: @state.selectedArtist.albums[idx]
        error: (jqXHR, textStatus, e) ->
          console.log(e)
    else
      @setState selectedAlbum: @state.selectedArtist.albums[idx]

  render: () ->
    artists = @state.spotifyArtists.map (artist, idx) =>
      initials = Helper.getInitials artist.name
      color = if artist.name == @state.selectedArtist.name then Colors.amber500 else 'white'
      <ListItem
        style={backgroundColor: color}
        key={idx}
        primaryText={artist.name}
        onTouchTap={@handleArtistClick.bind(null, idx)}
        leftAvatar={<Avatar>{initials}</Avatar>}/>

    selectedAlbums = @state.selectedArtist.albums.map (album, idx) =>
      icon = if album.inCollection then "check_box" else "check_box_outline_blank"
      isSelected = _.has(@state, 'selectedAlbum') && album.name == @state.selectedAlbum.name
      color = if isSelected then Colors.amber500 else 'white'
      <ListItem
        key={idx}
        style={backgroundColor: color}
        onTouchTap={@handleAlbumClick.bind(null, idx)}
        primaryText={album.name}
        rightAvatar={<FontIcon color="#455a64" className="material-icons" >{icon}</FontIcon>}
        />


    <div style={display: 'flex', justifyContent: 'space-between'}>
      <div style={width: '25%'}>
       <Paper style={width: '100%', marginBottom: 10, padding: 10} zDepth={0} children={
         <div>
          <h4>Spotify</h4>
          <table className="table">
            <thead>
              <tr>
                <td></td>
                <td>Available</td>
                <td>In your Collection</td>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Artists</td>
                <td>{@state.spotifyArtists.length}</td>
              </tr>
              <tr>
                <td>Albums</td>
                <td>{Helper.calculateNrOfAlbums(@state.spotifyArtists)}</td>
                <td>{Helper.calculateNrOfAlbumsInCollection(@state.spotifyArtists)}</td>
              </tr>
            </tbody>
          </table>
         </div>
         } />

       <List subheader="Spotify Artists">
        {artists}
       </List>
      </div>

     {
       if _.has(@state, 'selectedAlbum') && !_.isEmpty(@state.selectedAlbum)
         nrOfTracksInUsersCollection = _.size(@state.selectedAlbum.tracks.filter (track) -> track.inCollection)
         <div style={width: '40%'}>
          <Card>
            <CardTitle title={@state.selectedAlbum.name} />
            <CardMedia>
              <img src={@state.selectedAlbum.img}/>
            </CardMedia>
            <CardTitle title={_.size(@state.selectedAlbum.tracks) + " Tracks"} subtitle={"In your Collection: " + nrOfTracksInUsersCollection} />
            <div>
              <List>
                {
                  @state.selectedAlbum.tracks.map (tr,idx) ->
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
            <div style={display: 'flex', justifyContent: 'space-around', margin: 10}>
              <iframe src={"https://embed.spotify.com/?uri=spotify:album:#{@state.selectedAlbum.id}"} style={width: "300px", height: '80px'} frameBorder="0" allowTransparency="true"></iframe>
            </div>
          </Card>
         </div>
       else if _.has(@state, 'selectedArtist') && !_.isEmpty(@state.selectedArtist)
         nrAlbumsOnSpotify = _.size(@state.selectedArtist.albums)
         nrAlbumsInUserCollection = _.size(@state.selectedArtist.albums.filter (alb) -> alb.inCollection)
         <div style={width: '40%'}>
          <Card>
            <CardMedia>
              <img src={@state.selectedArtist.img}/>
            </CardMedia>
            <CardTitle
              title={@state.selectedArtist.name}
              subtitle={
                nrAlbumsOnSpotify + " Albums on Spotify of which you have " + nrAlbumsInUserCollection + "in your Collection"
                } />
          </Card>
         </div>
     }
     <div style={width: '33%'}>
        <List subheader={@state.selectedArtist.name + "'s " + "Albums on Spotify"}>
         {selectedAlbums}
        </List>
     </div>

    </div>


module.exports = SpotifyBox
