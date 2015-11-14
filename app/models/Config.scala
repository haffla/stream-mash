package models

import com.typesafe.config.ConfigFactory

object Config {
  val rabbitMqHost = ConfigFactory.load().getString("rabbitmq.host")
  val rabbitArtistIdQueue = ConfigFactory.load().getString("rabbitmq.queue")
}