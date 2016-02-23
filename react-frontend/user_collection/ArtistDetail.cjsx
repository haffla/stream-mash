React = require 'react'
Helper = require '../util/Helper'

ArrowForward = require 'material-ui/lib/svg-icons/navigation/arrow-forward'
AutoComplete = require 'material-ui/lib/auto-complete'
Avatar = require 'material-ui/lib/avatar'
Badge = require 'material-ui/lib/badge'
List = require 'material-ui/lib/lists/list'
ListItem = require 'material-ui/lib/lists/list-item'
Slider = require 'material-ui/lib/slider'

ArtistDetail = React.createClass

  componentDidMount: () ->

  render: () ->
    avatar =
      if !_.isEmpty(@props.selectedArtist.img) then <Avatar src={@props.selectedArtist.img} />
      else
        initials = Helper.getInitials @props.selectedArtist.name
        <Avatar>{initials}</Avatar>

    <div className="row" style={display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '25px'}>

      <List subheader={
        <AutoComplete
          style={width: '80%', marginLeft: 10}
          floatingLabelText="Select Artist"
          filter={(searchText, key) -> searchText isnt '' and key.toLowerCase().includes(searchText.toLowerCase())}
          onNewRequest={@props.onNewRequest}
          dataSource={@props.autoCompleteSource}/>
        }
        style={width: '33%'}>
        <ListItem
          secondaryText={Helper.getTrackCountProse(@props.selectedArtist.trackCount)}
          primaryText={@props.selectedArtist.name}
          leftAvatar={avatar} />
        <ListItem secondaryText="Rating" rightAvatar={
          <div>
            <Slider
              style={width: 100}
              name="selectedArtistSlider"
              min={0}
              onChange={@props.onArtistSlideChange.bind(null, @props.selectedArtist.idx)}
              max={3}
              step={0.5}
              value={@props.selectedArtist.rating}/>
          </div>
        } />
      </List>

      <div style={margin: "20px 10px"}><ArrowForward /></div>

      <List insetSubheader={true} subheader={@props.selectedArtist.name + "'s Albums"} style={width: '33%'}>
        {
          @props.selectedArtist.albums.map (alb, idx) ->
            <ListItem key={idx} primaryText={alb.name} />
        }
      </List>

      <div style={margin: "20px 10px"}><ArrowForward /></div>

      <List insetSubheader={true} subheader={@props.selectedArtist.name + "'s Tracks"} style={width: '33%'}>
        {
          tracks = @props.selectedArtist.albums.map (alb) -> alb.tracks
          _.flatten(tracks).map (track, idx) ->
            <ListItem
              key={idx}
              primaryText={track.name} />
        }
      </List>
    </div>

module.exports = ArtistDetail
