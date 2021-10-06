package ba.sake.chatbot

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ChatbotServer {

  sealed trait Message
  case class ClientRegister(from: ActorRef[ChatbotClient.Message], name: String) extends Message
  case class ClientLogout(from: ActorRef[ChatbotClient.Message], name: String)   extends Message
  case class AnalyzeMessage(from: ActorRef[ChatbotClient.Message], text: String) extends Message

  private case class ClientData(ref: ActorRef[ChatbotClient.Message], name: String)

  def apply(): Behavior[Message] =
    bot(Set.empty)

  def bot(clients: Set[ClientData]): Behavior[Message] = Behaviors.receive { (context, message) =>
    message match {
      case ClientRegister(clientRef, clientName) =>
        context.log.info(s"Registering client: $clientName")
        bot(clients + ClientData(clientRef, clientName))
      case ClientLogout(clientRef, clientName) =>
        context.log.info(s"Logging out client: $clientName")
        bot(clients.filterNot(_.ref == clientRef))
      case AnalyzeMessage(from, text) =>
        from ! ChatbotClient.AnalyzedMessage("bla")
        Behaviors.same
    }
  }
}
