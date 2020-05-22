/*
 * Copyright 2006-2020 Marcel Baumann
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

package net.tangly.bus.invoices;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import net.tangly.bus.crm.BankConnection;
import net.tangly.bus.crm.LegalEntity;

/**
 * The abstraction of an invoice with a set of positions, subtotals, one VAT rate and a total. The items and the subtotals have a position to order
 * them in the invoice. An invoice and its components have no dependencies to external entities. Therefore an invoice is complete and archived. For
 * example you can change the VAT percentage or a product price without any consequence on existing invoices.
 */
public class Invoice {

    private final String id;

    private LegalEntity invoicingEntity;

    private BankConnection invoicingConnection;

    private String contractId;

    private LegalEntity invoicedEntity;

    private LocalDate invoicedDate;

    private LocalDate dueDate;

    private Currency currency;

    private String text;

    private String paymentConditions;

    private BigDecimal vatRate;

    private final List<InvoiceLine> items;

    public Invoice(String id) {
        this.id = id;
        items = new ArrayList<>();
    }

    public String id() {
        return id;
    }

    public BigDecimal amountWithoutVat() {
        return items.stream().filter(InvoiceLine::isRawItem).map(InvoiceLine::amount).reduce(new BigDecimal(0), BigDecimal::add);
    }

    public BigDecimal amountVat() {
        return items.stream().filter(InvoiceLine::isRawItem).map(InvoiceLine::amount)
                .reduce(new BigDecimal(0), (a, b) -> (a.add(b.multiply(vatRate))));
    }

    public BigDecimal amountWithVat() {
        return amountWithoutVat().add(amountVat());
    }

    public LegalEntity invoicingEntity() {
        return invoicingEntity;
    }

    public void invoicingEntity(LegalEntity invoicingEntity) {
        this.invoicingEntity = invoicingEntity;
    }

    public BankConnection invoicingConnection() {
        return invoicingConnection;
    }

    public void invoicingConnection(BankConnection invoicingConnection) {
        this.invoicingConnection = invoicingConnection;
    }

    public String contractId() {
        return contractId;
    }

    public void contractId(String contractId) {
        this.contractId = contractId;
    }

    public LegalEntity invoicedEntity() {
        return invoicedEntity;
    }

    public void invoicedEntity(LegalEntity invoicedEntity) {
        this.invoicedEntity = invoicedEntity;
    }

    public LocalDate invoicedDate() {
        return invoicedDate;
    }

    public void invoicedDate(LocalDate invoicedDate) {
        this.invoicedDate = invoicedDate;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    public void dueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Currency currency() {
        return currency;
    }

    public void currency(Currency currency) {
        this.currency = currency;
    }

    public String text() {
        return text;
    }

    public void text(String text) {
        this.text = text;
    }

    public String paymentConditions() {
        return paymentConditions;
    }

    public void paymentConditions(String paymentConditions) {
        this.paymentConditions = paymentConditions;
    }

    public BigDecimal vatRate() {
        return vatRate;
    }

    public void vatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public void add(InvoiceLine item) {
        items.add(item);
    }

    public List<InvoiceLine> items() {
        return Collections.unmodifiableList(items);
    }

    public InvoiceLine getAt(int position) {
        return items.stream().filter(o -> o.position() == position).findAny().orElse(null);
    }
}
