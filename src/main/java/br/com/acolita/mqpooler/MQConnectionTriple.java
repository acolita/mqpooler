package br.com.acolita.mqpooler;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

class MQConnectionTriple implements AutoCloseable {
    private final MQQueueManager queueManager;
    private final MQQueue requestQueue;
    private final MQQueue responseQueue;
    private volatile boolean isValid = true;

    public MQConnectionTriple(MQQueueManager queueManager, MQQueue requestQueue, MQQueue responseQueue) {
        this.queueManager = queueManager;
        this.requestQueue = requestQueue;
        this.responseQueue = responseQueue;
    }

    public MQQueue getRequestQueue() {
        return requestQueue;
    }

    public MQQueue getResponseQueue() {
        return responseQueue;
    }

    public boolean isValid() {
        return isValid && queueManager != null && queueManager.isConnected();
    }

    public void invalidate() {
        isValid = false;
    }

    @Override
    public void close() {
        try {
            if (requestQueue != null) {
                requestQueue.close();
            }
        } catch (MQException e) {
            // Log error but continue closing other resources
        }

        try {
            if (responseQueue != null) {
                responseQueue.close();
            }
        } catch (MQException e) {
            // Log error but continue closing other resources
        }

        try {
            if (queueManager != null) {
                queueManager.disconnect();
            }
        } catch (MQException e) {
            // Log error
        }
    }
}