/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.erp.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A transaction is a money transfer between a set of debit and a set of credit accounts, the left ones being the debit account,
 * the right ones being the credit account. The majority of transactions have one debit and one credit account. The class is
 * immutable.
 */
public class Transaction {
    public static Transaction of(String date, int debitAccount, int creditAccount, String amount, String description) {
        return new Transaction(LocalDate.parse(date), String.valueOf(debitAccount), String.valueOf(creditAccount), new BigDecimal(amount),
                description, null, null);
    }

    public static Transaction of(String date, int debitAccount, int creditAccount, String amount, String text, String reference, String vatCode) {
        return new Transaction(LocalDate.parse(date), String.valueOf(debitAccount), String.valueOf(creditAccount), new BigDecimal(amount), text,
                reference, vatCode);
    }

    public static Transaction of(String date, int debitAccount, int creditAccount, String amount, String text, String reference) {
        return new Transaction(LocalDate.parse(date), String.valueOf(debitAccount), String.valueOf(creditAccount), new BigDecimal(amount), text,
                reference, null);
    }

    public static Transaction of(String date, List<Integer> splitDebitAccounts, List<String> splitDebitAmounts, List<Integer> splitCreditAccounts, List<String> splitCreditAmounts, String text, String reference) {
        LocalDate localDate = LocalDate.parse(date);
        return new Transaction(localDate, of(splitDebitAccounts, splitDebitAmounts, localDate, reference, true),
                of(splitCreditAccounts, splitDebitAmounts, localDate, reference, true), text, reference);
    }

    public static List<AccountEntry> of(List<Integer> accounts, List<String> amounts, LocalDate date, String text, boolean debit) {
        List<AccountEntry> splits = new ArrayList<>(accounts.size());
        for (int i = 0; i < accounts.size(); i++) {
            splits.add(new AccountEntry(String.valueOf(accounts.get(i)), date, new BigDecimal(amounts.get(i)), text, debit));
        }
        return splits;
    }

    private final List<AccountEntry> debitSplits;
    private final List<AccountEntry> creditSplits;
    private final String reference;
    private final String description;
    private final LocalDate date;
    private final String vatCode;

    public Transaction(LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, String description, String reference, String vatCode) {
        this.date = date;
        this.description = description;
        this.reference = reference;
        this.debitSplits = List.of(new AccountEntry(debitAccount, date, amount, reference, true));
        this.creditSplits = List.of(new AccountEntry(creditAccount, date, amount, reference, false));
        this.vatCode = vatCode;
    }

    public Transaction(LocalDate date, List<AccountEntry> debitSplits, List<AccountEntry> creditSplits, String description, String reference) {
        this.date = date;
        this.description = description;
        this.reference = reference;
        this.debitSplits = Collections.unmodifiableList(debitSplits);
        this.creditSplits = Collections.unmodifiableList(creditSplits);
        this.vatCode = null;
    }

    public List<AccountEntry> debitSplits() {
        return debitSplits;
    }

    public List<AccountEntry> creditSplits() {
        return creditSplits;
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
        return creditSplits.get(0).account();
    }

    /**
     * Returns the debit (Soll) account of the transaction.
     *
     * @return debit account of the transaction
     */
    public String debitAccount() {
        return debitSplits.get(0).account();
    }

    /**
     * Returns the amount of the transaction.
     *
     * @return amount of the transaction
     */
    public BigDecimal amount() {
        return debitSplits.stream().map(AccountEntry::amount).reduce(BigDecimal::add).get();
    }

    /**
     * Returns a human-readable description of the transaction.
     *
     * @return description of the transaction
     */
    public String description() {
        return description;
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
     * returns true if the transaction credit side is split between multiple accounts.
     *
     * @return true if the transaction is split
     */
    public boolean isSplit() {
        return !(debitSplits.isEmpty() && creditSplits.isEmpty());
    }

    public String vatCode() {
        return vatCode;
    }
}

