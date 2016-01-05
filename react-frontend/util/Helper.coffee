class Helper
  @calculateNrOfAlbums: (data) ->
    _.values(data).reduce (x,y) ->
      x + y.albums.length
    , 0

  @isMac: () ->
    if navigator.platform.match(/(Mac|iPhone|iPod|iPad)/i) then true else false

  @isWindows: () ->
    if navigator.platform.match(/Win/i) then true else false

module.exports = Helper
