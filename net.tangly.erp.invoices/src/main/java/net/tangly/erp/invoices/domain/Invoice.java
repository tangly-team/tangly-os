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

package net.tangly.erp.invoices.domain;

import net.tangly.commons.utilities.BigDecimalUtilities;
import net.tangly.core.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * <p>The abstraction of an invoice with a set of positions, subtotals, and a total. The items and the subtotals have a position
 * to order them in the invoice to provide human readable outputs. An invoice and its components have no dependencies to external entities. Therefore, an invoice is complete and
 * can be archived. For example, you can change the VAT percentage or a product price without any consequence on existing invoices.</p>
 * <p>The invoice assumes that a VAT rate applies to a given invoice item. The rate is dependent on the service sold and the customer.
 * For example educational services are VAT free in Switzerland. </p>
 * <p></p>Each item line has an associated article and a VAT rate. A subtotal does not have an article or a VAT rate. This assumption is reasonable for quite a lot of
 * businesses, in particular in the service industry.</p>
 * <p>Often an invoice references only one VAT rate, convenience methods are provided to streamline this scenario.</p>
 */
public class Invoice implements HasMutableId, HasMutableName, HasMutableDate, HasMutableText {
    /**
     * The identifier is the unique external identifier of the invoice used in the accounting, banking, and tracking systems.
     */
    private String id;

    /**
     * The name is a human-readable identifier of the invoice. The name is also used as a file name if the invoice is stored in a JSON file or an artifact is generated. Humans can
     * associate the invoice with the various artifacts generated out of it.
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
     * Currency of the invoice. We do not support multi-currency invoices. Please create one invoice for each currency. The decision is logical due to the complexities of handling
     *  value-added taxes and exchange rates.
     */
    private Currency currency;

    /**
     * Locale of the invoice. It is used for the localization of the invoice text and the selection of the most accurate template.
     */
    private Locale locale;

    /**
     * Human-readable text describing the payment conditions of the invoice.
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
     * Returns the amount of the invoice without the VAT tax. The amount is the sum of all invoice items. Subtotals are not considered in the amount.
     *
     * @return invoice amount without VAT tax
     */
    public BigDecimal amountWithoutVat() {
        return items().stream().filter(InvoiceLine::isItem).map(InvoiceLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
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
     * Returns the amount of the invoice including VAT tax.
     *
     * @return invoice amount with VAT tax
     */
    public BigDecimal amountWithVat() {
        return BigDecimalUtilities.roundToFiveCents(amountWithoutVat().add(vat()).setScale(2, RoundingMode.HALF_EVEN));
    }

    /**
     * Returns a map of VAT rates and associated VAT amounts for the whole invoice. An invoice line has a VAT rate and a computed VAT amount. A subtotal does
     * not have a VAT rate but
     * has an aggregated VAT amount
     *
     * @return map of entries VAT rate and associated VAT amounts
     */
    public Map<BigDecimal, BigDecimal> vatAmounts() {
        Map<BigDecimal, BigDecimal> vatAmounts = new TreeMap<>();
        items().stream().filter(InvoiceItem::isItem)
            .forEach(o -> vatAmounts.put(o.vatRate(), vatAmounts.getOrDefault(o.vatRate(), BigDecimal.ZERO).add(o.vat().setScale(2, RoundingMode.HALF_EVEN))));
        return vatAmounts;
    }

    /**
     * Return true if the invoice has multiple VAT rates different from 0%.
     *
     * @return flag if the invoice has multiple VAT rates
     */
    public boolean hasMultipleVatRates() {
        return this.items().stream().filter(InvoiceItem::isItem).map(InvoiceItem::vatRate).distinct().count() > 1;
    }

    /**
     * Return the unique VAT rate if defined otherwise empty optional. It is a convenience method to support service companies having exactly one VAT rate for all their products.
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
            Objects.nonNull(invoicingConnection()) && Objects.nonNull(currency) && name().startsWith(id()) &&
            items().stream().noneMatch(o -> Objects.isNull(o.article()));
    }

    @Override
    public String toString() {
        return """
            Invoice[id=%s, name=%s, text=%s, invoicingEntity=%s, invoicedEntity=%s, invoicingConnection=%s, contractId=%s, deliveryDate=%s, invoicedDate=%s, dueDate=%s, currency=%s, locale=%s, paymentConditions=%s, items=%s]
            """.formatted(id(), name(), text(), invoicingEntity(), invoicedEntity(), invoicingConnection(), contractId(), deliveryDate(), date(), dueDate(),
            currency(), locale(), paymentConditions(), items());
    }
}
