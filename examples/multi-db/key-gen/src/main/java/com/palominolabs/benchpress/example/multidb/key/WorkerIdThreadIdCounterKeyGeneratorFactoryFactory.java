package com.palominolabs.benchpress.example.multidb.key;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.configuration.Configuration;

import java.nio.CharBuffer;
import java.util.UUID;

@ThreadSafe
final class WorkerIdThreadIdCounterKeyGeneratorFactoryFactory implements KeyGeneratorFactoryFactory {

    private static final Factory FACTORY = new Factory();

    @Override
    public KeyGeneratorFactory getKeyGeneratorFactory(Configuration c) {
        return FACTORY;
    }

    @Nonnull
    @Override
    public String getRegistryId() {
        return "WORKER_ID_THREAD_ID_COUNTER";
    }

    @ThreadSafe
    private static class Factory implements KeyGeneratorFactory {
        private static KeyGenerator GENERATOR = new Generator();

        @Override
        public KeyGenerator getKeyGenerator() {
            return GENERATOR;
        }
    }

    @ThreadSafe
    private static class Generator implements KeyGenerator {
        @Override
        public void writeKey(CharBuffer buf, UUID workerId, long threadId, int sliceId, int counter) {
            buf.append(workerId.toString());
            buf.append('|').append(Long.toString(threadId)).append('|').append(Integer.toString(sliceId))
                .append('|')
                .append(Integer.toString(counter));
        }
    }
}
