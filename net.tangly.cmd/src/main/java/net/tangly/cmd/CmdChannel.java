/*
 * Copyright 2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.cmd;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The command channel is an interface class connecting an external format and channel to one or multiple command interpreters. Usually a channel has one interpreter to reflect
 * domain-driven design approaches.
 * <p>A dispatcher should be provided if multiple domains are accessed through the same protocol channel such as a terminal or a CAN bus.</p>
 */
public abstract class CmdChannel {
    public enum ChannelKind {TEXT, BINARY, JSON, PROTOBUF, OBJECT}

    private final AtomicInteger id = new AtomicInteger();

    EnumSet<ChannelKind> supports() {
        return EnumSet.of(ChannelKind.TEXT);
    }

    public abstract boolean canProcess(String command);

    public abstract void transmit(int cmdId, Object payload);

    protected int generatedId() {
        return id.getAndIncrement();
    }
}
