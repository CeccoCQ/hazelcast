/* 
 * Copyright (c) 2008-2010, Hazel Ltd. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hazelcast.impl;

import com.hazelcast.impl.base.DistributedLock;
import com.hazelcast.impl.concurrentmap.ValueHolder;
import com.hazelcast.nio.Data;

import static com.hazelcast.nio.IOUtil.toObject;

public final class DefaultRecord extends AbstractRecord {

    private volatile Object valueObject = null;
    private volatile Data value;

    public DefaultRecord(CMap cmap, int blockId, Data key, Data value, long ttl, long maxIdleMillis, long id) {
        super(cmap, blockId, key, ttl, maxIdleMillis, id);
        this.value = value;
    }

    public Record copy() {
        Record recordCopy = new DefaultRecord(cmap, blockId, key, value, getRemainingTTL(), getRemainingIdle(), id);
        if (optionalInfo != null) {
            recordCopy.setIndexes(getOptionalInfo().indexes, getOptionalInfo().indexTypes);
            recordCopy.setMultiValues(getOptionalInfo().lsMultiValues);
        }
        if (lock != null) {
            recordCopy.setLock(new DistributedLock(lock));
        }
        recordCopy.setVersion(getVersion());
        return recordCopy;
    }

    public Data getValueData() {
        return value;
    }

    public Object getValue() {
        final Object currentValue = valueObject;
        if (currentValue != null) {
            return currentValue;
        }
        synchronized (DefaultRecord.this) {
            if (valueObject != null) {
                return valueObject;
            }
            Object v = toObject(value);
            if (cmap.cacheValue) {
                valueObject = v;
            }
            return v;
        }
    }

    public Object setValue(Object value) {
        Object oldValue = getValue();
        valueObject = value;
        return oldValue;
    }

    protected void invalidateValueCache() {
        valueObject = null;
    }

    public void setValue(Data value) {
        invalidateValueCache();
        this.value = value;
    }

    public int valueCount() {
        int count = 0;
        if (getValueData() != null) {
            count = 1;
        } else if (getMultiValues() != null) {
            count = getMultiValues().size();
        }
        return count;
    }

    public long getCost() {
        long cost = 0;
        // avoid race condition with local references
        final Data dataValue = getValueData();
        final Data dataKey = getKeyData();
        if (dataValue != null) {
            cost = dataValue.size();
            if (valueObject != null) {
                cost += dataValue.size();
            }
        } else if (getMultiValues() != null && getMultiValues().size() > 0) {
            for (ValueHolder valueHolder : getMultiValues()) {
                if (valueHolder != null) {
                    cost += valueHolder.getData().size();
                }
            }
        }
        return cost + dataKey.size() + 312;
    }

    public boolean hasValueData() {
        return value != null;
    }

    public void invalidate() {
        invalidateValueCache();
        value = null;
    }
}