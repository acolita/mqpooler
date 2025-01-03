package br.com.acolita.mqpooler;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MQConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(MQConnectionManager.class);
    private final Map<QueueDefinition, GenericObjectPool<MQConnectionTriple>> pools = new ConcurrentHashMap<>();

    private GenericObjectPool<MQConnectionTriple> getOrCreatePool(QueueDefinition queueDef) {
        return pools.computeIfAbsent(queueDef, key -> {
            GenericObjectPoolConfig<MQConnectionTriple> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(20);
            config.setBlockWhenExhausted(false); // Don't block when pool is exhausted
            config.setTestOnBorrow(true);
            config.setTestOnReturn(true);
            
            return new GenericObjectPool<>(new MQConnectionFactory(key), config);
        });
    }

    public MQOperations forQueues(QueueDefinition queueDef) {
        return new MQOperationsImpl(this, queueDef);
    }

    MQConnectionTriple borrowConnection(QueueDefinition queueDef) throws Exception {
        GenericObjectPool<MQConnectionTriple> pool = getOrCreatePool(queueDef);
        
        try {
            MQConnectionTriple triple = pool.borrowObject();
            if (triple != null) {
                return triple;
            }
        } catch (Exception e) {
            logger.warn("Failed to borrow connection from pool, creating new instance", e);
        }
        
        // If pool is exhausted or borrowing fails, create new instance
        return new MQConnectionFactory(queueDef).create();
    }

    void returnConnection(QueueDefinition queueDef, MQConnectionTriple triple) {
        if (!triple.isValid()) {
            triple.close();
            return;
        }

        GenericObjectPool<MQConnectionTriple> pool = pools.get(queueDef);
        if (pool != null) {
            try {
                pool.returnObject(triple);
            } catch (Exception e) {
                logger.error("Error returning connection to pool", e);
                triple.close();
            }
        } else {
            triple.close();
        }
    }

    void invalidateConnection(QueueDefinition queueDef, MQConnectionTriple triple) {
        triple.invalidate();
        GenericObjectPool<MQConnectionTriple> pool = pools.get(queueDef);
        if (pool != null) {
            try {
                pool.invalidateObject(triple);
            } catch (Exception e) {
                logger.error("Error invalidating connection", e);
            }
        }
        triple.close();
    }

    @PreDestroy
    public void shutdown() {
        for (GenericObjectPool<MQConnectionTriple> pool : pools.values()) {
            pool.close();
        }
        pools.clear();
    }
}

