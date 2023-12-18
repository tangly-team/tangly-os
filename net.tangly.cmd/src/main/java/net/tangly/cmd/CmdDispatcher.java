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

import java.util.HashSet;
import java.util.Set;

public class CmdDispatcher {
    private final Set<CmdInterpreter<?>> interpreters;
    private final Set<CmdChannel> channels;

    public CmdDispatcher() {
        interpreters = new HashSet<>();
        channels = new HashSet<>();
    }

    public void register(@NotNull CmdInterpreter<?> interpreter) {
        interpreters.add(interpreter);
    }

    public void register(@NotNull CmdChannel channel) {
        channels.add(channel);
    }

    // TODO should return a completable future? CompletableFuture.supplyAsync(execute).thenAccept(send answer)
    public <T extends Cmd> void execute(Cmd command, CmdChannel channel) {
        interpreters.stream().filter(o -> o.canProcess(command)).findAny().ifPresent(o -> ((CmdInterpreter<T>) o).execute((T) command, channel));
    }
}
