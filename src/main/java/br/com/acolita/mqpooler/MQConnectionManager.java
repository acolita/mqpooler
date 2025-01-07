package br.com.acolita.mqpooler;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages pooled connections to IBM MQ, enabling efficient resource utilization
 * through connection pooling. Integrates with Apache Commons Pool to manage
 * connection lifecycle and provides methods for borrowing, returning, and
 * invalidating connections.
 */
@Component
public class MQConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(MQConnectionManager.class);
    private final Map<QueueDefinition, GenericObjectPool<MQConnectionTriple>> pools = new ConcurrentHashMap<>();

    @Value("${acolita.mqpooler.max-pool-size}")
    private int maxPoolSize;

    @Autowired
    MQConnectionConfig MQConnectionConfig;

    /**
     * Retrieves or creates a new object pool for a specific queue definition.
     * This method configures the pool with the predefined settings, including
     * the maximum number of objects, and validation strategies.
     *
     * @param queueDef The queue definition identifying the pool.
     * @return A configured GenericObjectPool for handling MQ connections.
     */
    private GenericObjectPool<MQConnectionTriple> getOrCreatePool(QueueDefinition queueDef) {
        return pools.computeIfAbsent(queueDef, key -> {
            System.out.println("MAX POOL SIZEEEEEE IN LIB: " + maxPoolSize);
            GenericObjectPoolConfig<MQConnectionTriple> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(maxPoolSize);
            config.setBlockWhenExhausted(false); // Don't block when pool is exhausted
            config.setTestOnBorrow(true);
            config.setTestOnReturn(true);

            return new GenericObjectPool<>(new MQConnectionFactory(key, MQConnectionConfig), config);
        });
    }

    /**
     * Provides an MQOperations instance configured for the specified queue
     * definition. This enables sending and receiving operations with IBM MQ.
     *
     * @param queueDef The queue definition for which operations are created.
     * @return An instance of MQOperations linked to the specified queues.
     */
    public MQOperations forQueues(QueueDefinition queueDef) {
        return new MQOperationsImpl(this, queueDef);
    }

    /**
     * Borrows a connection for the specified queue definition from the pool,
     * or creates a new instance if the borrowing fails. This connection is then
     * used for message operations with the MQ.
     *
     * @param queueDef The queue definition for which the connection is borrowed.
     * @return An MQConnectionTriple instance representing the connection.
     * @throws Exception If an error occurs during borrowing or connection creation.
     */
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
        return new MQConnectionFactory(queueDef, MQConnectionConfig).create();
    }

    /**
     * Returns a connection to the pool if it is valid; otherwise, closes it.
     * Ensures that the pooled connection remains in a usable state.
     *
     * @param queueDef The queue definition associated with the connection.
     * @param triple The MQConnectionTriple instance to return to the pool.
     */
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

    /**
     * Invalidates a connection, removing it from the pool and ensuring it is
     * properly closed. Used for handling erroneous or unusable connections.
     *
     * @param queueDef The queue definition associated with the connection.
     * @param triple The MQConnectionTriple instance to invalidate.
     */
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

    /**
     * Shuts down the connection manager, closing all active pools and clearing
     * resources. This method is invoked when the component is being destroyed,
     * ensuring that all resources are properly released.
     */
    @PreDestroy
    public void shutdown() {
        for (GenericObjectPool<MQConnectionTriple> pool : pools.values()) {
            pool.close();
        }
        pools.clear();
    }
}