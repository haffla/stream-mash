React = require 'react'
Helper = require '../../util/Helper'
Uploader = require '../../util/Uploader'

ItunesUpload = React.createClass

  componentWillMount: () ->
    jQuery.event.props.push 'dataTransfer'

  uploader: new Uploader('/itunes')

  dragEnter: (event) ->
    $('#dropzone').addClass('hover');
    Helper.preventDef(event)

  dragLeave: (event) ->
    Helper.preventDef(event)
    $('#dropzone').removeClass('hover')

  drop: (event) ->
    Helper.preventDef(event)
    unless window.File && window.FileList && window.FileReader
      return window.alert "Your browser does not support the File API"
    file = event.dataTransfer.files[0]
    if file.type is 'text/xml' && file.name.match(/^iTunes (Music )?Library/)
      formData = new FormData()
      formData.append 'file', file
      $('#dropzone').addClass('dropped')

      successCallback = () =>
        $('#dropzone').removeClass('dropped hover')
        @props.ws.send('itunes')
        @props.handleClose()

      @uploader.upload formData, false, successCallback
    else
      $('#dropzone').removeClass('hover')
      window.alert "Nono! Only iTunes Library XML files are allowed."

  render: () ->
    dropSentence = "Drop it on the iTunes Logo!"
    sentence = "The iTunes Music Library file is typically located under "
    <div>
      <div onDragOver={Helper.preventDef} onDrop={@drop} onDragEnter={@dragEnter} onDragLeave={@dragLeave}
      title="Drop your iTunes Library file here" id="dropzone"></div>
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


module.exports = ItunesUpload
