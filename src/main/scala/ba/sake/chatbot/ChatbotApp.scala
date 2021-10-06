package ba.sake.chatbot

import akka.actor.typed.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.io.StdIn

object ChatbotApp extends App {

  // setup server
  val server = ActorSystem(
    ChatbotServer(
      Map("Stack Builders" -> "https://stackbuilders.com", "Sakib" -> "https://sake.ba")
    ),
    "ChatbotServer"
  )
  locally {
    import server.executionContext
    server.scheduler.scheduleAtFixedRate(Duration.Zero, 1.second) { () =>
      server ! ChatbotServer.ExpireSessions
    }
  }

  // setup client
  val client = ActorSystem(ChatbotClient("console-client", server), "ChatbotClient")

  // loop interaction
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
