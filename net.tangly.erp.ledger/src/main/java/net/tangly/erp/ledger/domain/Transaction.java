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
import org.jetbrains.annotations.NotNull;

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
public record Transaction(@NotNull LocalDate date, String text, String reference, AccountEntry debit, AccountEntry credit, LocalDate dateExpected,
                          boolean synthetic, List<AccountEntry> splits) implements HasDate, HasText {

    public Transaction {
        splits = (Objects.nonNull(splits) && !splits.isEmpty()) ? List.copyOf(splits) : Collections.emptyList();
    }

    public Transaction withDateExpected(@NotNull LocalDate dateExpected) {
        return new Transaction(date(), text(), reference(), debit(), credit(), dateExpected, synthetic(), splits());
    }

    public Transaction(@NotNull LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, String text, String reference) {
        this(date, text, reference, amount, debitAccount, creditAccount, null, null, Collections.emptyList());
    }

    public Transaction(@NotNull LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, String text, String reference,
                       List<AccountEntry> splits) {
        this(date, text, reference, amount, debitAccount, creditAccount, null, null, splits);
    }

    public Transaction(@NotNull LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, String text, String reference, VatCode vatCode,
                       LocalDate dateExpected, List<AccountEntry> splits) {
        this(date, text, reference, (debitAccount != null) ? AccountEntry.debit(debitAccount, date, amount, null, null, vatCode) : null,
            (creditAccount != null) ? AccountEntry.credit(creditAccount, date, amount, null, null, vatCode) : null, dateExpected, false, splits);
    }

    public static Transaction of(@NotNull LocalDate date, String reference, String text, AccountEntry debitAccount, AccountEntry creditAccount,
                                 LocalDate dateExpected, List<AccountEntry> splits) {
        return new Transaction(date, text, reference, debitAccount, creditAccount, dateExpected, false, splits);
    }

    public static Transaction of(@NotNull String date, int debitAccount, int creditAccount, String amount, String text) {
        return new Transaction(LocalDate.parse(date), String.valueOf(debitAccount), String.valueOf(creditAccount), new BigDecimal(amount), text, null);
    }

    public static Transaction of(String date, int debitAccount, int creditAccount, String amount, String text, String reference) {
        return new Transaction(LocalDate.parse(date), String.valueOf(debitAccount), String.valueOf(creditAccount), new BigDecimal(amount), text, reference);
    }

    public static Transaction ofSynthetic(LocalDate date, String debitAccount, String creditAccount, BigDecimal amount, String text, String reference,
                                          VatCode vatCode, LocalDate dateExpected, List<AccountEntry> splits) {
        return new Transaction(date, text, reference, (debitAccount != null) ? AccountEntry.debit(debitAccount, date, amount, null, null, vatCode) : null,
            (creditAccount != null) ? AccountEntry.credit(creditAccount, date, amount, null, null, vatCode) : null, dateExpected, true, splits);
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

    public Optional<VatCode> vatCode() {
        return Optional.ofNullable((debit != null) ? debit.vatCode() : credit.vatCode());
    }

    public String vatCodeAsString() {
        return vatCode().map(VatCode::code).orElse(null);
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
     * Return true if the transaction is split between multiple accounts.
     *
     * @return true if the transaction is split
     */
    public boolean isSplit() {
        return (debit == null) || (credit == null);
    }
}
