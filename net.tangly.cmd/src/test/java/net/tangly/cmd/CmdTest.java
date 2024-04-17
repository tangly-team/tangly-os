/*
 * Copyright 2023-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.cmd;

import org.junit.jupiter.api.Test;

class CmdTest {
    static final String BOOK_TRANSACTION = "book-transaction -from source -to target -amount 1200.05 -date 2020-01-01 -text \"book transaction 42\"";

    @Test
    void testLedgerChannelText() {
        LedgerInterpreter interpreter = new LedgerInterpreter();
        LedgerChannelText channel = new LedgerChannelText(interpreter);

        CmdDispatcher dispatcher = new CmdDispatcher();
        dispatcher.register(interpreter);
        dispatcher.register(channel);

        String[] line = {"help"};
        channel.process(line);
        channel.process(BOOK_TRANSACTION.split(" "));
    }
}
