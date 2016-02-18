_ = require 'lodash'
d3 = require 'd3'

BarChart = require './BarChart'

class RedrawableBarChart extends BarChart

  redraw: (totals, selectedItemId, data) ->
    data.map (a) =>
      id = a.id
      total = totals[id] || 0
      a.total = total
      a
    d3.select(@svgElement).selectAll('g').remove()
    @start(selectedItemId)

module.exports = RedrawableBarChart
