package br.com.acolita.mqpooler;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.CMQC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Consumer;

class MQOperationsImpl implements MQOperations {
    private static final Logger logger = LoggerFactory.getLogger(MQOperationsImpl.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private final MQConnectionManager connectionManager;
    private final QueueDefinition queueDef;

    MQOperationsImpl(MQConnectionManager connectionManager, QueueDefinition queueDef) {
        this.connectionManager = connectionManager;
        this.queueDef = queueDef;
    }

    @Override
    public String requestResponse(String message) throws Exception {
        return requestResponse(message, DEFAULT_TIMEOUT);
    }

    @Override
    public String requestResponse(String message, Duration timeout) throws Exception {
        return this.requestResponse((MQMessage mqMsg) -> {
            try {
                this.prepareDefaultMessage(mqMsg, message, timeout);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public String requestResponse(Consumer<MQMessage> setupper) throws Exception {
        MQConnectionTriple triple = null;
        try {
            triple = connectionManager.borrowConnection(queueDef);
            MQMessage sendMessage = new MQMessage();
            setupper.accept(sendMessage);
            sendRequest(triple, sendMessage);
            String response = receiveResponse(triple, sendMessage);
            connectionManager.returnConnection(queueDef, triple);
            return response;
        } catch (Exception e) {
            if (triple != null) {
                connectionManager.invalidateConnection(queueDef, triple);
            }
            logger.error("Error in requestResponse operation", e);
            throw e;
        }
    }

    private void prepareDefaultMessage(MQMessage sendMessage, String message, Duration timeout) throws Exception {
        sendMessage.format = "MQSTR   ";
        sendMessage.characterSet = 37;
        sendMessage.expiry = (int) timeout.getSeconds()*10;
        sendMessage.correlationId = "AMQ!NEW_SESSION_CORRELID".getBytes();
        sendMessage.messageType = 1;
        sendMessage.replyToQueueName = queueDef.getResponseQueue();
        sendMessage.writeString(message);
    }

    private void sendRequest(MQConnectionTriple triple, MQMessage sendMessage) throws MQException {
        try {
            triple.getRequestQueue().put(sendMessage);
        } catch (MQException e) {
            connectionManager.invalidateConnection(queueDef, triple);
            throw e;
        }
    }

    private String receiveResponse(MQConnectionTriple triple, MQMessage sendMessage) throws Exception {
        MQMessage replyMessage = new MQMessage();
        replyMessage.correlationId = sendMessage.messageId;
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        gmo.options = CMQC.MQGMO_WAIT;
        if (sendMessage.expiry != -1) {
            // expiry is in tenths of a second and waitInterval in ms
            gmo.waitInterval = sendMessage.expiry * 100;
        }
        try {
            triple.getResponseQueue().get(replyMessage, gmo);
            return replyMessage.readStringOfCharLength(replyMessage.getMessageLength());
        } catch (MQException e) {
            connectionManager.invalidateConnection(queueDef, triple);
            throw e;
        }
    }
}