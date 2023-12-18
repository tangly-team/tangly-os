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

import org.jetbrains.annotations.NotNull;

/**
 * The interpreter decides if the request is synchronous or asynchronous. If the system follows the CQRS approach, commands should be asynchronous and queries can be synchronous
 * because processing time shall be negligible. Naturally, you still can implement a synchronous or asynchronous approach for all requests. Beware of the consequences on the
 * real-time behavior and response times.
 * <p>The command interpreter defines the boundary of bounded domain. The interpreter owns the commands and the functionality to transform Java objets into external
 * representations.</p>
 *
 * @param <T> type of commands which can be processed
 */
public abstract class CmdInterpreter<T extends Cmd> {

    public abstract boolean canProcess(String group, String cmdName);

    boolean canProcess(@NotNull Cmd cmd) {
        return canProcess(cmd.group(), cmd.name());
    }

    /**
     * THe method can implement at least three modes:
     * <ul>
     *     <li>The call is synchronous and blocking. If the command is an action it can return either upon execution or upon queuing of the request.
     *     A query will blocked until the results are available and returned.</li>
     *     <li>The call is asynchronous and non-blocking. A future is set in the returned command with the result of the query.</li>
     *     <li>The call is asynchronous and reactive. A completable future is set using the channel instance and the transmit method.</li>
     * </ul>
     *
     * @param cmd     command to be processed
     * @param channel requesting the command processing
     * @return result of the command if it is a command, otherwise null
     */
    public abstract T execute(@NotNull T cmd, CmdChannel channel);
}
