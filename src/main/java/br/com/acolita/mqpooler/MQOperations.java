package br.com.acolita.mqpooler;

import java.time.Duration;

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
}