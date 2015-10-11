import models.Config
import models.messaging.{Receiver, RabbitMQConnection}

object Global extends play.api.GlobalSettings {

  override def onStart(app: play.api.Application) {
    val connection = RabbitMQConnection.getConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(Config.RABBITMQ_QUEUE, false, false, false, null)
    val message = "Hello"
    channel.basicPublish("", Config.RABBITMQ_QUEUE, null, message.getBytes)
    println("Sent " + message)
    channel.close()
    connection.close()
    new Receiver().listen()

  }
}