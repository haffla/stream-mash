React = require 'react'

Album = React.createClass
  render: () ->
    classes = "album " + @props.userHas
    <div className={classes}>
        <a className="prevent-default" target="_blank" href="#">{@props.name}</a>
    </div>

module.exports = Album
