package com.palominolabs.benchpress.example.multidb.hbaseasync;

import com.google.common.collect.Lists;
import com.palominolabs.benchpress.example.multidb.key.KeyGeneratorFactory;
import com.palominolabs.benchpress.example.multidb.task.TaskFactoryBase;
import com.palominolabs.benchpress.example.multidb.value.ValueGeneratorFactory;
import com.palominolabs.benchpress.job.task.TaskFactory;
import com.palominolabs.benchpress.job.task.TaskOperation;
import com.palominolabs.benchpress.task.reporting.ScopedProgressClient;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.hbase.async.HBaseClient;
import org.hbase.async.TableNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class HbaseAsyncTaskFactory extends TaskFactoryBase implements TaskFactory {

    private static final Logger logger = LoggerFactory.getLogger(HbaseAsyncTaskFactory.class);

    private final String zkQuorum;
    private final String columnFamily;
    private final String table;
    private final String qualifier;
    private HBaseClient client;

    HbaseAsyncTaskFactory(TaskOperation taskOperation, ValueGeneratorFactory valueGeneratorFactory, int batchSize,
                          KeyGeneratorFactory keyGeneratorFactory, int numQuanta, int numThreads,
                          String columnFamily, String zkQuorum, String table, String qualifier) {
        super(taskOperation, valueGeneratorFactory, batchSize, keyGeneratorFactory, numQuanta, numThreads);
        this.columnFamily = columnFamily;
        this.zkQuorum = zkQuorum;
        this.table = table;
        this.qualifier = qualifier;
    }

    @Nonnull
    @Override
    public Collection<Runnable> getRunnables(@Nonnull UUID jobId, int sliceId, @Nonnull UUID workerId,
            @Nonnull ScopedProgressClient progressClient) throws IOException {
        List<Runnable> runnables = Lists.newArrayList();

        client = new HBaseClient(zkQuorum);

        try {
            client.ensureTableExists(table).joinUninterruptibly();
        } catch (TableNotFoundException e) {
            logger.warn("Table " + table + " doesn't exist", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.warn("Couldn't check if table exists", e);
            throw new RuntimeException(e);
        }

        int quantaPerThread = numQuanta / numThreads;

        for (int i = 0; i < numThreads; i++) {
            runnables.add(new HbaseAsyncRunnable(taskOperation, client, quantaPerThread, table, columnFamily,
                    qualifier, keyGeneratorFactory.getKeyGenerator(), valueGeneratorFactory.getValueGenerator(),
                    workerId, sliceId, batchSize, jobId));
        }
        return runnables;
    }

    @Override
    public void shutdown() {
        client.shutdown();
    }
}
