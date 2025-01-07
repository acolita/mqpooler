package br.com.acolita.mqpooler;

import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.Hashtable;

class MQConnectionFactory extends BasePooledObjectFactory<MQConnectionTriple> {
    private final QueueDefinition queueDef;
    private MQConnectionConfig MQConnectionConfig;

    public MQConnectionFactory(QueueDefinition queueDef, MQConnectionConfig MQConnectionConfig) {
        this.queueDef = queueDef;
        this.MQConnectionConfig = MQConnectionConfig;
    }

    @Override
    public MQConnectionTriple create() throws Exception {
        final Hashtable<String, Object> properties = MQConnectionConfig.getPropertiesTable();
        MQQueueManager queueManager = new MQQueueManager(MQConnectionConfig.getMessageQueueName(), properties);
        
        MQQueue requestQueue = queueManager.accessQueue(
            queueDef.getRequestQueue(),
                CMQC.MQOO_OUTPUT
        );
        
        MQQueue responseQueue = queueManager.accessQueue(
            queueDef.getResponseQueue(),
                CMQC.MQOO_INPUT_AS_Q_DEF
        );

        return new MQConnectionTriple(queueManager, requestQueue, responseQueue);
    }

    @Override
    public PooledObject<MQConnectionTriple> wrap(MQConnectionTriple triple) {
        return new DefaultPooledObject<>(triple);
    }

    @Override
    public boolean validateObject(PooledObject<MQConnectionTriple> pooledObject) {
        return pooledObject.getObject().isValid();
    }

    @Override
    public void destroyObject(PooledObject<MQConnectionTriple> pooledObject) {
        pooledObject.getObject().close();
    }
}