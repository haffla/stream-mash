d3 = require 'd3'
Colors = require 'material-ui/lib/styles/colors'
Helper = require '../../util/Helper'

class BarChart
  constructor: (data) ->
    @data = data
    @width = $('#trackcount-chartbox').width()
    @height = $('#trackcount-chartbox').height()

  start: () ->
    margin = {top: 20, right: 30, bottom: 50, left: 40}
    width = @width - margin.left - margin.right
    height = @height - margin.top - margin.bottom

    x = d3.scale.ordinal()
      .domain(@data.map (a) -> a.name)
      .rangeRoundBands([0, width], .05)

    xAxis = d3.svg.axis()
      .scale(x)
      .orient('bottom')

    y = d3.scale.linear()
      .domain([0, d3.max(@data, (d) -> d.trackCount)])
      .range([height, 0])

    chart = d3.select('.trackcount-chart')
        .attr('width', width + margin.left + margin.right)
        .attr('height', height + margin.top + margin.bottom)
      .append('g')
        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')

    bar = chart.selectAll('g')
        .data(@data)
      .enter().append('g')
        .attr('transform', (d) -> 'translate(' + x(d.name) + ',0)')

    bar.append('rect')
      .attr('y', (d) -> y(d.trackCount))
      .attr('height', (d) -> height - y(d.trackCount))
      .attr('width', x.rangeBand())
      .attr('fill', Colors.amber700)
      .attr('class', (d) -> 'artist artist' + d.id)

    bar.append('text')
      .attr('x', x.rangeBand() / 2)
      .attr('y', (d) -> y(d.trackCount) + 5)
      .attr('dy', '.75em')
      .attr('fill', 'white')
      .attr('text-anchor', 'middle')
      .text((d) -> d.trackCount)

    chart.append('g')
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis)
      .selectAll('text')
        .attr('text-anchor', 'end')
        .attr('transform', 'rotate(-45)')


module.exports = BarChart
