_ = require 'lodash'
d3 = require 'd3'

Colors = require 'material-ui/lib/styles/colors'

class ServiceChart
  constructor: (data) ->
    @artists = data.artists
    @totals = data.totals
    @width = $('#charts-box-right').width()
    @height = $('#charts-box-right').height()

  setTotals: (totals) ->
    @totals = totals

  redraw: (totals) ->
    @setTotals totals
    d3.select('#service-chart').selectAll('g').remove()
    @start()

  start: () ->
    margin = {top: 20, right: 30, bottom: 50, left: 40}
    width = @width - margin.left - margin.right
    height = @height - margin.top - margin.bottom

    names = @artists.map (a) -> a.name
    artistsWithTotals = @artists.map (a) =>
      id = a.id
      total = @totals[id] || 0
      a.total = total
      a

    x = d3.scale.ordinal()
      .domain(names)
      .rangeRoundBands([0, width], .05)

    xAxis = d3.svg.axis()
      .scale(x)
      .orient('bottom')

    y = d3.scale.linear()
      .domain([0, d3.max(artistsWithTotals, (d) -> d.total)])
      .range([height, 0])

    chart = d3.select('#service-chart')
        .attr('width', width + margin.left + margin.right)
        .attr('height', height + margin.top + margin.bottom)
      .append('g')
        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')

    bar = chart.selectAll('g')
        .data(artistsWithTotals)
      .enter().append('g')
        .attr('transform', (d) -> 'translate(' + x(d.name) + ',0)')

    bar.append('rect')
      .attr('y', (d) -> y(d.total))
      .attr('height', (d) => height - y(d.total))
      .attr('width', x.rangeBand())
      .attr('fill', Colors.amber700)
      .attr('class', (d) -> 'artist artist' + d.id)

    bar.append('text')
      .attr('x', x.rangeBand() / 2)
      .attr('y', (d) -> y(d.total) + 5)
      .attr('dy', '.75em')
      .attr('fill', 'white')
      .attr('text-anchor', 'middle')
      .text((d) -> d.total)

    chart.append('g')
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis)
      .selectAll('text')
        .attr('text-anchor', 'end')
        .attr('transform', 'rotate(-45)')


module.exports = ServiceChart
