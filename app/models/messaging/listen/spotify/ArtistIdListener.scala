package models.messaging.listen.spotify

import com.rabbitmq.client._
import models.database.facade.{ServiceFacade, RdioFacade, SpotifyFacade}
import models.Config
import models.messaging.RabbitMQConnection
import play.api.libs.json.Json

object ArtistIdListener {

  var facades:Map[String, ServiceFacade] = Map(
    "spotify" -> SpotifyFacade,
    "rdio" -> RdioFacade
  )

  def listen() = {
    val connection = RabbitMQConnection.getConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(Config.rabbitArtistIdQueue, true, false, false, null)


    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag:String, envelope:Envelope, properties:AMQP.BasicProperties, body:Array[Byte]): Unit = {
        val message = new String(body, "UTF-8")
        val js = Json.parse(message)
        val artist:String = (js \ "name").as[String]
        val id:String = (js \ "id").as[String]
        val typ:String = (js \ "type").as[String]
        facades.get(typ) match {
          case Some(facade) => facade.saveArtistWithServiceId(artist, id)
          case None => throw new Exception("Facade of type $typ does not exist")
        }
      }
    }
    channel.basicConsume(Config.rabbitArtistIdQueue, true, consumer)
  }
}
