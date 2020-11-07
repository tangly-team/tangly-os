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

package net.tangly.commons.generator;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class SnowflakeIdGenerator implements IdGenerator {
    private final ReentrantLock lock;
    private final long datacenterIdBits = 10L;
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    private final long sequenceBits = 12L;

    private final long datacenterIdShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + datacenterIdBits;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private final long twepoch = 1288834974657L;
    private long datacenterId;

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;


    public SnowflakeIdGenerator(long datacenterId) {
        lock = new ReentrantLock();
        if (datacenterId == 0) {
            try {
                this.datacenterId = getDatacenterId();
            } catch (SocketException | UnknownHostException | NullPointerException e) {
                Random rnd = new Random();
                this.datacenterId = rnd.nextInt((int) maxDatacenterId) + 1;
            }
        } else {
            this.datacenterId = datacenterId;
        }
        if (this.datacenterId > maxDatacenterId || datacenterId < 0) {
            Random rnd = new Random();
            this.datacenterId = rnd.nextInt((int) maxDatacenterId) + 1;
        }
    }

    @Override
    public long id() {
        lock.lock();
        try {
            long timestamp = System.currentTimeMillis();
            if (timestamp < lastTimestamp) {
                try {
                    Thread.sleep((lastTimestamp - timestamp));
                } catch (InterruptedException e) {
                }
            }
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0;
            }
            lastTimestamp = timestamp;
            long id = ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | sequence;
            return id;
        } finally {
            lock.unlock();
        }
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    protected long getDatacenterId() throws SocketException, UnknownHostException {
        NetworkInterface network = null;
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface nint = en.nextElement();
            if (!nint.isLoopback() && nint.getHardwareAddress() != null) {
                network = nint;
                break;
            }
        }
        byte[] mac = network.getHardwareAddress();
        Random rnd = new Random();
        byte rndByte = (byte) (rnd.nextInt() & 0x000000FF);
        return ((0x000000FF & (long) mac[mac.length - 1]) | (0x0000FF00 & (((long) rndByte) << 8))) >> 6;
    }
}
