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

package net.tangly.crm.ports;

import java.math.BigDecimal;
import java.time.LocalDate;

import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.invoices.Invoice;
import net.tangly.commons.utilities.DateUtilities;
import org.jetbrains.annotations.NotNull;

/**
 * Define business logic rules and functions for the CRM domain model. It connects the CRM entities with the invoices component.
 */
public class CrmBusinessLogic {
    private final Crm crm;

    public CrmBusinessLogic(@NotNull Crm crm) {
        this.crm = crm;
    }

    public BigDecimal contractAmountWithoutVat(@NotNull Contract contract, LocalDate from, LocalDate to) {
        return crm.invoices().getAll().stream().filter(o -> (o.contract().oid() == contract.oid()) && DateUtilities.isWithinRange(o.dueDate(), from, to))
                .map(Invoice::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal customerAmountWithoutVat(@NotNull LegalEntity customer, LocalDate from, LocalDate to) {
        return crm.contracts().getAll().stream().filter(o -> (o.sellee().oid() == customer.oid())).map(o -> contractAmountWithoutVat(o, from, to))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
