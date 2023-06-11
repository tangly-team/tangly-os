/*
 * Copyright 2022-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.ledger.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ledger {
    private final List<Account> accounts;
    private final List<Transaction> transactions;

    public Ledger() {
        accounts = new ArrayList<>();
        transactions = new ArrayList<>();
    }

    public List<Transaction> transactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void add(Transaction transaction) {
        transactions.add(transaction);
    }

    public List<Account> accounts() {
        return Collections.unmodifiableList(accounts);
    }

    public void add(Account account) {
        accounts.add(account);
    }
}
