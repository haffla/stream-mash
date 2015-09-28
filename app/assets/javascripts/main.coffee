window.Itunes ||= {}

class Itunes.File

  killEvent: (event) =>
    event.stopPropagation()
    event.preventDefault()
    $('#dropzone').addClass('hover')

  dragEnter: (event) =>
    @killEvent(event)

  dragLeave: (event) =>
    @killEvent(event)
    $('#dropzone').removeClass('hover')

  drop: (event) =>
    @killEvent(event)

    unless window.File && window.FileList && window.FileReader
      return window.alert "Your browser does not support the File API"
    file = event.dataTransfer.files[0]
    if file.type is 'text/xml'
      $('#dropzone').addClass('dropped')
      fileReader = new FileReader()
      $(fileReader).on 'load', () ->
        text = fileReader.result
        $.ajax(
          url: '/ifile'
          type: 'POST'
          data: text
          cache: false
          dataType: 'json'
          processData: false
          contentType: 'text/xml'
          success: (response) ->
            console.log(response)
            $('#dropzone').removeClass('dropped')
          error: (jqXHR, textStatus, errorThrown) -> console.log(textStatus + " oooo " + errorThrown)
        )
      fileReader.readAsText(file)
    else
      return window.alert "Nono!! Only XML"

  init: ->
    dropzone = $("#dropzone").get(0)
    dropzone.addEventListener "dragenter", @dragEnter
    dropzone.addEventListener "dragleave", @dragLeave
    dropzone.addEventListener "drop",      @drop
    dropzone.addEventListener "dragover",  @killEvent

$ ->
  $.material.init()
  it = new Itunes.File()
  it.init()