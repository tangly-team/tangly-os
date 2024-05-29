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

import net.tangly.core.HasTags;
import net.tangly.core.Tag;
import net.tangly.erp.ledger.services.VatCode;
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

    private final String reference;

    /**
     * Text describing the entry, often a reference to a document such as an invoice.
     */
    private final String text;

    /**
     * Is the entry on the debit side or not (meaning credit side).
     */
    private final boolean debit;

    private final VatCode vatCode;

    private final Set<Tag> tags;

    public AccountEntry(@NotNull String accountId, @NotNull LocalDate date, @NotNull BigDecimal amount, String reference, String text, boolean debit,
                        VatCode vatCode, Collection<Tag> tags) {
        this.accountId = accountId;
        this.date = date;
        this.amount = amount;
        this.reference = reference;
        this.text = text;
        this.debit = debit;
        this.vatCode = vatCode;
        this.tags = new HashSet<>();
        this.tags.addAll(tags);
    }

    public AccountEntry(@NotNull String accountId, @NotNull LocalDate date, @NotNull BigDecimal amount, String reference, String text, boolean debit,
                        VatCode vatCode) {
        this(accountId, date, amount, reference, text, debit, vatCode, Collections.emptyList());
    }

    @NotNull
    @Contract("_, _, _, _, _, _ -> new")
    public static AccountEntry credit(String account, String date, String amount, String reference, String text, VatCode vatCode) {
        return new AccountEntry(account, LocalDate.parse(date), new BigDecimal(amount), reference, text, false, vatCode);
    }

    @NotNull
    @Contract("_, _, _, _, _, _ -> new")
    public static AccountEntry debit(String account, String date, String amount, String reference, String text, VatCode vatCode) {
        return new AccountEntry(account, LocalDate.parse(date), new BigDecimal(amount), reference, text, true, vatCode);
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

    public String reference() {
        return reference;
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

    public Optional<VatCode> vatCode() {
        return Optional.ofNullable(vatCode);
    }

    public String vatCodeAsString() {
        return vatCode != null ? vatCode.name() : "";
    }

    // region HasTags Entity

    @Override
    public Collection<Tag> tags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public void tags(@NotNull Collection<Tag> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    @Override
    public void clear() {
        tags.clear();
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
        return vatCode().map(VatCode::vatRate);
    }

    public Optional<BigDecimal> getVatDue() {
        return vatCode().map(VatCode::vatDueRate);
    }
}
