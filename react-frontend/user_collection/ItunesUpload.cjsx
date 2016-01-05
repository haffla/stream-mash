React = require 'react'
Helper = require '../util/Helper'

ItunesUpload = React.createClass

  preventDef: (event) ->
    event.stopPropagation()
    event.preventDefault()
    $('#dropzone').addClass('hover')

  dragEnter: (event) ->
    @preventDef(event)

  dragLeave: (event) ->
    @preventDef(event)
    $('#dropzone').removeClass('hover')

  drop: (event) ->
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
        success: (data) =>
          $('#fileuploadmodal').modal 'hide'
          window.itunes.openmodal = 'no'
          if !data.error
            $('#dropzone').removeClass('dropped hover')
            @props.ws.send('itunes')
          else
            window.alert("We could not read the file.")
        error: (jqXHR, status, error) =>
          console.log('Error: '  + error + '\n' + 'Status: ' + status)
    else
      return window.alert "Nono! Only iTunes Library XML files are allowed"

  render: () ->
    dropSentence = "Drop it on the iTunes Logo!"
    sentence = "The iTunes Music Library file is typically located under "
    <div id="fileuploadmodal" className="modal fade">
        <div className="modal-dialog">
            <div className="modal-content">
                <div className="modal-header">
                    <button type="button" className="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                </div>
                <div className="modal-body">
                    <div title="Drop your iTunes Library file here" id="dropzone" onDragOver={@preventDef}
                         onDrop={@drop} onDragEnter={@dragEnter} onDragLeave={@dragLeave}>
                    </div>
                    <div className="drop-instruction centered">
                      {
                        if Helper.isMac()
                          <h4>{sentence} <br/>/Users/[username]/Music/iTunes/iTunes Music Library.xml<br/>{dropSentence}</h4>
                        else if Helper.isWindows()
                          <h4>{sentence} <br/>C:\Users\[username]\Music\iTunes\iTunes Music Library.xml<br/>{dropSentence}</h4>
                        else
                          <h4>Drop your iTunes Music Library.xml above on the logo!</h4>
                      }
                    </div>
                </div>
                <div className="modal-footer">
                    <button type="button" className="btn btn-default" data-dismiss="modal">Close</button>
                    <button type="button" className="btn btn-primary">Show Normal File Upload Dialog</button>
                </div>
            </div>
        </div>
    </div>


module.exports = ItunesUpload
