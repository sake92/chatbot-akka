package ba.sake.chatbot

import akka.actor.typed.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

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
        client ! ChatbotClient.DoRegister
      case "L" =>
        client ! ChatbotClient.DoLogout
      case msg =>
        client ! ChatbotClient.DoAnalyzeMessage(msg)
    }
  }

}
