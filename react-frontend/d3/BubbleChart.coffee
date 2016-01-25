d3 = require 'd3'

class BubbleChart
  constructor: (data) ->
    console.log("Hi I am bubble")

  doSomething: () ->
    d3.select('#content').selectAll("p")
        .data([1,2,3,4,5,6,52])
      .enter().append("p")
        .text((d) -> "Im a number " + d)

module.exports = BubbleChart
