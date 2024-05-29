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
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * The account entry class models one booking in an account of a double entry accounting system. The class is immutable.
 *
 * @param accountId the account on which the entry is booked
 * @param date      the date of the booking entry
 * @param amount    the amount of the entry
 * @param reference the reference of the entry
 * @param text      the text of the entry
 * @param isDebit   is the entry on the debit side or not (meaning credit side)
 * @param vatCode   the VAT code of the entry
 * @param tags      the tags of the entry
 */
public record AccountEntry(@NotNull String accountId, @NotNull LocalDate date, @NotNull BigDecimal amount, String reference, String text, boolean isDebit,
                           VatCode vatCode, Collection<Tag> tags) implements HasTags {
    public static final String FINANCE = "fin";
    public static final String PROJECT = "project";
    public static final String SEGMENT = "segment";
    public static final String DATE_EXPECTED = "date-expected";


    public AccountEntry of(@NotNull String accountId, @NotNull LocalDate date, @NotNull BigDecimal amount, String reference, String text, boolean debit,
                           VatCode vatCode) {
        return new AccountEntry(accountId, date, amount, reference, text, debit, vatCode, Collections.emptyList());
    }

    public static AccountEntry credit(String account, LocalDate date, BigDecimal amount, String reference, String text, VatCode vatCode, Collection<Tag> tags) {
        return new AccountEntry(account, date, amount, reference, text, false, vatCode, tags);
    }

    public static AccountEntry credit(String account, LocalDate date, BigDecimal amount, String reference, String text, VatCode vatCode) {
        return new AccountEntry(account, date, amount, reference, text, false, vatCode, Collections.emptyList());
    }

    public static AccountEntry debit(String account, LocalDate date, BigDecimal amount, String reference, String text, VatCode vatCode, Collection<Tag> tags) {
        return new AccountEntry(account, date, amount, reference, text, true, vatCode, tags);
    }

    public static AccountEntry debit(String account, LocalDate date, BigDecimal amount, String reference, String text, VatCode vatCode) {
        return new AccountEntry(account, date, amount, reference, text, true, vatCode, Collections.emptyList());
    }

    public boolean isCredit() {
        return !isDebit();
    }

    public boolean hasVatCode() {
        return vatCode != null;
    }

    public String vatCodeAsString() {
        return vatCode != null ? vatCode.name() : "";
    }

    @Override
    public Collection<Tag> tags() {
        return tags;
    }

    @Override
    public String toString() {
        return """
            AccountEntry[accountId=%s, date=%s, amount=%s, reference=%s, text=%s, isDebit=%s, vatCode=%s tags=%s]
            """.formatted(accountId(), date(), amount(), reference(), text(), isDebit(), vatCode(), tags());
    }

    public Optional<BigDecimal> getVat() {
        return Optional.ofNullable(vatCode()).map(VatCode::vatRate);
    }

    public Optional<BigDecimal> getVatDue() {
        return Optional.ofNullable(vatCode()).map(VatCode::vatDueRate);
    }
}
