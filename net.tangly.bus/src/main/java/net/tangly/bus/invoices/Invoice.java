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
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import net.tangly.bus.crm.BankConnection;
import net.tangly.bus.crm.LegalEntity;

/**
 * <p>The abstraction of an invoice with a set of positions, subtotals, and a total. The items and the subtotals have a position
 * to order them in the invoice to provide human readable outputs. An invoice and its components have no dependencies to external entities. Therefore,
 * an invoice is complete and can be archived. For example, you can change the VAT percentage or a product price without any consequence on existing
 * invoices.</p>
 * <p>The invoice assumes that a VAT rate applies to a specific product associated with a given invoice item. This assumption is reasonable for
 * quite a lot of businesses, in particular in the service industry. Often an invoice references only one VAT rate, conveniance methods are provided
 * to streamline this scenario.</p>
 */
public class Invoice {
    private String id;
    private LegalEntity invoicingEntity;
    private BankConnection invoicingConnection;
    private String contractId;
    private LegalEntity invoicedEntity;
    private LocalDate deliveryDate;
    private LocalDate invoicedDate;
    private LocalDate dueDate;
    private Currency currency;
    private String text;
    private String paymentConditions;
    private final List<InvoiceLine> items;

    public Invoice() {
        items = new ArrayList<>();
    }

    public String id() {
        return id;
    }

    public void id(String id) {
        this.id = id;
    }

    // region VAT

    /**
     * Returns the amount of the invoice without VAT tax.
     *
     * @return invoice amount without VAT tax
     */
    public BigDecimal amountWithoutVat() {
        return items().stream().map(InvoiceLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns the VAT tax amount for the whole invoice.
     *
     * @return invoice VAT tax
     */
    public BigDecimal vat() {
        return items().stream().filter(InvoiceLine::isItem).map(InvoiceLine::vat).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Return the amount of the invoice including VAT tax.
     *
     * @return invoice amount with VAT tax
     */
    public BigDecimal amountWithVat() {
        return amountWithoutVat().add(vat());
    }

    /**
     * Return a map of VAT rates and associated VAT amounts for the whole invoice.
     *
     * @return map of entries VAT rate and associated VAT amounts
     */
    public Map<BigDecimal, BigDecimal> vatAmounts() {
        Map<BigDecimal, BigDecimal> vatAmounts = new TreeMap<>();
        this.items()
                .forEach(o -> vatAmounts.put(o.product().vatRate(), vatAmounts.getOrDefault(o.product().vatRate(), BigDecimal.ZERO).add(o.vat())));
        return vatAmounts;
    }

    /**
     * Return true if the invoice has multiple VAT rates different from 0%.
     *
     * @return flag if the invoice has multiple VAT rates
     */
    public boolean hasMultipleVatRates() {
        return vatAmounts().entrySet().stream().filter(o -> o.getValue().compareTo(BigDecimal.ZERO) != 0).count() > 1;
    }

    /**
     * Return the unique VAT rate if defined otherwise empty optional. It is a convenience method to support service companies having exactly one VAT
     * rate for all their products.
     *
     * @return unique VAT rate if defined
     */
    public Optional<BigDecimal> uniqueVatRate() {
        return hasMultipleVatRates() ? Optional.empty() : vatAmounts().keySet().stream().findAny();
    }

    // endregion

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

    public LocalDate deliveryDate() {
        return deliveryDate;
    }

    public void deliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
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

    public void add(InvoiceLine item) {
        items.add(item);
    }

    public List<InvoiceLine> positions() {
        return Collections.unmodifiableList(items);
    }

    public List<InvoiceItem> items() {
        return items.stream().filter(InvoiceLine::isItem).map(o -> (InvoiceItem) o).collect(Collectors.toUnmodifiableList());
    }

    public InvoiceLine getAt(int position) {
        return items.stream().filter(o -> o.position() == position).findAny().orElse(null);
    }
}
