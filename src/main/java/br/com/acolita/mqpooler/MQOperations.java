package br.com.acolita.mqpooler;

import com.ibm.mq.MQMessage;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Provides operations for sending and receiving messages using IBM MQ.
 * This interface defines the contract for synchronous request-response
 * message exchanges.
 */
public interface MQOperations {

    /**
     * Sends a request message to a specified queue and waits for a response.
     * This method uses a default timeout for waiting for the response.
     *
     * @param message The message to be sent.
     * @return The response message received from the queue.
     * @throws Exception If an error occurs during sending or receiving the message,
     *                   including any MQ-related issues.
     */
    String requestResponse(String message) throws Exception;

    /**
     * Sends a request message to a specified queue and waits for a response
     * with a specified timeout.
     *
     * @param message The message to be sent.
     * @param timeout The maximum duration to wait for a response.
     * @return The response message received from the queue.
     * @throws Exception If an error occurs during sending or receiving the message,
     *                   including any MQ-related issues, or if the timeout expires.
     */
    String requestResponse(String message, Duration timeout) throws Exception;

    /**
     * Sends a request message to a specified queue using a custom setup for the
     * {@link MQMessage} and waits for a response. The message setup can be customized
     * using a {@link Consumer} to configure properties of the {@link MQMessage} before
     * it is sent.
     *
     * <p>This method allows advanced configurations of the request message, such as
     * setting custom headers, correlation IDs, or expiry settings. The {@link Consumer}
     * parameter is responsible for applying these configurations to the {@link MQMessage}
     * before sending it.
     *
     * @param setupper A {@link Consumer} that accepts an {@link MQMessage} and applies
     *                 the desired configurations to it before sending.
     * @return The response message received from the queue.
     * @throws Exception If an error occurs during message configuration, sending, or
     *                   receiving, including any MQ-related issues.
     */
    String requestResponse(Consumer<MQMessage> setupper) throws Exception;
}