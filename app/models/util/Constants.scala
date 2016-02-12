package models.util

object Constants {
  val maxArtistCountToAnalyse = 30
  val maxPlaylistCountToImport = "5"
  val serviceLastFm = "lastfm"
  val serviceSoundcloud = "soundcloud"
  val serviceNapster = "napster"
  val serviceDeezer = "deezer"
  val serviceSpotify = "spotify"
  val intendedLocation = "intended_location"

  val sessionTamperingMessage = "The session has been tampered with."

  val userSessionKey = "user_session_key"
  val userId = "user_id"
  val username = "username"
  val authSecret = "auth_secret"
  val fileHashCacheKeyPrefix = "it-file-hash|"

  val accessTokenRetrievalError = "The access token could not be retrieved"
  val userTracksRetrievalError  = "Error requesting user tracks"

  val stateMismatchError = "Error: State Mismatch. You might be a victim of a CSRF attack."
  val missingOAuthCodeError = "The service did not send an OAuth code."

  val mapKeyArtist = "artist"
  val mapKeyArtistPic = "artist_pic"
  val mapKeyTrack = "track"
  val mapKeyAlbum = "album"
  val mapKeyServiceId = "service_id"
  val mapKeyService = "service"
  val mapKeyUnknownAlbum = "UNKNOWNALBUM"
  val mapKeyUnknownArtist = "UNKNOWNARTIST"

  val jsonKeyAccessToken = "access_token"
  val jsonKeyRefreshToken = "refresh_token"
  val jsonKeyAlbumsOnlyInUserCollection = "albumsOnlyInUserCollection"
  val jsonKeyArtists = "artists"
  val jsonKeyStats = "stats"
  val jsonKeyNrUserAlbs = "nrUserAlbums"
  val jsonKeyNrAlbs = "nrAlbums"
  val jsonKeyNrArts = "nrArtists"
}
