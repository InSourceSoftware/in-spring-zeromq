# in-spring-zeromq
Spring Boot enabled ZeromMQ integrations inspired by spring-rabbit and built on jzmq-api.

## Getting Started

This project is built on Spring Boot. Add this project as a dependency to your Spring Boot application to get started.

Maven:

```xml
<dependency>
  <groupId>io.insource</groupId>
  <artifactId>in-spring-zeromq</artifactId>
  <version>0.0.2</version>
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

To publish messages, use a `ZmqTemplate`. For example:

```java
ZmqTemplate zmqTemplate = new ZmqTemplate();
zmqTemplate.setTopic("events");                                      // Use topic "events"
zmqTemplate.setRoutingKey("MyEvent");                                // Use routing key "MyEvent" by default
zmqTemplate.setMessageConverter(new VerySimpleMessageConverter());   // Use custom MessageConverter

// Send using defaults
zmqTemplate.send("This is a test.");
``` 

This publishes a message with the string `This is a test.` to the `events` topic using a routing key of `MyEvent`. Other flavors of `send()` look like this:

```java
// Send using custom routing key
zmqTemplate.send("MyRoutingKey", "This is another test.");

// Send using custom routing key and headers
Map<String, String> headers = new HashMap<>();
headers.put("my-header", "TEST");
zmqTemplate.send("MyRoutingKey", headers, "This is yet another test.");
```

Serialization to and from bytes is done using a `MessageConverter`. Here is the definition of a very simple `MessageConverter`:

```java
public class VerySimpleMessageConverter implements MessageConverter {
  @Override
  public Message toMessage(Object obj, Map<String, String> headers) {
    return new Message(obj.toString());
  }

  @Override
  public Object fromMessage(Message message) {
    return message.popString();
  }
}
```

**Note:** This example does not consider headers. The bundled class `SimpleMessageConverter` does consider headers, which changes the data format over the wire considerably. Both sides must use the same `MessageConverter` or risk incompatible (de)serialization.

Messages sent on any thread within the JVM will be sent to a local proxy to be forwarded to a remote host. To configure the proxy to forward messages for this topic, use the following configuration:

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
        return new VerySimpleMessageConverter();
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

### Listener

This library also includes a basic socket listener abstraction, for times when pub/sub breaks down, and raw 0MQ messaging is required. Instead of forcing the listener to adopt a different programming model, the `@ZmqListener` annotation can be used.

To use the listener, add the `@EnableZmqListener` annotation to your Spring Boot application:

```java
@SpringBootApplication
@EnableZmqListener
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

To create a listener, add the `@ZmqListener` annotation to a class that implements `MessageListener`:

```java
import io.insource.framework.annotation.QueueBinding;
import io.insource.framework.annotation.ZmqSubscriber;
import io.insource.framework.zeromq.MessageListener;
import org.zeromq.api.Message;

@ZmqListener(1337)
public class MyListener implements MessageListener {
    @Override
    public void onMessage(Message message) {
        // Do something with message
        System.out.println(message.popString());
    }
}
```

This will create a PULL socket listening on port 1337 with its own inbox. The inbox can hold up to 1000 messages at a time. Additional messages will cause the sender to block.

Each inbox will be managed by a thread dedicated to dispatching messages to `MyListener`.

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
        return new VerySimpleMessageConverter();
    }
}
```

Then simply add a method to your `@ZmqListener` with the `@ZmqHandler` annotation. Make sure it accepts exactly the type returned by your `MessageConverter`:

```java
import io.insource.framework.annotation.QueueBinding;
import io.insource.framework.annotation.ZmqHandler;
import io.insource.framework.annotation.ZmqSubscriber;

@ZmqListener(1337)
public class MyListener {
    @ZmqHandler
    public void onMyEvent(String message) {
        // Do something with message
        System.out.println(message);
    }
}
``` 

## Contributing

To build this project, use the provided gradle build script.

`> ./gradlew build` 