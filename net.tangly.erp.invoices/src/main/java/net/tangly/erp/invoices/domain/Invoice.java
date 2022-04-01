/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.invoices.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import net.tangly.core.BankConnection;
import net.tangly.core.HasDate;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasText;

/**
 * <p>The abstraction of an invoice with a set of positions, subtotals, and a total. The items and the subtotals have a position
 * to order them in the invoice to provide human readable outputs. An invoice and its components have no dependencies to external entities. Therefore, an
 * invoice is complete and can be archived. For example, you can change the VAT percentage or a product price without any consequence on existing invoices.</p>
 * <p>The invoice assumes that a VAT rate applies to a specific product associated with a given invoice item. This assumption is reasonable for
 * quite a lot of businesses, in particular in the service industry. Often an invoice references only one VAT rate, convenience methods are provided to
 * streamline this scenario.</p>
 */
public class Invoice implements HasId, HasName, HasDate, HasText {
    /**
     * The identifier is the unique external identifier of the invoice used in the accounting, banking and tracking systems.
     */
    private String id;

    /**
     * The name is a human-readable identifier of the invoice. The name is also used as file name if the invoice is stored in a JSON file or an artifact is generated.
     * Humans can associate the invoice with the various artifacts generated out of it.
     */
    public String name;
    private String text;
    private InvoiceLegalEntity invoicingEntity;
    private InvoiceLegalEntity invoicedEntity;
    private String contractId;
    private BankConnection invoicingConnection;
    private LocalDate deliveryDate;

    /**
     * The date is the invoiced date of the invoice, meaning when the invoice was created.
     */
    private LocalDate date;

    /**
     * The date when the invoice should be paid by the invoiced party.
     */
    private LocalDate dueDate;

    /**
     * The date when the invoice was paid. This information can be used for debtor analysis.
     */
    private LocalDate paidDate;

    /**
     * Currency of the invoice. We do not support multi-currency invoices. Please create one invoice for each currency.
     * The decision is logical due to the complexities of handling value added taxes and exchange rates.
     */
    private Currency currency;

    /**
     * Locale of the invoice. It is used for the localization of the invoice text and the selection of the most accurate template.
     */
    private Locale locale;

    /**
     * Human-readable text describing the payment conditions of the infoice.
     */
    private String paymentConditions;
    private final List<InvoiceLine> items;

    public Invoice() {
        items = new ArrayList<>();
        currency = Currency.getInstance("CHF");
        locale = Locale.ENGLISH;
    }

    // region HasEditableQualifiers

    @Override
    public String id() {
        return id;
    }

    @Override
    public void id(String id) {
        this.id = id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }

    public String text() {
        return text;
    }

    @Override
    public void text(String text) {
        this.text = text;
    }

    // endregion

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
        this.items().forEach(o -> vatAmounts.put(o.article().vatRate(), vatAmounts.getOrDefault(o.article().vatRate(), BigDecimal.ZERO).add(o.vat())));
        return vatAmounts;
    }

    /**
     * Return true if the invoice has multiple VAT rates different from 0%.
     *
     * @return flag if the invoice has multiple VAT rates
     */
    public boolean hasMultipleVatRates() {
        return this.items().stream().filter(InvoiceItem::isItem).map(o -> o.article().vatRate()).distinct().count() > 1;
    }

    /**
     * Return the unique VAT rate if defined otherwise empty optional. It is a convenience method to support service companies having exactly one VAT rate for
     * all their products.
     *
     * @return unique VAT rate if defined
     */
    public Optional<BigDecimal> uniqueVatRate() {
        return hasMultipleVatRates() ? Optional.empty() : vatAmounts().keySet().stream().findAny();
    }

    public BigDecimal expenses() {
        return this.items().stream().filter(o -> ArticleCode.expenses == o.article().code()).map(InvoiceItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    // endregion

    public InvoiceLegalEntity invoicingEntity() {
        return invoicingEntity;
    }

    public void invoicingEntity(InvoiceLegalEntity invoicingEntity) {
        this.invoicingEntity = invoicingEntity;
    }

    public InvoiceLegalEntity invoicedEntity() {
        return invoicedEntity;
    }

    public void invoicedEntity(InvoiceLegalEntity invoicedEntity) {
        this.invoicedEntity = invoicedEntity;
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

    public LocalDate deliveryDate() {
        return deliveryDate;
    }

    public void deliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @Override
    public LocalDate date() {
        return date;
    }

    @Override
    public void date(LocalDate invoicedDate) {
        this.date = invoicedDate;
    }

    public LocalDate dueDate() {
        return dueDate;
    }

    public void dueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate paidDate() {
        return paidDate;
    }

    public void paidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    public Currency currency() {
        return currency;
    }

    public void currency(Currency currency) {
        this.currency = currency;
    }

    public Locale locale() {
        return locale;
    }

    public void locale(Locale locale) {
        this.locale = locale;
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

    /**
     * Returns all positions defined in the invoice either invoice items or subtotals.
     *
     * @return list of invoice lines
     */
    public List<InvoiceLine> lines() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Returns all invoice items defined in the invoice.
     *
     * @return list of invoice items
     */
    public List<InvoiceItem> items() {
        return items.stream().filter(InvoiceLine::isItem).map(InvoiceItem.class::cast).toList();
    }

    public InvoiceLine getAt(int position) {
        return items.stream().filter(o -> o.position() == position).findAny().orElse(null);
    }

    public boolean check() {
        return Objects.nonNull(contractId()) && Objects.nonNull(invoicingEntity()) && Objects.nonNull(invoicedEntity()) &&
            Objects.nonNull(invoicingConnection()) && Objects.nonNull(currency) && name().startsWith(id());
    }

    @Override
    public String toString() {
        return """
            Invoice[id=%s, name=%s, text=%s, invoicingEntity=%s, invoicedEntity=%s, invoicingConnection=%s, contractId=%s, deliveryDate=%s, invoicedDate=%s, dueDate=%s, currency=%s, locale=%s, paymentConditions=%s, items=%s]
            """.formatted(id(), name(), text(), invoicingEntity(), invoicedEntity(), invoicingConnection(), contractId(), deliveryDate(), date(), dueDate(),
            currency(), locale(), paymentConditions(), items());
    }
}
