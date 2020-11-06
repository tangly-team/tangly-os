/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.core.app;

import java.util.concurrent.locks.ReentrantLock;

public class IdGenerator {
    private final ReentrantLock lock;
    private long oidValue;

    public IdGenerator(long oidInitialValue) {
        this.oidValue = oidInitialValue;
        lock = new ReentrantLock();
    }

    public long nextOid(Class<?> clazz) {
        lock.lock();
        try {
            oidValue += 1;
            return oidValue;
        } finally {
            lock.unlock();
        }
    }
}
