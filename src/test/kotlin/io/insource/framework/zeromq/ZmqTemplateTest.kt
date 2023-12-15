package io.insource.framework.zeromq

import io.insource.framework.annotation.QueueBinding
import io.insource.framework.annotation.ZmqHandler
import io.insource.framework.annotation.ZmqSubscriber
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch

@SpringBootTest(classes = [ZeromqTestConfiguration::class])
class ZmqTemplateTest {
  @ZmqSubscriber(
    QueueBinding(topic = "ZmqTemplateTest", key = "Greeting", queue = "greetings"),
    QueueBinding(topic = "ZmqTemplateTest", key = "Message", queue = "messages")
  )
  class Subscriber {
    @ZmqHandler
    fun onMessage(m: String) {
      messages += m
      countDownLatch.countDown()
    }
  }

  @BeforeEach
  fun setUp() {
    // Allow a little time for PUB and SUB to connect to each other
    Thread.sleep(50)
  }

  @Test
  fun testHello() {
    val zmqTemplate = ZmqTemplate().apply {
      topic = "ZmqTemplateTest"
      routingKey = "Message"
      messageConverter = VerySimpleMessageConverter()
    }

    // Received using default routing key of Message
    zmqTemplate.send("This is a message.")
    // Received using explicit routing key of Greeting
    zmqTemplate.send("Greeting", "Hello, World")
    // Not received using bunk routing key
    zmqTemplate.send("Nothing", "This message is not received.")

    countDownLatch.await()
    assertThat(messages, hasSize(2))
    assertThat(messages, hasItem("Hello, World"))
    assertThat(messages, hasItem("This is a message."))
  }

  companion object {
    val messages = mutableListOf<String>()
    val countDownLatch = CountDownLatch(2)
  }
}
