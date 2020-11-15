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

package net.tangly.bus.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * A simple transaction is a money transfer between a debit and a credit accounts. A split transaction is a money transfer between a debit account and a set of
 * credit accounts or a credit account and a set of debit accounts. The total debit and the total credit of a transaction must be the same format. The majority
 * of transactions have one debit and one credit account. The class is immutable.
 */
public class Transaction {
    private final LocalDate date;
    private final AccountEntry debit;
    private final AccountEntry credit;
    private final List<AccountEntry> splits;
    private final String reference;
    private final String text;

    public Transaction(LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, String text, String reference) {
        this.date = date;
        this.text = text;
        this.reference = reference;
        this.debit = new AccountEntry(debitAccount, date, amount, reference, true);
        this.credit = new AccountEntry(creditAccount, date, amount, reference, false);
        this.splits = Collections.emptyList();
    }

    public Transaction(LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, List<AccountEntry> splits, String text, String reference) {
        this.date = date;
        this.text = text;
        this.reference = reference;
        this.debit = (debitAccount != null) ? new AccountEntry(debitAccount, date, amount, reference, true) : null;
        this.credit = (creditAccount != null) ? new AccountEntry(creditAccount, date, amount, reference, false) : null;
        this.splits = List.copyOf(splits);
    }

    public static Transaction of(String date, int debitAccount, int creditAccount, String amount, String text) {
        return new Transaction(LocalDate.parse(date), String.valueOf(debitAccount), String.valueOf(creditAccount), new BigDecimal(amount), text, null);
    }

    public static Transaction of(String date, int debitAccount, int creditAccount, String amount, String text, String reference) {
        return new Transaction(LocalDate.parse(date), String.valueOf(debitAccount), String.valueOf(creditAccount), new BigDecimal(amount), text, reference);
    }

    public List<AccountEntry> debitSplits() {
        return (debit != null) ? List.of(debit) : splits;
    }

    public List<AccountEntry> creditSplits() {
        return (credit != null) ? List.of(credit) : splits;
    }

    /**
     * Returns an optional reference identifier to an external accounting document describing the transaction.
     *
     * @return the reference identifier, can be null
     */
    public String reference() {
        return reference;
    }

    /**
     * Returns the credit (Haben) account of the transaction.
     *
     * @return credit account of the transaction
     */
    public String creditAccount() {
        return (credit != null) ? credit.accountId() : null;
    }

    /**
     * Returns the debit (Soll) account of the transaction.
     *
     * @return debit account of the transaction
     */
    public String debitAccount() {
        return (debit != null) ? debit.accountId() : null;
    }

    /**
     * Returns the amount of the transaction.
     *
     * @return amount of the transaction
     */
    public BigDecimal amount() {
        return (debit != null) ? debit.amount() : credit.amount();
    }

    /**
     * Returns a human-readable description of the transaction.
     *
     * @return description of the transaction
     */
    public String text() {
        return text;
    }

    /**
     * Returns the date of the execution of the transaction.
     *
     * @return date of the transaction
     */
    public LocalDate date() {
        return date;
    }

    /**
     * Returns true if the transaction is split between multiple accounts.
     *
     * @return true if the transaction is split
     */
    public boolean isSplit() {
        return (debit == null) || (credit == null);
    }

    @Override
    public String toString() {
        return """
            Transaction[date=%s, debit=%s, credit=%s, splits=%s, reference, text]
            """.formatted(date(), debitAccount(), creditAccount(), splits, reference(), text());
    }
}

