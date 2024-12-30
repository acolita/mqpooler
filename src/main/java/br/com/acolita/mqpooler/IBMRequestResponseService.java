package br.com.acolita.mqpooler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IBMRequestResponseService {
    private final MQConnectionManager connectionManager;

    @Autowired
    public IBMRequestResponseService(MQConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public MQOperations forQueues(String requestQueue, String responseQueue) {
        return connectionManager.forQueues(QueueDefinition.of(requestQueue, responseQueue));
    }

    private MQOperations forQueues(QueueDefinition queueDef) {
        return connectionManager.forQueues(queueDef);
    }
}