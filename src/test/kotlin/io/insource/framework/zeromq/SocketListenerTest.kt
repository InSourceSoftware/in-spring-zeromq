package io.insource.framework.zeromq

import io.insource.framework.annotation.ZmqHandler
import io.insource.framework.annotation.ZmqListener
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.zeromq.Sockets
import org.zeromq.api.Message
import org.zeromq.api.SocketType
import java.util.concurrent.CountDownLatch

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ZeromqTestConfiguration::class])
class SocketListenerTest {
  @ZmqListener(52346)
  class Listener {
    @ZmqHandler
    fun onMessage(m: String) {
      messages += m
      countDownLatch.countDown()
    }
  }

  @BeforeEach
  fun setUp() {
  }

  @Test
  fun testHello() {
    val socket = Sockets.connect(SocketType.PUSH, "tcp://localhost:52346")
    socket.send(Message("Hello, World"))
    socket.send(Message("This is a message."))

    countDownLatch.await()
    assertThat(messages, hasItem("Hello, World"))
    assertThat(messages, hasItem("This is a message."))
  }

  companion object {
    val messages = mutableListOf<String>()
    val countDownLatch = CountDownLatch(2)
  }
}