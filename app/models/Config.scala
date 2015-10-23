package models

import com.typesafe.config.ConfigFactory

object Config {
  val rabbitMqHost = ConfigFactory.load().getString("rabbitmq.host")
  val rabbitMqQueue = ConfigFactory.load().getString("rabbitmq.queue")
}