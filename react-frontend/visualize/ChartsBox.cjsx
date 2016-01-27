React = require 'react'
BubbleChart = require './d3/BubbleChart'
BarChart = require './d3/BarChart'
BarChartSimple = require './d3/BarChartSimple'

ChartsBox = React.createClass

  barChartSimple: null

  handleBubbleClick: (d) ->
    @barChartSimple.makeBarChart(d)

  componentDidMount: () ->
    $.ajax '/vidata',
      type: 'GET',
      dataType: 'json',
      success: (data) =>
        @setState data: data
        bubble = new BubbleChart(@state.data, @handleBubbleClick)
        bubble.start()
        bar = new BarChart(@state.data)
        bar.start()
        @barChartSimple = new BarChartSimple(@state.data)
      error: (e) ->
        console.log(e)

  render: () ->
    <div style={display: 'flex', justifyContent: 'space-between'}>
      <div id="charts-box-left" style={width: '45%', height: 500}>
        <svg className="bubbles"></svg>
        <svg className="chart"></svg>
      </div>
      <div id="charts-box-right" style={width: '45%', height: 500}>
        <svg className="chartTwo"></svg>
      </div>
    </div>


module.exports = ChartsBox
