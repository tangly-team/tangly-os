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

package net.tangly.erp.ledger.services;

import net.tangly.core.DateRange;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.domain.AccountEntry;
import net.tangly.erp.ledger.domain.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The ledger implements a ledger with a chart of accounts and a set of transactions. It provides the logic for the automatic processing of VAT amounts and related bookings to the
 * VAT related accounts.
 */
public interface LedgerRealm extends Realm {
    String VAT_FLAT_RATE = "VAT flat rate";
    String VAT_ACCOUNT = "2201";
    Logger logger = LogManager.getLogger();

    Provider<Account> accounts();

    Provider<Transaction> transactions();

    Provider<AccountEntry> entries();

    default List<Account> assets() {
        return accounts().items().stream().filter(o -> Account.AccountGroup.ASSETS == o.group()).toList();
    }

    default List<Account> liabilities() {
        return accounts().items().stream().filter(o -> Account.AccountGroup.LIABILITIES == o.group()).toList();
    }

    default List<Account> profitAndLoss() {
        return accounts().items().stream().filter(o -> Account.AccountGroup.PROFITS_AND_LOSSES == o.group()).toList();
    }

    default List<Account> bookableAccounts() {
        return accounts().items().stream().filter(o -> !o.isAggregate()).toList();
    }

    default List<Transaction> transactions(LocalDate from, LocalDate to) {
        return transactions().items().stream().filter(o -> DateRange.of(from, to).isActive(o.date())).toList();
    }

    default Optional<Account> accountBy(String id) {
        return accounts().items().stream().filter(o -> id.equals(o.id())).findAny();
    }

    default List<Account> accountsOwnedBy(String id) {
        return accounts().items().stream().filter(o -> id.equals(o.ownedBy())).toList();
    }

    void add(@NotNull Account account);

    default void book(@NotNull Transaction transaction) {
        bookTransaction(transaction);
        Transaction vatSyntheticTransaction = computeVat(transaction);
        if (vatSyntheticTransaction != null) {
            bookTransaction(vatSyntheticTransaction);
        }
    }

    /**
     * Create synthetic transactions to deduct the VAT due amount and transfer it to the VAT account.
     *
     * @param transaction transaction which VAT amount shall be computed and transferred to the VAT account.
     * @return the transaction with the VAT entries
     */
    default Transaction computeVat(@NotNull Transaction transaction) {
        Transaction vatSyntheticTransaction = null;
        var vatCode = transaction.vatCode().orElse(null);
        if (!transaction.isSplit() && Objects.nonNull(vatCode)) {
            // non-split transaction with VAT code defined is processed to compute the VAT amount to be transferred to the VAT account
            BigDecimal vatDue = transaction.amount().multiply(vatCode.vatDueRate());
            vatSyntheticTransaction = Transaction.ofSynthetic(transaction.date(), transaction.creditAccount(), VAT_ACCOUNT, vatDue, VAT_FLAT_RATE,
                transaction.reference(), vatCode, null, Collections.emptyList());
        } else if (transaction.isSplit() && transaction.splits().stream().anyMatch(o -> Objects.nonNull(o.vatCode()))) {
            // split transaction with VAT code defined in at least one split is processed to compute the VAT amount to be transferred to the VAT account
            var splits = transaction.splits().stream().map(o ->
                {
                    BigDecimal vatDueRate = Objects.nonNull(o.vatCode()) ? o.vatCode().vatDueRate() : BigDecimal.ZERO;
                    return AccountEntry.credit(VAT_ACCOUNT, o.date(), o.amount().multiply(vatDueRate), o.reference(), VAT_FLAT_RATE, o.vatCode());
                })
                .toList();
            BigDecimal totalVatDue = splits.stream().map(AccountEntry::amount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            vatSyntheticTransaction = Transaction.ofSynthetic(transaction.date(), transaction.splits().getFirst().accountId(), null, totalVatDue, VAT_FLAT_RATE,
                transaction.reference(), vatCode, null, splits);

        }
        return vatSyntheticTransaction;
    }

    /**
     * Build the account tree structure and perform basic validation.
     */
    default void build() {
        accounts().items().stream().filter(Account::isAggregate)
            .forEach(o -> o.updateAggregatedAccounts(accounts().items().stream().filter(sub -> o.id().equals(sub.ownedBy())).toList()));
        accounts().items().stream().filter(Account::isAggregate).filter(o -> o.aggregatedAccounts().isEmpty())
            .forEach(o -> logger.atError().log("Aggregate account wrongly defined {}", o.id()));
    }

    private void bookEntry(@NotNull AccountEntry entry) {
        Optional<Account> account = accountBy(entry.accountId());
        account.ifPresent(o -> {
            o.addEntry(entry);
            accounts().update(o);
        });

        if (account.isEmpty()) {
            logger.atError().log("account {} for entry with amount {} booked {} is undefined", entry.accountId(), entry.amount(), entry.date());
        }
    }

// region VAT-computations

    default BigDecimal computeVatSales(LocalDate from, LocalDate to) {
        return transactions(from, to).stream().flatMap(o -> o.creditSplits().stream()).filter(o -> Objects.nonNull(o.vatCode()))
            .map(AccountEntry::amount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    default BigDecimal computeVat(LocalDate from, LocalDate to) {
        return transactions(from, to).stream().flatMap(o -> o.creditSplits().stream()).map(o -> {
            Optional<BigDecimal> vat = o.getVat();
            return vat.map(bigDecimal -> o.amount().subtract(o.amount().divide(BigDecimal.ONE.add(bigDecimal), 2, RoundingMode.HALF_UP))).orElse(
                BigDecimal.ZERO);
        }).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    default BigDecimal computeDueVat(LocalDate from, LocalDate to) {
        Optional<Account> account = accountBy("2201");
        return account.map(
                value -> value.getEntriesFor(from, to).stream().filter(AccountEntry::isCredit).map(AccountEntry::amount).reduce(BigDecimal::add).orElse(
                    BigDecimal.ZERO))
            .orElse(BigDecimal.ZERO);
    }

    private void bookTransaction(Transaction transaction) {
        transactions().update(transaction);
        transaction.creditSplits().forEach(this::bookEntry);
        transaction.debitSplits().forEach(this::bookEntry);
    }

// endregion
}
