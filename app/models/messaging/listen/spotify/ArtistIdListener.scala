package models.messaging.listen.spotify

import com.rabbitmq.client._
import models.Config
import models.messaging.RabbitMQConnection
import models.util.SpotifyLibrary

class ArtistIdListener {
  def listen() = {
    val connection = RabbitMQConnection.getConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(Config.RABBITMQ_QUEUE, true, false, false, null)

    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag:String, envelope:Envelope, properties:AMQP.BasicProperties, body:Array[Byte]): Unit = {
        val message = new String(body, "UTF-8")
        val splitMessage:Array[String] = message.split("""\|%\|""")
        val artist:String = splitMessage(0)
        val id:String = splitMessage(1)
        SpotifyLibrary.saveArtistId(artist,id)
      }
    }
    channel.basicConsume(Config.RABBITMQ_QUEUE, true, consumer)
  }
}
