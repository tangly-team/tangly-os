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

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ledger implements a ledger with a chart of accounts and a set of transactions. It provides the logic for the automatic processing of VAT amounts and
 * related bookings to the VAT related accounts.
 */
public class LedgerRealm {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final List<Account> accounts;
    private final List<Transaction> journal;

    public LedgerRealm() {
        accounts = new ArrayList<>();
        journal = new ArrayList<>();
    }

    public List<Account> assets() {
        return accounts.stream().filter(o -> Account.AccountGroup.ASSETS == o.group()).collect(Collectors.toUnmodifiableList());
    }

    public List<Account> liabilities() {
        return accounts.stream().filter(o -> Account.AccountGroup.LIABILITIES == o.group()).collect(Collectors.toUnmodifiableList());
    }

    public List<Account> profitAndLoss() {
        return accounts.stream().filter(o -> Account.AccountGroup.PROFITS_AND_LOSSES == o.group()).collect(Collectors.toUnmodifiableList());
    }

    public List<Account> bookableAccounts() {
        return accounts.stream().filter(o -> !o.isAggregate()).collect(Collectors.toUnmodifiableList());
    }

    public List<Transaction> transactions() {
        return Collections.unmodifiableList(journal);
    }

    public List<Transaction> transactions(LocalDate from, LocalDate to) {
        return journal.stream().filter(o -> (o.date().isAfter(from) || o.date().equals(from)) && (o.date().isBefore(to) || o.date().isEqual(to)))
            .collect(Collectors.toUnmodifiableList());
    }

    public List<Account> accounts() {
        return Collections.unmodifiableList(accounts);
    }

    public Optional<Account> accountBy(String id) {
        return accounts.stream().filter(o -> id.equals(o.id())).findAny();
    }

    public List<Account> accountsOwnedBy(String id) {
        return accounts.stream().filter(o -> id.equals(o.ownedBy())).collect(Collectors.toUnmodifiableList());
    }

    public void add(@NotNull Account account) {
        accounts.add(account);
    }

    /**
     * Adds a transaction to the ledger and the referenced accounts. A warning message is written to the log file if one of the involved accounts is not
     * registered in the ledger. The involved accounts cannot be aggregate accounts.
     *
     * @param transaction transaction to add to the ledger
     */
    public void add(@NotNull Transaction transaction) {
        Transaction booked = transaction;
        // handle VAT for credit entries in a non split-transaction, the payment is split between amount for the company, and due vat to government
        Optional<BigDecimal> vatDuePercent = transaction.creditSplits().get(0).getVatDue();
        if (!transaction.isSplit() && vatDuePercent.isPresent()) {
            AccountEntry credit = transaction.creditSplits().get(0);
            BigDecimal vatDue = credit.amount().multiply(vatDuePercent.get());
            List<AccountEntry> splits =
                List.of(new AccountEntry(credit.accountId(), credit.date(), credit.amount().subtract(vatDue), credit.text(), false, credit.tags()),
                    new AccountEntry("2201", credit.date(), vatDue, null, false));
            booked = new Transaction(transaction.date(), transaction.debitAccount(), null, transaction.amount(), splits, transaction.text(),
                transaction.reference());
        }
        journal.add(booked);
        booked.debitSplits().forEach(this::bookEntry);
        booked.creditSplits().forEach(this::bookEntry);
    }

    /**
     * Build the account tree structure and perform basic validation.
     */
    public void build() {
        accounts.stream().filter(Account::isAggregate)
            .forEach(o -> o.updateAggregatedAccounts(accounts.stream().filter(sub -> o.id().equals(sub.ownedBy())).collect(Collectors.toList())));
        accounts.stream().filter(Account::isAggregate).filter(o -> o.aggregatedAccounts().isEmpty())
            .forEach(o -> logger.atError().log("Aggregate account wrongly defined {}", o.id()));
    }

    private void bookEntry(@NotNull AccountEntry entry) {
        Optional<Account> account = accountBy(entry.accountId());
        account.ifPresent(o -> o.addEntry(entry));
        if (account.isEmpty()) {
            logger.atError().log("account {} for entry with amount {} booked {} is undefined", entry.accountId(), entry.amount(), entry.date());
        }
    }

    // region VAT-computations

    public BigDecimal computeVatSales(LocalDate from, LocalDate to) {
        return transactions(from, to).stream().flatMap(o -> o.creditSplits().stream()).filter(o -> o.findBy(AccountEntry.FINANCE, AccountEntry.VAT).isPresent())
            .map(AccountEntry::amount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    public BigDecimal computeVat(LocalDate from, LocalDate to) {
        return transactions(from, to).stream().flatMap(o -> o.creditSplits().stream()).map(o -> {
            Optional<BigDecimal> vat = o.getVat();
            return vat.map(bigDecimal -> o.amount().subtract(o.amount().divide(BigDecimal.ONE.add(bigDecimal), 2, RoundingMode.HALF_UP)))
                .orElse(BigDecimal.ZERO);
        }).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    public BigDecimal computeDueVat(LocalDate from, LocalDate to) {
        Optional<Account> account = accountBy("2201");
        return account.map(value -> value.getEntriesFor(from, to).stream().filter(AccountEntry::isCredit).map(AccountEntry::amount).reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO)).orElse(BigDecimal.ZERO);
    }

    // endregion
}
