package br.com.acolita.consumer;

import java.time.Duration;

public interface MQOperations {
    String requestResponse(String message) throws Exception;
    String requestResponse(String message, Duration timeout) throws Exception;
}
