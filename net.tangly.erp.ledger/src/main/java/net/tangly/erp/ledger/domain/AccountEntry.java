/*
 * Copyright 2006-2023 Marcel Baumann
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

package net.tangly.erp.ledger.domain;

import net.tangly.core.HasTags;
import net.tangly.core.Tag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * The account entry class models one booking in an account of a double entry accounting system. The class is immutable.
 */
public class AccountEntry implements HasTags {
    public static final String FINANCE = "fin";
    public static final String VAT = "vat";
    public static final String VAT_DUE = "vat-due";
    public static final String VAT_FLAG = "flag";
    public static final String PROJECT = "project";
    public static final String SEGMENT = "segment";
    public static final String DATE_EXPECTED = "date-expected";

    /**
     * Account on which the entry is booked.
     */
    private final String accountId;

    /**
     * Date of the booking entry.
     */
    private final LocalDate date;

    /**
     * Amount of the entry.
     */
    private final BigDecimal amount;

    /**
     * Text describing the entry, often a reference to a document such as an invoice.
     */
    private final String text;

    /**
     * Is the entry on the debit side or not (meaning credit side).
     */
    private final boolean debit;

    private final Set<Tag> tags;

    public AccountEntry(@NotNull String accountId, @NotNull LocalDate date, @NotNull BigDecimal amount, String text, boolean debit) {
        this.accountId = accountId;
        this.date = date;
        this.amount = amount;
        this.text = text;
        this.debit = debit;
        this.tags = new HashSet<>();
    }

    public AccountEntry(String accountId, LocalDate date, BigDecimal amount, String text, boolean debit, Collection<Tag> tags) {
        this(accountId, date, amount, text, debit);
        this.tags.addAll(tags);
    }

    @NotNull
    @Contract("_, _, _, _ -> new")
    public static AccountEntry credit(String account, String date, String amount, String text) {
        return new AccountEntry(account, LocalDate.parse(date), new BigDecimal(amount), text, false);
    }

    @NotNull
    @Contract("_, _, _, _ -> new")
    public static AccountEntry debit(String account, String date, String amount, String text) {
        return new AccountEntry(account, LocalDate.parse(date), new BigDecimal(amount), text, true);
    }

    public String accountId() {
        return accountId;
    }

    public LocalDate date() {
        return date;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String text() {
        return text;
    }

    public boolean isDebit() {
        return debit;
    }

    public boolean isCredit() {
        return !isDebit();
    }

    // region HasTags Entity

    @Override
    public Collection<Tag> tags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public void tags(@NotNull Collection<Tag> tags) {
        tags.clear();
        tags.addAll(tags);
    }

    @Override
    public boolean add(Tag tag) {
        return tags.add(tag);
    }

    @Override
    public boolean remove(Tag tag) {
        return tags.remove(tag);
    }

    // endregion

    @Override
    public String toString() {
        return """
            AccountEntry[accountId=%s, date=%s, amount=%s, isDebit=%s", text="%s, tags=%s]
            """.formatted(accountId(), date(), amount(), isDebit(), text(), tags());
    }

    public Optional<BigDecimal> getVat() {
        return findBy(FINANCE, VAT).map(tag1 -> new BigDecimal(tag1.value()));
    }

    public Optional<BigDecimal> getVatDue() {
        return findBy(FINANCE, VAT_DUE).map(tag1 -> new BigDecimal(tag1.value()));
    }
}
