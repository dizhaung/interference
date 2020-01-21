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

import su.interference.core.Chunk;
import su.interference.core.ChunkIdComparator;
import su.interference.exception.InternalException;
import su.interference.persistent.Session;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Yuriy Glotanov
 * @since 1.0
 */

public class ContainerFrame implements FrameApi {
    private final int objectId;
    private List<FrameApi> frames;

    public ContainerFrame(int objectId, List<FrameApi> frames) {
        this.objectId = objectId;
        this.frames = frames;
    }

    public long getFrameId() {
        return 0;
    }

    public long getAllocId() {
        return 0;
    }

    public int getObjectId() {
        return objectId;
    }

    public int getImpl() {
        return FrameApi.IMPL_CONTAINER;
    }

    public ArrayList<Chunk> getFrameChunks(Session s) throws IOException, ClassNotFoundException, InternalException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        return null;
    }

    public ArrayList<Object> getFrameEntities(Session s) throws IOException, ClassNotFoundException, InternalException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        if (s.isStream()) {
            final List<Chunk> chunks = new ArrayList<>();
            final ArrayList<Object> res = new ArrayList<>();
            for (FrameApi f : frames) {
                if (f.getImpl() == FrameApi.IMPL_DATA) {
                    chunks.addAll(f.getFrameChunks(s));
                }
            }
            Collections.sort(chunks, new ChunkIdComparator(s));
            for (Chunk c : chunks) {
                res.add(c.getEntity());
            }
            return res;
        }
        return null;
    }

    public boolean hasLiveTransaction(long transId) {
        return false;
    }

    public boolean hasLocalTransactions() {
        return false;
    }

    public int hasRemoteTransactions() throws InternalException {
        return 0;
    }

}
