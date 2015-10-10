package models.messaging

import com.rabbitmq.client._
import models.Config

class Receiver {
  def listen() = {
    val connection = RabbitMQConnection.getConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(Config.RABBITMQ_QUEUE, false, false, false, null)
    println("Waiting for messages")

    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag:String, envelope:Envelope, properties:AMQP.BasicProperties, body:Array[Byte]): Unit = {
        val message = new String(body, "UTF-8")
        println("Received " + message)
      }
    }
    channel.basicConsume(Config.RABBITMQ_QUEUE, true, consumer)
  }
}
