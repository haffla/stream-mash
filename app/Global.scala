import models.messaging.listen.spotify.ArtistIdListener

object Global extends play.api.GlobalSettings {

  override def onStart(app: play.api.Application) {
    ArtistIdListener.listen()
  }
}