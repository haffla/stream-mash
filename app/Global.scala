import models.Config
import models.messaging.RabbitMQConnection
import models.messaging.listen.spotify.ArtistIdListener

object Global extends play.api.GlobalSettings {

  override def onStart(app: play.api.Application) {
    new ArtistIdListener().listen()
  }
}