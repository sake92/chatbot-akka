package ba.sake.chatbot

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ChatbotClient {
  sealed trait Message
  case object DoRegister                    extends Message
  case object DoLogout                      extends Message
  case class DoAnalyzeMessage(text: String) extends Message
  case class AnalyzedMessage(text: String)  extends Message
  case class ErrorMessage(text: String)  extends Message

  def apply(name: String, server: ActorRef[ChatbotServer.Message]): Behavior[Message] =
    Behaviors.receive { (context, message) =>
      message match {
        case AnalyzedMessage(text) =>
          context.log.info(s"Got analyzed message from server: $text")
          Behaviors.same
        case DoRegister =>
          server ! ChatbotServer.ClientRegister(context.self, name)
          Behaviors.same
        case DoLogout =>
          server ! ChatbotServer.ClientLogout(context.self, name)
          Behaviors.same
        case DoAnalyzeMessage(text) =>
          server ! ChatbotServer.AnalyzeMessage(context.self, text)
          Behaviors.same
        case ErrorMessage(msg) =>
          context.log.warn(s"Got error response from server: $msg")
          Behaviors.same
      }
    }
}
