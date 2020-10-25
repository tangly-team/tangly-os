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

package net.tangly.bus.invoices;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;

import static net.tangly.commons.utilities.DateUtilities.isWithinRange;

/**
 * The business logic and rules of the bounded domain of invoices entities.
 */
public class BusinessLogicInvoices {
    private final RealmInvoices realm;

    @Inject
    public BusinessLogicInvoices(@NotNull RealmInvoices realm) {
        this.realm = realm;
    }

    public RealmInvoices realm() {
        return realm;
    }

    public BigDecimal expensesForContract(@NotNull String contractId, LocalDate from, LocalDate to) {
        return realm.invoices().items().stream().filter(o -> (contractId.equals(o.contractId())) && isWithinRange(o.invoicedDate(), from, to))
                .map(Invoice::expenses).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal invoicedAmountWithoutVatForContract(@NotNull String contractId, LocalDate from, LocalDate to) {
        return realm.invoices().items().stream().filter(o -> (contractId.equals(o.contractId())) && isWithinRange(o.invoicedDate(), from, to))
                .map(Invoice::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal paidAmountWithoutVatForContract(@NotNull String contractId, LocalDate from, LocalDate to) {
        return realm.invoices().items().stream().filter(o -> (contractId.equals(o.contractId())) && isWithinRange(o.dueDate(), from, to))
                .map(Invoice::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal invoicedAmountWithoutVatForCustomer(@NotNull String customerId, LocalDate from, LocalDate to) {
        return realm.invoices().items().stream().filter(o -> (customerId.equals(o.invoicedEntity().id())) && isWithinRange(o.invoicedDate(), from, to))
                .map(Invoice::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal paidAmountWithoutVatForCustomer(@NotNull String customerId, LocalDate from, LocalDate to) {
        return realm.invoices().items().stream().filter(o -> (customerId.equals(o.invoicedEntity().id())) && isWithinRange(o.dueDate(), from, to))
                .map(Invoice::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
