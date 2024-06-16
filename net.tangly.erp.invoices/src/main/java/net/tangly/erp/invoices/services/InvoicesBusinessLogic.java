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

package net.tangly.erp.invoices.services;

import net.tangly.erp.invoices.domain.Invoice;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import static net.tangly.commons.lang.Dates.isWithinRange;

/**
 * The business logic and rules of the bounded domain for invoices entities.
 */
public class InvoicesBusinessLogic {
    private final InvoicesRealm realm;

    public InvoicesBusinessLogic(@NotNull InvoicesRealm realm) {
        this.realm = realm;
    }

    public InvoicesRealm realm() {
        return realm;
    }

    public BigDecimal expensesForContract(@NotNull String contractId, LocalDate from, LocalDate to) {
        return realm.invoices().items().stream().filter(o -> (contractId.equals(o.contractId())) && isWithinRange(o.date(), from, to)).map(Invoice::expenses)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal invoicedAmountWithoutVatForContract(@NotNull String contractId, LocalDate from, LocalDate to) {
        return realm.invoices().items().stream().filter(o -> (contractId.equals(o.contractId())) && isWithinRange(o.date(), from, to))
            .map(Invoice::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal paidAmountWithoutVatForContract(@NotNull String contractId, LocalDate from, LocalDate to) {
        return realm.invoices().items().stream().filter(o -> (contractId.equals(o.contractId())) && isWithinRange(o.dueDate(), from, to))
            .map(Invoice::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal invoicedAmountWithoutVatForCustomer(@NotNull String customerId, LocalDate from, LocalDate to) {
        return realm.invoices().items().stream().filter(o -> (customerId.equals(o.invoicedEntity().id())) && isWithinRange(o.date(), from, to))
            .map(Invoice::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal paidAmountWithoutVatForCustomer(@NotNull String customerId, LocalDate from, LocalDate to) {
        return realm.invoices().items().stream().filter(o -> (customerId.equals(o.invoicedEntity().id())) && isWithinRange(o.dueDate(), from, to))
            .map(Invoice::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Semantically copy an invoice. The identifier and all invoice dates are not copied.
     *
     * @param invoice invoice prototype to copy
     * @return a new copied invoice instance
     */
    public Invoice createWith(@NotNull Invoice invoice) {
        Invoice copy = new Invoice();
        copy.name(invoice.name());
        copy.text(invoice.text());
        copy.invoicingEntity(invoice.invoicingEntity());
        copy.invoicedEntity(invoice.invoicedEntity());
        copy.contractId(invoice.contractId());
        copy.currency(invoice.currency());
        copy.locale(invoice.locale());
        copy.paymentConditions(invoice.paymentConditions());
        invoice.lines().forEach(copy::add);
        return copy;
    }
}
