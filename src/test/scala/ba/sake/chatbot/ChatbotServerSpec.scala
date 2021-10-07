package ba.sake.chatbot

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class ChatbotServerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "A ChatbotServer" must {
    "return error when client not registered" in {
      val clientProbe = createTestProbe[ChatbotClient.Message]()
      val serverTest  = spawn(ChatbotServer())
      serverTest ! ChatbotServer.AnalyzeMessage(clientProbe.ref, "Hello test1!")
      clientProbe.expectMessageType[ChatbotClient.ErrorMessage]
    }
    "analyze message" in {
      val textMappings = Map("test1" -> "testLink")
      val clientProbe  = createTestProbe[ChatbotClient.Message]()
      val serverTest   = spawn(ChatbotServer(textMappings))
      serverTest ! ChatbotServer.ClientRegister(clientProbe.ref, "test-client")
      clientProbe.expectNoMessage()
      serverTest ! ChatbotServer.AnalyzeMessage(clientProbe.ref, "Hello test1!")
      clientProbe.expectMessage(
        ChatbotClient.AnalyzedMessage("Here is more info about test1: testLink")
      )
    }
  }

}
