package ba.sake.chatbot

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class ChatbotClientSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val clientName = "test-client"

  "A ChatbotClient" must {
    "do register action" in {
      val serverProbe = createTestProbe[ChatbotServer.Message]()
      val clientTest  = spawn(ChatbotClient(clientName, serverProbe.ref))
      clientTest ! ChatbotClient.DoRegister
      serverProbe.expectMessage(ChatbotServer.ClientRegister(clientTest.ref, clientName))
    }
    "do logout action" in {
      val serverProbe = createTestProbe[ChatbotServer.Message]()
      val clientTest  = spawn(ChatbotClient(clientName, serverProbe.ref))
      clientTest ! ChatbotClient.DoLogout
      serverProbe.expectMessage(ChatbotServer.ClientLogout(clientTest.ref, clientName))
    }
  }

}
