package com.palominolabs.benchpress.task.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import javax.annotation.concurrent.ThreadSafe;
import java.util.UUID;

// TODO this should be more flexible for reporting task-impl-specific types of progress (e.g. latency)
@ThreadSafe
public interface TaskProgressClient {
    /**
     * Indicates that all threads for the task have completed
     */
    void reportFinished(UUID jobId, int sliceId, Duration duration, String url);

    void reportProgress(UUID jobId, int sliceId, JsonNode data, String url);
}
