_ = require 'lodash'

class NapsterHelper
  @getImage: (data) ->
    filtered = data.images.filter (image) -> image.width < 570
    _.last(_.sortBy(filtered, 'width')).url


module.exports = NapsterHelper
