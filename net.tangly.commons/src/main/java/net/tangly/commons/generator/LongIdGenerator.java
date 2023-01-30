/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.generator;

import java.util.concurrent.locks.ReentrantLock;

public class LongIdGenerator implements IdGenerator {
    private final ReentrantLock lock;
    private long id;

    public LongIdGenerator(long initialValue) {
        this.lock = new ReentrantLock();
        this.id = initialValue;
    }

    @Override
    public long id() {
        lock.lock();
        try {
            return id++;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void set(long externalId) {
        lock.lock();
        try {
            id = Math.max(id, externalId);
        } finally {
            lock.unlock();
        }
    }
}
