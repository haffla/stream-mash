React = require 'react'
BubbleChart = require './d3/BubbleChart'

ChartsBox = React.createClass

  componentDidMount: () ->
    $.ajax '/vidata',
      type: 'GET',
      dataType: 'json',
      success: (data) =>
        console.log(data)
        @setState data: data
        @b = new BubbleChart(@state.data.user)
        @b.start()
      error: (e) ->
        console.log(e)

  render: () ->
    <div id="charts-box">
    </div>

module.exports = ChartsBox
