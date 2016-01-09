React = require 'react'

IconButton = require 'material-ui/lib/icon-button'
IconMenu = require 'material-ui/lib/menus/icon-menu'
MenuItem = require 'material-ui/lib/menus/menu-item'
NavigationExpandMoreIcon = require 'material-ui/lib/svg-icons/navigation/expand-more'
RaisedButton = require 'material-ui/lib/raised-button'
Toolbar = require 'material-ui/lib/toolbar/toolbar'
ToolbarGroup = require 'material-ui/lib/toolbar/toolbar-group'
ToolbarSeparator = require 'material-ui/lib/toolbar/toolbar-separator'
ToolbarTitle = require 'material-ui/lib/toolbar/toolbar-title'
Slider = require 'material-ui/lib/slider'

services = ["spotify", "deezer", "soundcloud", "lastfm"]

ControlBar = React.createClass

  render: () ->
    <Toolbar>
      <ToolbarGroup firstChild={true} float="left">
        <Slider
          description="Number of columns"
          name="colSlider"
          disabled={@props.disabled}
          defaultValue={3}
          step={1}
          min={1}
          max={5}
          onChange={@props.handleSlider} />
      </ToolbarGroup>
      <ToolbarGroup float="right">
        <ToolbarTitle text="Import" />
        <RaisedButton style={marginLeft: 9} onTouchTap={@props.openDialog.bind(null, "itunes")} label="itunes" primary={true} />
        <RaisedButton style={marginLeft: 0} onTouchTap={@props.openDialog.bind(null, "audio")} label="audio files" primary={true} />
        <IconMenu
          style={marginTop: 5, marginLeft: -15}
          desktop={true}
          targetOrigin={vertical: "center", horizontal: "right"}
          onItemTouchTap={@props.handleStreamingServiceSelection}
          iconButtonElement={
            <IconButton touch={true}>
              <NavigationExpandMoreIcon />
            </IconButton>
          }>
          {
            services.map (service) ->
              <MenuItem key={service} primaryText={service.charAt(0).toUpperCase() + service.slice(1)} data={service} />
          }
        </IconMenu>
      </ToolbarGroup>
    </Toolbar>

module.exports = ControlBar
