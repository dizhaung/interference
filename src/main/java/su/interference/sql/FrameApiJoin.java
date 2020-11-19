/**
 The MIT License (MIT)

 Copyright (c) 2010-2019 head systems, ltd

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 the Software, and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package su.interference.sql;

import su.interference.core.Config;
import su.interference.metrics.Metrics;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * @author Yuriy Glotanov
 * @since 1.0
 */

public class FrameApiJoin implements Serializable, Callable<FrameApiJoin> {
    private final int nodeId;
    private final transient FrameApi bd1;
    private final transient FrameApi bd2;
    private final transient FrameJoinTask frameJoinTask;
    private final long leftAllocId;
    private final long rightAllocId;
    private final transient CountDownLatch latch = new CountDownLatch(1);
    private List<Object> result;
    private boolean failed;

    public FrameApiJoin(int nodeId, SQLCursor cur, FrameApi bd1, FrameApi bd2) {
        this.nodeId = nodeId;
        this.bd1 = bd1;
        this.bd2 = bd2;
        this.leftAllocId = bd1.getAllocId();
        this.rightAllocId = bd2 == null ? 0 : bd2 instanceof SQLIndexFrame ? 0 : bd2.getAllocId();
        if (nodeId == Config.getConfig().LOCAL_NODE_ID) {
            frameJoinTask = cur.buildFrameJoinTask(nodeId, bd1, bd2);
        } else {
            frameJoinTask = null;
        }
    }

    public FrameApiJoin call() throws Exception {
        if (nodeId == Config.getConfig().LOCAL_NODE_ID) {
            Metrics.get("localTask").start();
            result = frameJoinTask.call();
            Metrics.get("localTask").stop();
        } else {
            Metrics.get("remoteTask").start();
            latch.await();
            Metrics.get("remoteTask").stop();
        }
        return this;
    }

    protected String getKey() {
        return this.leftAllocId + "-" + this.rightAllocId;
    }

    @Override
    public boolean equals(Object obj) {
        return this.getKey().equals(((FrameApiJoin)obj).getKey());
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }

    public int getNodeId() {
        return nodeId;
    }

    public long getLeftAllocId() {
        return leftAllocId;
    }

    public long getRightAllocId() {
        return rightAllocId;
    }

    public FrameApi getBd1() {
        return bd1;
    }

    public FrameApi getBd2() {
        return bd2;
    }

    public List<Object> getResult() {
        return result;
    }

    public void setResult(List<Object> result) {
        this.result = result;
    }

    public void setResultWithCountDown(List<Object> result) {
        this.result = result;
        latch.countDown();
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
        latch.countDown();
    }
}
