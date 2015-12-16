import models.messaging.listen.ArtistIdListener

object Global extends play.api.GlobalSettings {

  override def onStart(app: play.api.Application) {
    ArtistIdListener.listen()
  }
}