package models.messaging

import com.rabbitmq.client.{Connection, ConnectionFactory}
import models.Config

object RabbitMQConnection {

  private val connection: Connection = null

  def getConnection(): Connection = {
    connection match {
      case null => {
        val factory = new ConnectionFactory()
        factory.setHost(Config.RABBITMQ_HOST)
        factory.newConnection()
      }
      case _ => connection
    }
  }
}