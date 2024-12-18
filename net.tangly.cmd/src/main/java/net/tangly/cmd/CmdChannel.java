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

/**
 * The command channel is an interface class connecting an external format and channel to one or multiple command interpreters. A channel supports one protocol format and one and
 * receives commands for one or multiple interpreters.
 * <p>A dispatcher should be provided if multiple domains are accessed through the same protocol channel such as a terminal or a CAN bus.</p>
 */
public interface CmdChannel {
    enum ChannelKind {TEXT, BINARY, JSON, PROTOBUF, OBJECT}

    default ChannelKind supports() {
        return ChannelKind.TEXT;
    }

    void transmit(String group, String command, Object payload);
}
