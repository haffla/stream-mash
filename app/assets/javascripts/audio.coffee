class Uploader

  @upload: (files) ->
    formData = new FormData()
    for file in files
      console.log file.name
      formData.append 'files[]', file, file.name

    $.ajax
      url: '/fileupload'
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
      success: (data) =>
        if data.error
          alert data.error
        else if data.redirect
          window.location.href = data.redirect
        else
          alert "Sorry, something went wrong!"
      error: (jqXHR, status, error) =>
        console.log('Error: '  + error + '\n' + 'Status: ' + status)


$ ->

  jQuery.event.props.push 'dataTransfer'

  window.addEventListener 'dragover', (evt) ->
    e = evt || event
    e.preventDefault()
  , false

  window.addEventListener 'drop', (evt) ->
    e = evt || event;
    e.preventDefault()
  , false

  $('#audio-submit').on 'click', () ->
    files = $('#audio-files')[0].files
    Uploader.upload files

  $('#audio-drop').on 'dragenter', (evt) ->
    console.log('dragenter')
    evt.stopPropagation()
    evt.preventDefault()

  $('#audio-drop').on 'dragleave', (evt) ->
    console.log('dragleave')
    evt.stopPropagation()
    evt.preventDefault()

  $('#audio-drop').on 'drop', (e) ->
    files = e.target.files || e.dataTransfer.files
    Uploader.upload files



