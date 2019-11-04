# in-spring-zeromq
Spring Boot enabled ZeromMQ integrations inspired by spring-rabbit and built on jzmq-api.

## Getting Started

This project is built on Spring Boot. Add this project as dependency to your Spring Boot application to get started.

Maven:

```xml
<dependency>
  <groupId>io.insource</groupId>
  <artifactId>in-spring-zeromq</artifactId>
  <version>0.0.1</version>
</dependency> 
```

Gradle:

```kotlin
implementation("io.insource:in-spring-zeromq:0.0.1")
```

### Publisher

To use the publisher, add the `@EnableZmqPublisher` annotation to your Spring Boot application:

```java
@SpringBootApplication
@EnableZmqPublisher
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

To publish messages, use a `ChannelFactory` to create `ThreadLocal` instances of a `Channel`. For example:

```java
ChannelFactory channelFactory = ChannelFactory.create("events"); // Use topic "events"
Channel channel = channelFactory.channel();                      // Get a thread-safe channel
channel.send("MyEvent", new Message("my event"));                // Send a message using routing key "MyEvent"
```

This publishes a message with the string `my event` to the `events` topic using a routing key of `MyEvent`.

**Note:** Serialization on the publishing side will be added in a future release. Currently, you must use the `org.zeromq.api.Message` class to send messages.

This message, and all messages sent on any thread within this JVM will be sent to a local proxy to be forwarded to a remote host. To configure the proxy to forward messages for this topic, use the following configuration:

application.yml:

```yaml
zmq:
  publisher:
    host: 10.0.0.123
    port: 1337
    topics:
      - events
```

### Subscriber

To use the subscriber, add the `@EnableZmqSubscriber` annotation to your Spring Boot application:

```java
@SpringBootApplication
@EnableZmqSubscriber
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

To create a subscriber, add the `@ZmqSubscriber` annotation to a class that implements `MessageListener`:

```java
import io.insource.framework.annotation.QueueBinding;
import io.insource.framework.annotation.ZmqSubscriber;
import io.insource.framework.zeromq.MessageListener;
import org.zeromq.api.Message;

@ZmqSubscriber({
    @QueueBinding(topic = "events", key = "MyEvent", queue = "my-events"),
    @QueueBinding(topic = "events", key = "AnotherEvent", queue = "other")
})
public class MySubscriber implements MessageListener {
    @Override
    public void onMessage(Message message) {
        // Do something with message
        System.out.println(message.popString());
    }
}
```

This will create queues called `my-events` and `other` each with their own inbox. Each inbox can hold up to 1000 messages at a time. Additional messages will be discarded.

Both inboxes will be managed by a thread dedicated to dispatching messages to `MySubscriber`. Only events with routing key `MyEvent` and `AnotherEvent` on topic `events` will be delivered. Other topics and routing keys will be ignored.

Alternatively, you can register a `MessageConverter` in your Spring `@Configuration` to deserialize messages into Java objects:

```java
import io.insource.framework.zeromq.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zeromq.api.Message;

@Configuration
public class MyConfiguration {
    @Bean
    public MessageConverter messageConverter() {
        return new MyMessageConverter();
    }

    public static class MyMessageConverter implements MessageConverter {
        @Override
        public Object fromMessage(Message message) {
            return message.popString();
        }

        @Override
        public Message toMessage(Object obj) {
            return new Message(obj.toString());
        }
    }
}
```

Then simply add a method to your `@ZmqSubscriber` with the `@ZmqHandler` annotation. Make sure it accepts exactly the type returned by your `MessageConverter`:

```java
import io.insource.framework.annotation.QueueBinding;
import io.insource.framework.annotation.ZmqHandler;
import io.insource.framework.annotation.ZmqSubscriber;

@ZmqSubscriber({
    @QueueBinding(topic = "events", key = "MyEvent", queue = "my-events"),
    @QueueBinding(topic = "events", key = "AnotherEvent", queue = "other")
})
public class MySubscriber {
    @ZmqHandler
    public void onMyEvent(String message) {
        // Do something with message
        System.out.println(message);
    }
}
``` 

**Note:** More flexible method signatures for `@ZmqHandler` will be added in a future release.

## Contributing

To build this project, use the provided gradle build script.

`> ./gradlew build` 