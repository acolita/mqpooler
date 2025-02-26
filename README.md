![License](https://img.shields.io/github/license/acolita/mqpooler)
![Maven Central](https://img.shields.io/maven-central/v/br.com.acolita/mqpooler)
![GitHub issues](https://img.shields.io/github/issues/acolita/mqpooler)
![Java Version](https://img.shields.io/badge/Java-8%2B-blue)
![GitHub last commit](https://img.shields.io/github/last-commit/acolita/mqpooler)

# mqpooler

`mqpooler` is a Java library that provides connection pooling for the IBM MQ client, enhancing the efficiency and scalability of message-driven applications. It offers seamless integration with Spring, allowing for automatic handling of connection lifecycles and dependency injection. Designed for ease of use, the library delivers a robust interface, `MQOperations`, that facilitates synchronous request-response interactions with MQ queues, ensuring efficient message processing with configurable timeout settings to meet the demands of enterprise environments.

![image](https://github.com/user-attachments/assets/91c4f515-4d58-47af-af7e-0db61a924da6)

## Prerequisites

1. Java Development Kit (JDK) 8 or later.
2. Apache Maven for dependency management.
3. IBM MQ libraries included in the project classpath.
4. Spring Boot application configuration.

## Installation

Include the necessary dependencies for mqpooler in your `pom.xml` file. Below is an example:

```xml
<dependency>
    <groupId>br.com.acolita</groupId>
    <artifactId>mqpooler</artifactId>
    <version>1.0</version>
</dependency>
```

## Usage
The `IBMRequestResponseService` is a service that provides access to `MQOperations`, which defines methods for synchronous request-response communication with IBM MQ. It leverages a connection pooling mechanism provided by the `MQConnectionManager` to enhance efficiency and scalability.

### Key Components
- **IBMRequestResponseService**: Manages connections and provides `MQOperations` for interacting with specific queues.
- **MQOperations**: Interface defining methods for sending and receiving messages with IBM MQ.
- **MQConnectionManager**: Handles connection pooling for MQ operations, using configurable properties for pool management.

### Configurable Properties
`mqpooler` requires the following configurable properties, which can be set in your application.properties or application.yml file:

- `acolita.mqpooler.max-pool-size`: Specifies the maximum number of connections in the pool.
- `acolita.mqpooler.queue-manager-name`: The name of the MQ Queue Manager used for connections.
- `acolita.mqpooler.properties.host-name`: The hostname of the MQ server.
- `acolita.mqpooler.properties.port`: The port of the MQ server.
- `acolita.mqpooler.properties.user-id`: The user ID for authentication.
- `acolita.mqpooler.properties.password`: The password for authentication.
- `acolita.mqpooler.properties.channel`: The MQ channel name.

#### Example Configuration:
```properties
acolita.mqpooler.max-pool-size=30
acolita.mqpooler.queue-manager-name=QM1
acolita.mqpooler.properties.host-name=mq.example.com
acolita.mqpooler.properties.port=1414
acolita.mqpooler.properties.user-id=admin
acolita.mqpooler.properties.password=secret
acolita.mqpooler.properties.channel=DEV.APP.SVRCONN
```

### 1. Autowiring the IBMRequestResponseService

Use Spring's `@Autowired` annotation to inject the `IBMRequestResponseService` into your component or service.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MQClient {

    private final IBMRequestResponseService requestResponseService;

    @Autowired
    public MQClient(IBMRequestResponseService requestResponseService) {
        this.requestResponseService = requestResponseService;
    }

    public void executeRequestResponse() {
        MQOperations mqOperations = requestResponseService.forQueues("REQUEST.QUEUE", "RESPONSE.QUEUE");

        try {
            String response = mqOperations.requestResponse("Sample Request Message");
            System.out.println("Received response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 2. Sending a Request and Receiving a Response

The `MQOperations` interface provides three methods for request-response communication:

#### Default Timeout

```java
String response = mqOperations.requestResponse("Your Message Here");
```

#### Custom Timeout

Specify a timeout using the `Duration` class:

```java
import java.time.Duration;

String response = mqOperations.requestResponse("Your Message Here", Duration.ofSeconds(30));
```

#### Advanced Message Configuration

Use the `requestResponse` method with a `Consumer` to configure the `MQMessage`:

```java
import com.ibm.mq.MQMessage;

String response = mqOperations.requestResponse(mqMessage -> {
    try {
        mqMessage.format = CMQC.MQFMT_STRING;
        mqMessage.characterSet = 37;
        mqMessage.replyToQueueName = "RESPONSE.QUEUE";
        mqMessage.writeString("Custom Configured Message");
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
});
```

### 3. Connection Pooling

The `MQConnectionManager` handles connection pooling to optimize performance and resource usage. Connections are pooled using `GenericObjectPool` from Apache Commons Pool2, with the pool size controlled by the `acolita.mqpooler.max-pool-size` property.

The `MQConnectionFactory` creates and validates connections, ensuring they are ready for use in the pool. Invalid connections are automatically closed and removed from the pool.

### 4. Exception Handling

Handle exceptions that may occur during message exchange. These exceptions may be related to connection issues, message format, or timeout expiration.

```java
try {
    String response = mqOperations.requestResponse("Your Message Here");
} catch (Exception e) {
    System.err.println("Error during request-response operation: " + e.getMessage());
}
```

## Best Practices

1. **Resource Management**: Leverage connection pooling to avoid creating excessive MQ connections and ensure efficient resource usage.
2. **Error Handling**: Implement robust exception handling for production use cases.
3. **Timeouts**: Use appropriate timeouts to prevent hanging operations.
4. **Security**: Secure MQ connections using SSL/TLS and configure proper authentication.

## Example Application

```java
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MQApplication implements CommandLineRunner {

    private final MQClient mqClient;

    public MQApplication(MQClient mqClient) {
        this.mqClient = mqClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(MQApplication.class, args);
    }

    @Override
    public void run(String... args) {
        mqClient.executeRequestResponse();
    }
}
```
