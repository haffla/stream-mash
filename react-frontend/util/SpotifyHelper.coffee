_ = require 'lodash'

class SpotifyHelper
  @getImage: (data, size) ->
    filtered = data.images.filter (img) ->
      if size is 'small' then img.width < 300 else img.width < 1000 && img.width > 300
    _.head(filtered).url

module.exports = SpotifyHelper
