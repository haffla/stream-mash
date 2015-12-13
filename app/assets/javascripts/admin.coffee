$ ->
  $('.coll-delete-button').on 'click', (evt) ->
    $.ajax
      url: $(evt.target).data('url')
      type: 'DELETE'
      success: (data) ->
        if data.success
          alert 'Deletion was successful'
        else
          alert 'There was a problem'
