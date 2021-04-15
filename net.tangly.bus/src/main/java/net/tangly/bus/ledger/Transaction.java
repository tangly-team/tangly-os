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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.tangly.core.HasDate;

/**
 * A simple transaction is a money transfer between a debit and a credit accounts. A split transaction is a money transfer between a debit account and a set of
 * credit accounts or a credit account and a set of debit accounts. The total debit and the total credit of a transaction must be the same format. The majority
 * of transactions have one debit and one credit account. The class is immutable.
 */
public class Transaction implements HasDate {
    public static class Builder {
        private LocalDate date;
        private String reference;
        private String text;
        private List<AccountEntry> splits;

        Builder() {
            splits = new ArrayList<>();
        }

        Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        Builder reference(String reference) {
            this.reference = reference;
            return this;
        }

        Builder text(String text) {
            this.text = text;
            return this;
        }

        Builder debit(String account, BigDecimal amount) {
            splits.add(new AccountEntry(account, date, amount, reference, true));
            return this;
        }

        Builder credit(String account, BigDecimal amount) {
            splits.add(new AccountEntry(account, date, amount, reference, false));
            return this;
        }

        Transaction build() {
            List<AccountEntry> debits = splits.stream().filter(AccountEntry::isDebit).toList();
            List<AccountEntry> credits = splits.stream().filter(AccountEntry::isCredit).toList();
            BigDecimal amount = (debits.size() == 1) ? debits.get(0).amount() : ((credits.size() == 1) ? credits.get(0).amount() : BigDecimal.ZERO);
            return new Transaction(date, text, reference, amount, (debits.size() == 1) ? debits.get(0) : null, (credits.size() == 1) ? credits.get(0) : null,
                (debits.size() > 1) ? debits : ((credits.size() > 1) ? credits : null));
        }
    }

    private final LocalDate date;
    private final String reference;
    private final String text;
    private final BigDecimal amount;
    private final AccountEntry debit;
    private final AccountEntry credit;
    private final List<AccountEntry> splits;

    public Transaction(LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, String text, String reference) {
        this(date, text, reference, amount, new AccountEntry(debitAccount, date, amount, reference, true),
            new AccountEntry(creditAccount, date, amount, reference, false), null);
    }

    public Transaction(LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, List<AccountEntry> splits, String text, String reference) {
        this(date, text, reference, amount, (debitAccount != null) ? new AccountEntry(debitAccount, date, amount, reference, true) : null,
            (creditAccount != null) ? new AccountEntry(creditAccount, date, amount, reference, false) : null, splits);
    }

    protected Transaction(LocalDate date, String text, String reference, BigDecimal amount, AccountEntry debit, AccountEntry credit,
                          List<AccountEntry> splits) {
        this.date = date;
        this.text = text;
        this.reference = reference;
        this.amount = amount;
        this.debit = debit;
        this.credit = credit;
        this.splits = (splits != null) ? List.copyOf(splits) : Collections.emptyList();
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

    public AccountEntry debit() {
        return debit;
    }

    public AccountEntry credit() {
        return credit;
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
            Transaction[date=%s, debit=%s, credit=%s, splits=%s, reference=%s, text=%s]
            """.formatted(date(), debitAccount(), creditAccount(), splits, reference(), text());
    }
}
