package models.messaging.listen.spotify

import com.rabbitmq.client._
import database.facade.{RdioFacade, SpotifyFacade}
import models.Config
import models.messaging.RabbitMQConnection
import play.api.libs.json.Json

object ArtistIdListener {
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
        if(typ == "spotify") {
          SpotifyFacade.saveArtistId(artist,id)
        }
        else {
          RdioFacade.saveArtistId(artist,id)
        }
      }
    }
    channel.basicConsume(Config.rabbitArtistIdQueue, true, consumer)
  }
}
