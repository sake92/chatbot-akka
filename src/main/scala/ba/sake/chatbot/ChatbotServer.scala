package ba.sake.chatbot

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

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

object ChatbotClient {
  sealed trait Message
  case object DoRegister                    extends Message
  case object DoLogout                      extends Message
  case class DoAnalyzeMessage(text: String) extends Message
  case class AnalyzedMessage(text: String)  extends Message

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
      }
    }
}

object ChatbotApp extends App {
  val server: ActorSystem[ChatbotServer.Message] = ActorSystem(ChatbotServer(), "ChatbotServer")

  val client: ActorSystem[ChatbotClient.Message] =
    ActorSystem(ChatbotClient("console-client", server), "ChatbotClient")

  while (true) {
    println(s"""Please select one option
         |Q - quit
         |R - register
         |L - logout
         |anything else - analyze message
         |""".stripMargin)
    StdIn.readLine().trim.toUpperCase match {
      case "Q" =>
        server.terminate()
        client.terminate()
        println("Exiting...")
        Await.ready(server.whenTerminated, Duration.Inf)
        Await.ready(client.whenTerminated, Duration.Inf)
        System.exit(0)
      case "R" =>
        println("rrrrrr")
        client ! ChatbotClient.DoRegister
      case "L" => client ! ChatbotClient.DoLogout
      case msg => client ! ChatbotClient.DoAnalyzeMessage(msg)
    }
  }

}
