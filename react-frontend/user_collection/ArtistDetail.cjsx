React = require 'react'

ArrowForward = require 'material-ui/lib/svg-icons/navigation/arrow-forward'
AutoComplete = require 'material-ui/lib/auto-complete'
List = require 'material-ui/lib/lists/list'
ListItem = require 'material-ui/lib/lists/list-item'

ArtistDetail = React.createClass

  render: () ->
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
        <ListItem primaryText={@props.selectedArtist.name} />
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
