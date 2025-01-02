package br.com.acolita.mqpooler;

import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

class MQConnectionFactory extends BasePooledObjectFactory<MQConnectionTriple> {
    private final QueueDefinition queueDef;

    public MQConnectionFactory(QueueDefinition queueDef) {
        this.queueDef = queueDef;
    }

    @Override
    public MQConnectionTriple create() throws Exception {
        MQQueueManager queueManager = new MQQueueManager("QM1");
        
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