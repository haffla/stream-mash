$ ->
  $('.form-prevent-default').on('submit', (event) ->
    event.preventDefault()
  )
