# mqpooler

## Prerequisites

1. Java Development Kit (JDK) 8 or later.
2. Apache Maven or Gradle for dependency management.
3. IBM MQ libraries included in the project classpath.
4. Spring Boot application configuration.

## Installation

Include the necessary dependencies for Spring and IBM MQ in your `pom.xml` or `build.gradle` file. Below is an example for Maven:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.ibm.mq</groupId>
    <artifactId>mq-allclient</artifactId>
    <version>9.2.0.0</version>
</dependency>
```

## Usage
The `IBMRequestResponseService` is a Spring-managed service that provides access to `MQOperations`, which defines methods for synchronous request-response communication with IBM MQ.

### Key Components
- **IBMRequestResponseService**: Manages connections and provides `MQOperations` for interacting with specific queues.
- **MQOperations**: Interface defining methods for sending and receiving messages with IBM MQ.

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

The `MQOperations` interface provides two methods for request-response communication:

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

### 3. Defining Queue Connections

Queues are defined using the `forQueues` method in `IBMRequestResponseService`. Ensure the queue names match your IBM MQ configuration.

```java
MQOperations mqOperations = requestResponseService.forQueues("REQUEST.QUEUE", "RESPONSE.QUEUE");
```

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

1. **Resource Management**: Ensure MQ connections are efficiently managed to avoid resource leaks.
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
