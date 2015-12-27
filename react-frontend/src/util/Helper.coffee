class Helper
  @calculateNrOfAlbums: (data) ->
    _.values(data).reduce (x,y) ->
      x + y.albums.length
    , 0

module.exports = Helper
