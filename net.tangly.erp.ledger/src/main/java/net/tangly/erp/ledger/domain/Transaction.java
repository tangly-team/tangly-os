/*
 * Copyright 2006-2024 Marcel Baumann
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

package net.tangly.erp.ledger.domain;

import net.tangly.core.HasDate;
import net.tangly.core.HasText;
import net.tangly.erp.ledger.services.VatCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A simple transaction is a money transfer between a debit and a credit account. A split transaction is a money transfer between a debit account and a set of
 * credit accounts or a credit account and a set of debit accounts. The total debit and the total credit of a transaction must be the same format. The majority
 * of transactions have one debit and one credit account. The class is immutable.
 * <p> Two kinds of transactions are supported. The simple transaction is a transfer from one debit account to a credit account. All information are stored
 * at the transaction level. The split transaction is a transfer from one debit account to multiple credit accounts or from one credit account to multiple
 * debit accounts. General information is stored on the transaction level and specific data is stored in the associated split accounts.</p>
 * <p>A transaction is an event in the ledger domain. The whole sequence of transactions fully describes the state of the ledger and all accounts defined
 * inside.</p>
 * <p> A transaction can have a VAT code. The code is either on the debit or the credit account. If the transaction is split the VAT code can optionally
 * be defined on multiple split positions. In this case the debit or credit account should not have a VAT code.</p>
 */
public class Transaction implements HasDate, HasText {
    private final LocalDate date;
    private final String reference;
    private final String text;
    private final AccountEntry debit;
    private final AccountEntry credit;
    private final LocalDate dateExpected;
    private final boolean synthetic;
    private final List<AccountEntry> splits;

    public Transaction(LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, String text, String reference) {
        this(date, text, reference, amount, debitAccount, creditAccount, null, null, null);
    }

    public Transaction(LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, String text, String reference, List<AccountEntry> splits) {
        this(date, text, reference, amount, debitAccount, creditAccount, null, null, splits);
    }

    public Transaction(LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, String text, String reference, VatCode vatCode,
                       LocalDate dateExpected,
                       List<AccountEntry> splits) {
        this(date, text, reference, (debitAccount != null) ? AccountEntry.debit(debitAccount, date, amount, null, null, vatCode) : null,
            (creditAccount != null) ? AccountEntry.credit(creditAccount, date, amount, null, null, vatCode) : null, vatCode, dateExpected, false, splits);
    }

    public static Transaction of(LocalDate date, String reference, String text, AccountEntry debitAccount, AccountEntry creditAccount, VatCode vatCode,
                                 LocalDate dateExpected, List<AccountEntry> splits) {
        return new Transaction(date, text, reference, debitAccount, creditAccount, vatCode, dateExpected, false, splits);
    }

    public Transaction(LocalDate date, String text, String reference,
                       AccountEntry debit, AccountEntry credit, VatCode vatCode, LocalDate dateExpected, boolean synthetic, List<AccountEntry> splits) {
        this.date = date;
        this.text = text;
        this.reference = reference;
        this.debit = debit;
        this.credit = credit;
        this.dateExpected = dateExpected;
        this.synthetic = synthetic;
        this.splits = (Objects.nonNull(splits) && !splits.isEmpty()) ? List.copyOf(splits) : Collections.emptyList();
    }

    public static Transaction of(String date, int debitAccount, int creditAccount, String amount, String text) {
        return new Transaction(LocalDate.parse(date), String.valueOf(debitAccount), String.valueOf(creditAccount), new BigDecimal(amount), text, null);
    }

    public static Transaction of(String date, int debitAccount, int creditAccount, String amount, String text, String reference) {
        return new Transaction(LocalDate.parse(date), String.valueOf(debitAccount), String.valueOf(creditAccount), new BigDecimal(amount), text, reference);
    }

    public static Transaction ofSynthetic(LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, String text, String reference,
                                          VatCode vatCode, LocalDate dateExpected, List<AccountEntry> splits) {
        return new Transaction(date, text, reference,
            (debitAccount != null) ? AccountEntry.debit(debitAccount, date, amount, null, null, vatCode) : null,
            (creditAccount != null) ? AccountEntry.credit(creditAccount, date, amount, null, null, vatCode) : null,
            vatCode, dateExpected, true, splits);
    }

    public List<AccountEntry> debitSplits() {
        return (debit != null) ? List.of(debit) : splits;
    }

    public boolean hasDebitSplits() {
        return Objects.isNull(debit);
    }

    public List<AccountEntry> creditSplits() {
        return (credit != null) ? List.of(credit) : splits;
    }

    public boolean hasCreditSplits() {
        return Objects.isNull(credit);
    }

    public AccountEntry debit() {
        return debit;
    }

    public AccountEntry credit() {
        return credit;
    }

    public LocalDate dateExpected() {
        return dateExpected;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public List<AccountEntry> splits() {
        return splits;
    }

    public Optional<VatCode> vatCode() {
        return Optional.ofNullable((debit != null) ? debit.vatCode() : credit.vatCode());
    }

    public String vatCodeAsString() {
        return vatCode().map(VatCode::code).orElse(null);
    }

    /**
     * Return an optional reference identifier to an external accounting document describing the transaction.
     *
     * @return the reference identifier. It can be null
     */
    public String reference() {
        return reference;
    }

    /**
     * Return the credit (Haben) account of the transaction.
     *
     * @return credit account of the transaction
     */
    public String creditAccount() {
        return (credit != null) ? credit.accountId() : null;
    }

    /**
     * Return the debit (Soll) account of the transaction.
     *
     * @return debit account of the transaction
     */
    public String debitAccount() {
        return (debit != null) ? debit.accountId() : null;
    }

    /**
     * Return the amount of the transaction.
     *
     * @return amount of the transaction
     */
    public BigDecimal amount() {
        return (debit != null) ? debit.amount() : credit.amount();
    }

    /**
     * Return a human-readable description of the transaction.
     *
     * @return description of the transaction
     */
    @Override
    public String text() {
        return text;
    }

    /**
     * Return the date of the execution for the transaction.
     *
     * @return date of the transaction
     */
    @Override
    public LocalDate date() {
        return date;
    }

    /**
     * Return true if the transaction is split between multiple accounts.
     *
     * @return true if the transaction is split
     */
    public boolean isSplit() {
        return (debit == null) || (credit == null);
    }

    @Override
    public String toString() {
        return """
            Transaction[date=%s, debit=%s, credit=%s, splits=%s, reference=%s, text=%s, dateExpected=%s
            """.formatted(date(), debitAccount(), creditAccount(), splits, reference(), text(), dateExpected());
    }
}
