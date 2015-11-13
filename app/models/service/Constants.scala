package models.service

object Constants {
  val accessTokenRetrievalError = "The access token could not be retrieved"
  val userTracksRetrievalError  = "Error requesting user tracks"

  val stateMismatchError = "Error: State Mismatch. You might be a victim of a CSRF attack."

  val mapKeyArtist = "artist"
  val mapKeyAlbum = "album"

  val jsonKeyAccessToken = "access_token"
}
