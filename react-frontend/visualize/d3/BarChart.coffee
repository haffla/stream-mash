_ = require 'lodash'
d3 = require 'd3'

Colors = require 'material-ui/lib/styles/colors'

class BarChart
  constructor: (data, names, container, svgElement, totalKey) ->
    @data = data
    @names = names
    @svgElement = svgElement
    @width = $(container).width()
    @height = $(container).height()
    @totalKey = totalKey

  start: (selectedItemId) ->
    margin = {top: 20, right: 30, bottom: 50, left: 40}
    width = @width - margin.left - margin.right
    height = @height - margin.top - margin.bottom

    x = d3.scale.ordinal()
      .domain(@names)
      .rangeRoundBands([0, width], .05)

    xAxis = d3.svg.axis()
      .scale(x)
      .orient('bottom')

    y = d3.scale.linear()
      .domain([0, d3.max(@data, (d) => d[@totalKey])])
      .range([height, 0])

    chart = d3.select(@svgElement)
        .attr('width', width + margin.left + margin.right)
        .attr('height', height + margin.top + margin.bottom)
      .append('g')
        .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')

    bar = chart.selectAll('g')
        .data(@data)
      .enter().append('g')
        .attr('transform', (d) -> 'translate(' + x(d.name) + ',0)')

    bar.append('rect')
      .attr('y', (d) => y(d[@totalKey]))
      .attr('height', (d) => height - y(d[@totalKey]))
      .attr('width', x.rangeBand())
      .attr('fill', (d) -> if d.id is selectedItemId then Colors.purple700 else Colors.amber700)
      .attr('class', (d) -> 'artist artist' + d.id)

    bar.append('text')
      .attr('x', x.rangeBand() / 2)
      .attr('y', (d) =>
        adjustment = if d[@totalKey] == 0 then -30 else 5
        y(d[@totalKey]) + adjustment)
      .attr('dy', '.75em')
      .attr('fill', (d) => unless d[@totalKey] == 0 then 'white' else Colors.red900)
      .attr('text-anchor', 'middle')
      .text((d) => d[@totalKey])

    chart.append('g')
      .attr("class", "x axis")
      .attr("transform", "translate(0," + height + ")")
      .call(xAxis)
      .selectAll('text')
        .attr('text-anchor', 'end')
        .attr('transform', 'rotate(-45)')


module.exports = BarChart
