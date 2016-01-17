_ = require 'lodash'

class Helper
  @calculateNrOfAlbums: (data) ->
    _.values(data).reduce (x,y) ->
      x + y.albums.length
    , 0

  @calculateNrOfAlbumsInCollection: (data) ->
    _.values(data).reduce (x,y) ->
      x + y.albums.reduce (a,b) ->
        if b.inCollection then a + b else a
      , 0
    , 0

  @isMac: () ->
    if navigator.platform.match(/(Mac|iPhone|iPod|iPad)/i) then true else false

  @isWindows: () ->
    if navigator.platform.match(/Win/i) then true else false

  @preventDef: (event) ->
    event.stopPropagation()
    event.preventDefault()

  @getInitials: (name) ->
    arr = (name.split(' ').map (s) -> s[0].toUpperCase())
    if arr.length > 2 then _.first(arr).concat(_.last(arr)) else arr.join('')

  @getBestImage: (images, size) ->
    filtered = images.filter (img) ->
      if size is 'small' then img.width < 300 else img.width < 1000 && img.width > 300
    _.head(filtered).url

module.exports = Helper
