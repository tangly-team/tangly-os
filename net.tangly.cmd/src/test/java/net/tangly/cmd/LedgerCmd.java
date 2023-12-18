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

public sealed interface LedgerCmd extends Cmd permits CmdBookTransaction, CmdBookSplitTransaction, CmdGetAccountBalance, AnswerGetAccountBalance {
    String GROUP = "ledger";

    default String group() {
        return GROUP;
    }

}

record CmdBookTransaction(String name, boolean hasAnswer, String accountIdFrom, String accountIdTo, LocalDate date, BigDecimal amount, String text) implements LedgerCmd {
    static String NAME = "book-transaction";
    static boolean HAS_ANSWER = false;

    CmdBookTransaction(String accountIdFrom, String accountIdTo, LocalDate date, BigDecimal amount, String text) {
        this(NAME, HAS_ANSWER, accountIdFrom, accountIdTo, date, amount, text);
    }
}

record Booking(String accountId, BigDecimal amount, String text) {
}

record CmdBookSplitTransaction(String name, boolean hasAnswer, List<Booking> from, List<Booking> to, LocalDate date) implements LedgerCmd {
    static String NAME = "book-split-transaction";
    static boolean HAS_ANSWER = false;

    CmdBookSplitTransaction(List<Booking> from, List<Booking> to, LocalDate date) {
        this(NAME, HAS_ANSWER, from, to, date);
    }
}

record CmdGetAccountBalance(String name, boolean hasAnswer, String accountId, LocalDate date) implements LedgerCmd {
    static String NAME = "get-account-balance";
    static boolean HAS_ANSWER = true;

    CmdGetAccountBalance(String accountId, LocalDate date) {
        this(NAME, HAS_ANSWER, accountId, date);
    }
}

record AnswerGetAccountBalance(String name, boolean hasAnswer, String accountId, LocalDate date, BigDecimal amount) implements LedgerCmd {
    static String NAME = "account-balance";
    static boolean HAS_ANSWER = false;

    AnswerGetAccountBalance(String accountId, LocalDate date, BigDecimal amount) {
        this(NAME, HAS_ANSWER, accountId, date, amount);
    }
}


