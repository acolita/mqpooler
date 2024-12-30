package br.com.acolita.mqpooler;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.MQConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;


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
        MQConnectionTriple triple = null;
        try {
            triple = connectionManager.borrowConnection(queueDef);

            MQMessage sendMessage = new MQMessage();
            sendMessage.format = "MQSTR   ";
            sendMessage.characterSet = 37;
            sendMessage.expiry = (int)timeout.getSeconds();
            sendMessage.correlationId = "AMQ!NEW_SESSION_CORRELID".getBytes();
            sendMessage.messageType = 1;
            sendMessage.replyToQueueName = queueDef.getResponseQueue();
            sendMessage.writeString(message);

            try {
                triple.getRequestQueue().put(sendMessage);
            } catch (MQException e) {
                connectionManager.invalidateConnection(queueDef, triple);
                throw e;
            }

            MQMessage replyMessage = new MQMessage();
            replyMessage.correlationId = sendMessage.messageId;

            MQGetMessageOptions gmo = new MQGetMessageOptions();
            gmo.options = MQConstants.MQGMO_WAIT;
            gmo.waitInterval = (int)timeout.toMillis();

            try {
                triple.getResponseQueue().get(replyMessage, gmo);
                String response = replyMessage.readStringOfCharLength(replyMessage.getMessageLength());
                connectionManager.returnConnection(queueDef, triple);
                return response;
            } catch (MQException e) {
                connectionManager.invalidateConnection(queueDef, triple);
                throw e;
            }

        } catch (Exception e) {
            if (triple != null) {
                connectionManager.invalidateConnection(queueDef, triple);
            }
            logger.error("Error in requestResponse operation", e);
            throw e;
        }
    }
}