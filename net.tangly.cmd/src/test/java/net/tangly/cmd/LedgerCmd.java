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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public sealed interface LedgerCmd extends Cmd permits CmdBookTransaction, CmdBookSplitTransaction, CmdGetAccountBalance {
}

record CmdBookTransaction(String name, boolean hasAnswer, int id, String accountIdFrom, String accountIdTo, LocalDate date, BigDecimal amount, String text) implements LedgerCmd {
    static String NAME = "book-transaction";
    static boolean HAS_ANSWER = false;

    CmdBookTransaction(int id, String accountIdFrom, String accountIdTo, LocalDate date, BigDecimal amount, String text) {
        this(NAME, HAS_ANSWER, id, accountIdFrom, accountIdTo, date, amount, text);
    }
}

record Booking(String accountId, BigDecimal amount, String text) {
}

record CmdBookSplitTransaction(String name, boolean hasAnswer, int id, List<Booking> from, List<Booking> to, LocalDate date) implements LedgerCmd {
    static String NAME = "book-split-transaction";
    static boolean HAS_ANSWER = false;

    CmdBookSplitTransaction(int id, List<Booking> from, List<Booking> to, LocalDate date) {
        this(NAME, HAS_ANSWER, id, from, to, date);
    }
}

record AccountBalance(String accountId, LocalDate date, BigDecimal amount) {
}

record CmdGetAccountBalance(String name, boolean hasAnswer, int id, String accountId, LocalDate date) implements LedgerCmd {
    static String NAME = "get-account-balance";
    static boolean HAS_ANSWER = true;

    CmdGetAccountBalance(int id, String accountId, LocalDate date) {
        this(NAME, HAS_ANSWER, id, accountId, date);
    }
}


