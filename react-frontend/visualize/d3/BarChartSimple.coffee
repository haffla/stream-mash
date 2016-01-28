d3 = require 'd3'
Colors = require 'material-ui/lib/styles/colors'
Helper = require '../../util/Helper'

class BarChartSimple
  constructor: () ->
    @width = $('#simple-chartbox').width()
    @height = $('#simple-chartbox').height()
    @barHeight = 20

  serviceColor: (d) ->
    if typeof d is 'undefined' then Colors.red400
    else 'white'

  services: ["Spotify", "Deezer", "Napster"]

  makeBarChart: (data) ->

    failSafe = (d, adjustment) ->
      if typeof d is 'undefined'
        x(0)
      else
        if typeof adjustment is 'undefined'
          x(d)
        else
          x(d) + adjustment

    d3.select('.simple-chart').selectAll('g').remove()

    x = d3.scale.linear().domain([0, d3.max(data)]).range([0, @width])
    chart = d3.select('.simple-chart')
      .attr('width', @width)
      .attr('height', @barHeight * data.length)

    bar = chart.selectAll('g').data(data)
      .enter().append('g')
        .attr('transform', (d,i) => 'translate(0,' + i * @barHeight + ')')

    bar.append('rect')
      .attr('width', failSafe)
      .attr('height', @barHeight - 1)
      .attr('fill', 'steelblue')

    bar.append('text')
      .attr('x', (d) -> failSafe(d, -5))
      .attr('y', @barHeight / 2)
      .attr('dy', '.35em')
      .attr('fill', 'white')
      .attr('text-anchor', 'end')
      .text((d) -> d)

    bar.append('text')
      .attr('x', 5)
      .attr('y', @barHeight / 2)
      .attr('dy', '.35em')
      .attr('fill', @serviceColor)
      .attr('text-anchor', 'start')
      .text((d,i) => @services[i])


module.exports = BarChartSimple
