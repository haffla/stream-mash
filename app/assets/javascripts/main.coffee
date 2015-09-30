App = {}

class App.ItunesFileImport

  preventDef: (event) =>
    event.stopPropagation()
    event.preventDefault()
    $('#dropzone').addClass('hover')

  dragEnter: (event) =>
    @preventDef(event)

  dragLeave: (event) =>
    @preventDef(event)
    $('#dropzone').removeClass('hover')

  drop: (event) =>
    @preventDef(event)
    unless window.File && window.FileList && window.FileReader
      return window.alert "Your browser does not support the File API"
    file = event.dataTransfer.files[0]
    if file.type is 'text/xml' && file.name.match(/^iTunes (Music )?Library/)
      formData = new FormData()
      formData.append 'file', file
      $('#dropzone').addClass('dropped')
      $.ajax
        url: '/itunes'
        type: 'POST'
        data: formData
        dataType: 'json'
        cache: false
        contentType: false
        processData: false
        success: (response) ->
          console.log(response)
          $('#dropzone').removeClass('dropped hover')
        error: (jqXHR, status, error) ->
          console.log('Error: '  + error + '\n' + 'Status: ' + status)
    else
      return window.alert "Nono!! Only XML"

  handleUpload: (event) ->
    event.preventDefault()
    event.stopPropagation()
    fileSelect = $('#file-select').get(0)
    file = fileSelect.files[0]
    formData = new FormData()
    formData.append 'file', file
    $.ajax(
      url: '/itunes'
      type: 'POST'
      data: formData
      dataType: 'json'
      cache: false
      contentType: false
      processData: false
      success: (response) ->
        console.log(response)
        $('#dropzone').removeClass('dropped')
      error: (jqXHR, status, error) ->
        console.log('Error: '  + error + '\n' + 'Status: ' + status)
    )

  init: ->
    $.event.props.push('dataTransfer')
    dropzone = $("#dropzone")
    dropzone.on "dragenter", @dragEnter
    dropzone.on "dragleave", @dragLeave
    dropzone.on "drop",      @drop
    dropzone.on "dragover",  @preventDef

$ ->
  $.material.init()
  it = new App.ItunesFileImport()
  it.init()