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

class LedgerInterpreter extends CmdInterpreter<LedgerCmd> {
    final static int GROUP_LEDGER = 1;

    @Override
    public int group() {
        return GROUP_LEDGER;
    }

    @Override
    public boolean canProcess(Cmd command) {
        return command instanceof LedgerCmd;
    }

    @Override
    public LedgerCmd execute(@NotNull LedgerCmd cmd, @NotNull CmdChannel channel) {
        switch (cmd) {
            case CmdBookTransaction bookTransaction -> System.out.println(CmdBookTransaction.NAME);
            case CmdBookSplitTransaction cmdBookSplitTransaction -> System.out.println(CmdBookSplitTransaction.NAME);
            case CmdGetAccountBalance cmdGetAccountBalance -> System.out.println(CmdGetAccountBalance.NAME);
        }
        return cmd;
    }
}