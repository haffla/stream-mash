package models.messaging.push

import com.rabbitmq.client.MessageProperties
import models.Config
import models.messaging.RabbitMQConnection
import play.api.libs.json.Json

trait ArtistIdPusher {

  def pushToArtistIdQueue(name: String, id: String, typ:String):Unit = {
    val connection = RabbitMQConnection.getConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(Config.rabbitArtistIdQueue, true, false, false, null)
    val message = Json.toJson(Map("name" -> name, "id" -> id, "type" -> typ)).toString()
    channel.basicPublish("", Config.rabbitArtistIdQueue, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes)
    channel.close()
    connection.close()
  }
}