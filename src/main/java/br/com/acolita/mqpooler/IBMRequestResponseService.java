package br.com.acolita.mqpooler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for handling request-response operations with IBM MQ.
 * It manages connections and provides access to {@link MQOperations}
 * for specified request and response queues.
 */
@Service
public class IBMRequestResponseService {
    private final MQConnectionManager connectionManager;

    /**
     * Constructs an IBMRequestResponseService with a specified
     * MQConnectionManager to manage connections.
     *
     * @param connectionManager The connection manager responsible for creating
     *                          and managing MQ connections.
     */
    @Autowired
    public IBMRequestResponseService(MQConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Provides an {@link MQOperations} instance for interacting with the
     * specified request and response queues.
     *
     * @param requestQueue  The name of the request queue.
     * @param responseQueue The name of the response queue.
     * @return An instance of {@link MQOperations} for the specified queues.
     */
    public MQOperations forQueues(String requestQueue, String responseQueue) {
        return connectionManager.forQueues(QueueDefinition.of(requestQueue, responseQueue));
    }

    private MQOperations forQueues(QueueDefinition queueDef) {
        return connectionManager.forQueues(queueDef);
    }
}