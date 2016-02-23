class TagReader

  @readFiles: (files) ->
    promises = (idx for idx in [0...files.length]).map (i) ->
      file = files[i]
      if file.type.match(/audio\/*/)
        TagReader.readFile(files[i])
      else
        {}
    Promise.all(promises)

  @readFile: (file) ->
    new Promise (resolve) ->
      window.jsmediatags.read(file, {
        onSuccess: (tag) ->
          resolve {
            artist: tag.tags.artist,
            album: tag.tags.album || 'UNKNOWNALBUM',
            title: tag.tags.title
          }
        onError: (error) ->
          console.log(error)
        }, false)


  @filterData: (obj) ->
    !(_.isUndefined(obj.artist) or _.isUndefined(obj.title) or _.isUndefined(obj.album))

module.exports = TagReader
