package br.com.acolita.mqpooler;

import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Hashtable;

@Configuration
class MQConnectionConfig {
    @Value("${acolita.mqpooler.queue-manager-name}")
    private String messageQueueName;

    @Value("${acolita.mqpooler.properties.host-name}")
    private String hostname;
    @Value("${acolita.mqpooler.properties.port}")
    private int port;
    @Value("${acolita.mqpooler.properties.user-id}")
    private String userId;
    @Value("${acolita.mqpooler.properties.password}")
    private String password;
    @Value("${acolita.mqpooler.properties.channel}")
    private String channel;

    Hashtable<String, Object> properties;

    public String getMessageQueueName() {
        return messageQueueName;
    }

    @PostConstruct
    private void setPropertiesTable(){
        properties = new Hashtable<>();
        properties.put(MQC.HOST_NAME_PROPERTY, hostname);
        properties.put(MQC.PORT_PROPERTY, port);
        properties.put(MQC.USER_ID_PROPERTY, userId);
        properties.put(MQC.PASSWORD_PROPERTY, password);
        properties.put(MQC.CHANNEL_PROPERTY, channel);
    }

    public Hashtable<String, Object> getPropertiesTable() {
        return properties;
    }
}
