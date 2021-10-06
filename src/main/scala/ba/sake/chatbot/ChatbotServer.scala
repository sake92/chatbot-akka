package ba.sake.chatbot

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import java.time.Instant

object ChatbotServer {

  sealed trait Message
  case class ClientRegister(from: ActorRef[ChatbotClient.Message], name: String) extends Message
  case class ClientLogout(from: ActorRef[ChatbotClient.Message], name: String)   extends Message

  case class AnalyzeMessage(from: ActorRef[ChatbotClient.Message], text: String)
      extends Message // TODO add msgId
  case object ExpireSessions extends Message

  private case class ClientData(
      ref: ActorRef[ChatbotClient.Message],
      name: String,
      lastAccess: Instant
  )

  def apply(): Behavior[Message] =
    bot(Set.empty, Map.empty)

  def apply(textMappings: Map[String, String]): Behavior[Message] =
    bot(Set.empty, textMappings)

  private def bot(clients: Set[ClientData], textMappings: Map[String, String]): Behavior[Message] =
    Behaviors.receive { (context, message) =>
      message match {
        case ClientRegister(clientRef, clientName) =>
          context.log.info(s"Registering client: $clientName")
          bot(clients + ClientData(clientRef, clientName, Instant.now()), textMappings)
        case ClientLogout(clientRef, clientName) =>
          context.log.info(s"Logging out client: $clientName")
          bot(clients.filterNot(_.ref == clientRef), textMappings)
        case AnalyzeMessage(from, text) =>
          if (!clients.map(_.ref).contains(from)) {
            from ! ChatbotClient.ErrorMessage("Client not registered! Please do register first! :)")
            Behaviors.same
          } else {
            val maybeLink = textMappings.find { case (key, _) =>
              text.contains(key.toUpperCase)
            }
            maybeLink.foreach { case (name, link) =>
              val msg = s"Here is more info about $name: $link"
              from ! ChatbotClient.AnalyzedMessage(msg)
            }
            val touchedClients = clients.map { c =>
              if (c.ref == from) c.copy(lastAccess = Instant.now()) else c
            }
            bot(touchedClients, textMappings)
          }
        case ExpireSessions =>
          val threshold      = Instant.now().minusSeconds(30)
          val expiredClients = clients.filter(_.lastAccess.isBefore(threshold))
          if (expiredClients.nonEmpty) {
            context.log.info(s"Expired clients: $expiredClients")
          }
          val validClients = clients -- expiredClients
          bot(validClients, textMappings)
      }
    }
}
