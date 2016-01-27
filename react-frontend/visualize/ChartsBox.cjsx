_ = require 'lodash'

React = require 'react'
BubbleChart = require './d3/BubbleChart'
Colors = require 'material-ui/lib/styles/colors'
BarChart = require './d3/BarChart'
ServiceChart = require './d3/ServiceChart'
BarChartSimple = require './d3/BarChartSimple'

FlatButton = require 'material-ui/lib/flat-button'

ChartsBox = React.createClass

  barChartSimple: null
  serviceChart: null

  handleBubbleClick: (d) ->
    id = d.id
    $('.artist').attr('fill', 'steelblue')
    $('.artist' + id).attr('fill', Colors.red400)
    @barChartSimple.makeBarChart [@state.data.spotify[id], @state.data.deezer[id], @state.data.napster[id]]

  componentDidMount: () ->
    $.ajax '/vidata',
      type: 'GET',
      dataType: 'json',
      success: (data) =>
        console.log(data)
        @setState data: data
        bubble = new BubbleChart(@state.data, @handleBubbleClick)
        bar = new BarChart(@state.data.user)
        @barChartSimple = new BarChartSimple()
        @serviceChart = new ServiceChart({
          artists: @state.data.user
          totals: @state.data.total
          })
        bar.start()
        bubble.start()
        @serviceChart.start()
      error: (e) ->
        console.log(e)

  boxStyle: {
    width: '49%'
    height: 500
    backgroundColor: Colors.grey300
    borderRadius: 5
  }

  handleServiceButtonClick: (target) ->
    switch target
      when 'spotify' then @serviceChart.redraw @state.data.spotify
      when 'deezer' then @serviceChart.redraw @state.data.deezer
      when 'napster' then @serviceChart.redraw @state.data.napster
      else @serviceChart.redraw @state.data.total

  render: () ->
    <div>
      <div style={display: 'flex', justifyContent: 'space-between'}>
        <div id='charts-box-left' style={@boxStyle}>
          <svg className='bubbles'></svg>
        </div>
        <div id='charts-box-right' style={@boxStyle}>
          <svg className='chart-two'></svg>
        </div>
      </div>

      <div style={display: 'flex', justifyContent: 'space-between', marginTop: 20}>
        <div style={@boxStyle}>
          <div>
            <FlatButton backgroundColor={Colors.grey300} onClick={@handleServiceButtonClick.bind(null, 'all')} label='All' />
            <FlatButton backgroundColor={Colors.grey300} onClick={@handleServiceButtonClick.bind(null, 'spotify')} label='Spotify' secondary={true} />
            <FlatButton backgroundColor={Colors.grey300} onClick={@handleServiceButtonClick.bind(null, 'deezer')} label='Deezer' secondary={true} />
            <FlatButton backgroundColor={Colors.grey300} onClick={@handleServiceButtonClick.bind(null, 'napster')} label='Napster' secondary={true} />
          </div>
          <svg id='service-chart'></svg>
        </div>
        <div style={_.merge(@boxStyle, {padding: 10})}>
          <svg className='chart'></svg>
        </div>
      </div>
    </div>


module.exports = ChartsBox
