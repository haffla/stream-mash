class Uploader

  constructor: (@url) ->

  upload: (
    formData
    successCallback = () ->
    successNoErrorCallback = () ->
    ) ->
    $.ajax
      url: @url
      type: 'POST'
      data: formData
      dataType: 'json'
      cache: false
      contentType: false
      processData: false
      xhr: () ->
        xhr = new window.XMLHttpRequest()
        xhr.upload.addEventListener 'progress' , (evt) ->
          if evt.lengthComputable
            percentComplete = evt.loaded / evt.total
            console.log(percentComplete)
        , false
        xhr
      success: (data) ->
        successCallback()
        if data.error
          alert data.error
        else
          successNoErrorCallback()
          if data.redirect
            window.location.href = data.redirect
      error: (jqXHR, status, error) ->
        console.log('Error: '  + error + '\n' + 'Status: ' + status)

module.exports = Uploader
