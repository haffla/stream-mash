React = require 'react'

ArrowForward = require 'material-ui/lib/svg-icons/navigation/arrow-forward'
AutoComplete = require 'material-ui/lib/auto-complete'
Avatar = require 'material-ui/lib/avatar';
List = require 'material-ui/lib/lists/list'
ListItem = require 'material-ui/lib/lists/list-item'

ArtistDetail = React.createClass

  componentDidMount: () ->

  render: () ->

    avatar =
      if _.has(@props.selectedArtist, 'img') then <Avatar src={@props.selectedArtist.img} />
      else
        arr = (@props.selectedArtist.name.split(' ').map (s) -> s[0].toUpperCase())
        res = if arr.length > 2 then _.first(arr).concat(_.last(arr)) else arr.join('')
        <Avatar>{res}</Avatar>

    <div className="row" style={display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '25px'}>

      <List subheader={
        <AutoComplete
          style={width: '80%', marginLeft: 10}
          floatingLabelText="Select Artist"
          filter={AutoComplete.caseInsensitiveFilter}
          onNewRequest={@props.onNewRequest}
          dataSource={@props.autoCompleteSource}/>
        }
        style={width: '33%'}>
        <ListItem primaryText={@props.selectedArtist.name} leftAvatar={avatar} />
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
            <ListItem key={idx} primaryText={track} />
        }
      </List>

    </div>

module.exports = ArtistDetail
