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

package net.tangly.bus.crm;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.Product;
import net.tangly.bus.providers.Provider;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the customer relationship management <i>CRM</i> subsystem. The major abstractions are
 * <ul>
 *     <li>Natural entities</li>
 *     <li>Legal Entities</li>
 *     <li>Employees - defines a relation between a natural entity and a legal entity</li>
 *     <li>Contract - defines a legal contractual obligation between two legal entities</li>
 *     <li>Interaction - defines an interaction between your company and a set of legal and natural entities</li>
 *     <li>Activity - defines a specific event part of an interaction</li>
 *     <li>Subject - defines a registered user of the application</li>
 *     <li>CRM tags - defining an ontology in the business domain of customer relationships management</li>
 * </ul>
 */
public interface EntitiesRealm {
    TagTypeRegistry tagTypeRegistry();

    Provider<NaturalEntity> naturalEntities();

    Provider<LegalEntity> legalEntities();

    Provider<Employee> employees();

    Provider<Contract> contracts();

    Provider<Interaction> interactions();

    Provider<Activity> activities();

    Provider<Subject> subjects();

    Provider<Product> products();

    Provider<Invoice> invoices();

    default List<Employee> employeesFor(@NotNull LegalEntity entity) {
        return employees().items().stream().filter(o -> entity.oid() == o.organization().oid()).collect(Collectors.toList());
    }

    default List<Employee> employeesFor(@NotNull NaturalEntity entity) {
        return employees().items().stream().filter(o -> entity.oid() == o.person().oid()).collect(Collectors.toList());
    }

    default List<Invoice> invoicesFor(@NotNull Contract contract) {
        return invoices().items().stream().filter(o -> contract.oid() == o.contract().oid()).collect(Collectors.toList());
    }

    default List<Contract> contractsFor(@NotNull LegalEntity entity) {
        return contracts().items().stream().filter(o -> entity.oid() == o.sellee().oid()).collect(Collectors.toList());
    }

    default BigDecimal invoicedAmount(@NotNull Contract contract) {
        return invoices().items().stream().filter(o -> o.contract().oid() == contract.oid()).map(Invoice::amountWithoutVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal invoicedAmount(@NotNull LegalEntity customer) {
        return invoices().items().stream().filter((o -> o.invoicedEntity().oid() == customer.oid())).map(Invoice::amountWithoutVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
