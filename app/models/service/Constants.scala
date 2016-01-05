package models.service

object Constants {
  val userSessionKey = "user_session_key"
  val fileHashCacheKeyPrefix = "it-file-hash|"

  val accessTokenRetrievalError = "The access token could not be retrieved"
  val userTracksRetrievalError  = "Error requesting user tracks"

  val stateMismatchError = "Error: State Mismatch. You might be a victim of a CSRF attack."
  val missingOAuthCodeError = "The service did not send an OAuth code."

  val mapKeyArtist = "artist"
  val mapKeyTrack = "track"
  val mapKeyAlbum = "album"
  val mapKeyServiceId = "service_id"
  val mapKeyService = "service"
  val mapKeyUnknownAlbum = "UNKNOWNALBUM"

  val jsonKeyAccessToken = "access_token"
}
