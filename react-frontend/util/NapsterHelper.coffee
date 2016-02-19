_ = require 'lodash'

class NapsterHelper
  @getImage: (data) ->
    if data.images
      filtered = data.images.filter (image) -> image.width < 570
      _.last(_.sortBy(filtered, 'width')).url


module.exports = NapsterHelper
