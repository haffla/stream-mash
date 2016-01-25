React = require 'react'
BubbleChart = require '../d3/BubbleChart'

Charts = React.createClass
  
  componentDidMount: () ->
    @b = new BubbleChart()

  bang: () ->
    @b.doSomething()

  render: () ->
    <p onClick={@bang}>HI</p>

module.exports = Charts
