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

import net.tangly.commons.models.HasTags;
import net.tangly.commons.models.Tag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * The account entry class models one booking in an account of a double entry accounting system. The class is immutable.
 */
public class AccountEntry implements HasTags {
    public static final String FINANCE = "fin";
    public static final String VAT = "vat";
    public static final String VAT_DUE = "vat-due";
    public static final String PROJECT = "project";
    /**
     * Account on which the entry is booked.
     */
    private final String account;

    /**
     * Date of the booking entry.
     */
    private final LocalDate date;

    /**
     * Amount of the entry.
     */
    private final BigDecimal amount;

    /**
     * Text describing the entry, often a reference to a document such as an invoice
     */
    private final String text;

    /**
     * Is the entry on the debit side or not (meaning credit side).
     */
    private final boolean debit;

    private final Set<Tag> tags;

    @NotNull
    @Contract("_, _, _, _ -> new")
    public static AccountEntry debit(String account, String date, String amount, String text) {
        return new AccountEntry(account, LocalDate.parse(date), new BigDecimal(amount), text, true);
    }

    @NotNull
    @Contract("_, _, _, _ -> new")
    public static AccountEntry credit(String account, String date, String amount, String text) {
        return new AccountEntry(account, LocalDate.parse(date), new BigDecimal(amount), text, false);
    }

    public AccountEntry(String account, LocalDate date, BigDecimal amount, String text, boolean debit) {
        this.account = account;
        this.date = date;
        this.amount = amount;
        this.text = text;
        this.debit = debit;
        this.tags = new HashSet<>();
    }

    public String account() {
        return account;
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
    public Set<Tag> tags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public void add(Tag tag) {
        tags.add(tag);
    }

    @Override
    public void remove(Tag tag) {
        tags.remove(tag);
    }

    @Override
    public void clear() {
        tags.clear();
    }


    // endregion

    @Override
    public String toString() {
        return "{account=" + Objects.toString(account) + ", date=" + Objects.toString(date) + ", amount=" + Objects
                .toString(amount) + ", isDebit=" + Objects.toString(debit) + ", text=" + Objects.toString(text) + "}";
    }

    public Optional<BigDecimal> getVat() {
        return findBy(FINANCE, VAT).map(tag1 -> (BigDecimal) tag1.value());
    }
}
