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

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Defines an account as seen for double entry booking ledger and legal accounting for tax ports.
 * { id = 1, kind = ASSET, description = "Assets"}
 */
public class Account {

    public enum AccountKind {ASSET, LIABILITY, INCOME, EXPENSE, AGGREGATE}

    public enum AccountGroup {ASSETS, LIABILITIES, EXPENSES, PROFITS_AND_LOSSES}

    public static Account of(int id, AccountKind kind, String description, String ownedByGroupId) {
        return new Account(Integer.toString(id), kind, Currency.getInstance("CHF"), description, ownedByGroupId);
    }

    public static Account of(String id, AccountGroup group, String description, String ownedByGroupId) {
        return new Account(id, AccountKind.AGGREGATE, group, Currency.getInstance("CHF"), description, ownedByGroupId);
    }

    /**
     * Unique identifier of the account in the ledger context.
     */
    private final String id;

    /**
     * Kind of the account.
     */
    private final AccountKind kind;

    /**
     * Group to which the account belongs to.
     */
    private final AccountGroup group;

    /**
     * Currency of the account.
     */
    private final Currency currency;

    /**
     * Human readable description of the account function.
     */
    private final String description;

    /**
     * Optional account identifier owning the account.
     */
    private final String ownedBy;

    /**
     * Set of accounts the account owns.
     */
    private final Set<Account> aggregatedAccounts;

    /**
     * Entries describing the bookings on the account.
     */
    private final List<AccountEntry> entries;

    public Account(String id, AccountKind kind, Currency currency, String description, String ownedBy) {
        this(id, kind, null, currency, description, ownedBy);
    }

    public Account(@NotNull String id, @NotNull AccountKind kind, AccountGroup group, @NotNull Currency currency, String description, String ownedBy) {
        this.id = id;
        this.kind = kind;
        this.group = group;
        this.currency = currency;
        this.description = description;
        this.ownedBy = ownedBy;
        this.aggregatedAccounts = new HashSet<>();
        this.entries = new ArrayList<>();
    }

    public String id() {
        return id;
    }

    public AccountKind kind() {
        return kind;
    }

    public Currency currency() {
        return currency;
    }

    public Set<Account> aggregatedAccounts() {
        return Collections.unmodifiableSet(aggregatedAccounts);
    }

    public String description() {
        return description;
    }

    public String ownedBy() {
        return ownedBy;
    }

    public AccountGroup group() {
        switch (kind()) {
            case ASSET:
                return AccountGroup.ASSETS;
            case LIABILITY:
                return AccountGroup.LIABILITIES;
            case EXPENSE:
            case INCOME:
                return AccountGroup.PROFITS_AND_LOSSES;
            case AGGREGATE:
                return group;
        }
        return null;
    }

    /**
     * Returns true if the account is an aggregate account, meaning the balance is the aggregation of a the balance of a set of accounts.
     *
     * @return flag indicating if the account is an aggregate account
     */
    public boolean isAggregate() {
        return AccountKind.AGGREGATE.equals(kind());
    }

    /**
     * Returns true if the account is a debit account.
     *
     * @return flag indicating if the account is a debit account
     */
    public boolean isDebit() {
        return (AccountKind.ASSET.equals(kind()) || AccountKind.EXPENSE.equals(kind()));
    }

    /**
     * Returns true if the account is a credit account.
     *
     * @return flag indicating if the account is a credit account
     */
    public boolean isCredit() {
        return (AccountKind.LIABILITY.equals(kind()) || AccountKind.INCOME.equals(kind()));
    }

    /**
     * Debits and Credits are from the bank’s point of view and so are the opposite of what you expect when it comes to the maths
     * involved. Debits - meaning assets and expenses - have a positive balance and Credits - meaning liabilities and income -
     * have a negative balance.
     * <p>
     * If I Debit (add to) a Debit account it gets more positive and has a larger value (Debit -> Soll)
     * <p>
     * If I Credit (subtract from) a Debit account, it gets less positive and has a smaller value (Credit -> Haben)
     * <p>
     * If I Debit (add to) a Credit account, it gets less negative and has a smaller value
     * <p>
     * If I Credit (subtract from) a Credit account, it gets more negative and has a larger value
     *
     * @param date date at which the balance is computed
     */
    public BigDecimal balance(LocalDate date) {
        if (isAggregate()) {
            return aggregatedAccounts.stream().map(o -> o.balance(date)).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        } else {
            return entries.stream().filter(o -> date.compareTo(o.date()) >= 0).map(this::booking).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        }
    }

    public List<AccountEntry> getEntriesFor(LocalDate from, LocalDate to) {
        return entries.stream().filter(o -> (o.date().isAfter(from) || o.date().equals(from)) && (o.date().isBefore(to) || o.date().isEqual(to)))
                .collect(Collectors.toList());
    }

    public void addEntry(AccountEntry entry) {
        entries.add(entry);
    }


    void updateAggregatedAccounts(Collection<Account> aggregatedAccounts) {
        this.aggregatedAccounts.clear();
        this.aggregatedAccounts.addAll(aggregatedAccounts);
    }

    private BigDecimal booking(AccountEntry entry) {
        if (entry.isCredit()) {
            // debit account: asset, expense -> means increase; credit account: liability, income -> means decrease
            return entry.amount().negate();
        } else if (entry.isDebit()) {
            // debit account: asset, expense -> means decrease; credit account: liability, income -> means increase
            return entry.amount();
        } else {
            return BigDecimal.ZERO;
        }

    }
}
