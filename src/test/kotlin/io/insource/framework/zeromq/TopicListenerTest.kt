package io.insource.framework.zeromq

import io.insource.framework.annotation.QueueBinding
import io.insource.framework.annotation.ZmqHandler
import io.insource.framework.annotation.ZmqSubscriber
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.zeromq.api.Message
import java.util.concurrent.CountDownLatch

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ZeromqTestConfiguration::class])
class TopicListenerTest {
  @ZmqSubscriber(
    QueueBinding(topic = "TopicListenerTest", key = "Greeting", queue = "greetings"),
    QueueBinding(topic = "TopicListenerTest", key = "Message", queue = "messages")
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
    val channelFactory = ChannelFactory.create()
    val channel = channelFactory.createChannel("TopicListenerTest")
    channel.send("Greeting", Message("Hello, World"))
    channel.send("Message", Message("This is a message."))

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