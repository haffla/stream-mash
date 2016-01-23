React = require 'react'
Toggle = require 'material-ui/lib/toggle'
Checkbox = require 'material-ui/lib/checkbox'
Slider = require 'material-ui/lib/slider'

Artist = React.createClass

  render: () ->
    <div onTouchTap={@props.onArtistClick.bind(null, @props.idx)} className="artist panel panel-default" style={width: (100 / @props.nrCols - 0.5) + '%', height: 50}>
      <div className="panel-body" style={display: 'flex', justifyContent: 'space-between', cursor: 'pointer'}>
        <div style={position: 'absolute', left: 0, margin: 5}><i className="fa fa-music"></i> {@props.artist.name}</div>
        <div style={position: 'absolute', right: 0, margin: 6}>
          <Slider
            style={width: 100}
            name="artistSlider"
            min={0}
            onChange={@props.onArtistSlideChange.bind(null, @props.idx)}
            max={3}
            step={1}
            value={@props.artist.rating}/>
        </div>
      </div>
    </div>


module.exports = Artist
