_ = require 'lodash'

class DeezerHelper
  @getImage: (data) -> data.images.picture_big

module.exports = DeezerHelper
