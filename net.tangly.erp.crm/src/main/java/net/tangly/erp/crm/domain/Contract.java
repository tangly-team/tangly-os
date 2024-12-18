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

package net.tangly.erp.crm.domain;


import net.tangly.core.Address;
import net.tangly.core.BankConnection;
import net.tangly.core.MutableEntityExtended;
import net.tangly.core.MutableEntityExtendedImp;
import net.tangly.erp.crm.events.ContractSignedEvent;
import org.javamoney.moneta.FastMoney;
import org.jetbrains.annotations.NotNull;

import javax.money.Monetary;
import java.math.BigDecimal;
import java.util.*;

/**
 * A legal contract between two parties, one being the seller, and one being the sellee. The properties of the entity define the identifiers of the contract and the time interval.
 * <ul>
 *   <li>correspondence address used in mail exchange and invoicing</li>
 *   <li>bank account used for invoicing</li>
 *   <li>amount (without VAT or expenses) of the whole contract</li>
 *   <li>seller of the contract</li>
 *   <li>selle of the contract</li>
 *   <li>The locale of the contract defines the language used to generate invoice documents.</li>
 *   <li>The currency of the contract amount and the one used in the invoices</li>
 * </ul>
 */
public class Contract extends MutableEntityExtendedImp implements MutableEntityExtended, CrmEntity {
    private BankConnection bankConnection;
    private BigDecimal amountWithoutVat;
    private BigDecimal budgetInHours;
    private LegalEntity seller;
    private LegalEntity sellee;
    private Locale locale;
    private Currency currency;
    private final List<ContractExtension> contractExtensions;

    public Contract(long oid) {
        super(oid);
        contractExtensions = new ArrayList<>();
    }

    public static ContractSignedEvent of(Contract contract) {
        var amountWithoutVat = FastMoney.of(contract.amountWithoutVat(), Monetary.getCurrency(contract.currency().getCurrencyCode()));
        return new ContractSignedEvent(contract.id(), null, contract.range(), contract.locale(), amountWithoutVat, contract.budgetInHours());
    }

    @Override
    public Optional<Address> address() {
        return address(VcardType.work);
    }

    public void address(Address address) {
        address(VcardType.work, address);
    }

    public BankConnection bankConnection() {
        return bankConnection;
    }

    public void bankConnection(BankConnection bankConnection) {
        this.bankConnection = bankConnection;
    }

    public BigDecimal amountWithoutVat() {
        return amountWithoutVat;
    }

    public BigDecimal totalAmountWithoutVat() {
        return contractExtensions.stream().map(ContractExtension::amountWithoutVat).reduce(amountWithoutVat(), BigDecimal::add);
    }

    public BigDecimal budgetInHours() {
        return budgetInHours;
    }

    public void budgetInHours(BigDecimal budgetInHours) {
        this.budgetInHours = budgetInHours;
    }

    public void amountWithoutVat(BigDecimal amountWithoutVat) {
        this.amountWithoutVat = amountWithoutVat;
    }

    public LegalEntity seller() {
        return seller;
    }

    public void seller(LegalEntity seller) {
        this.seller = seller;
    }

    public LegalEntity sellee() {
        return sellee;
    }

    public void sellee(LegalEntity sellee) {
        this.sellee = sellee;
    }

    public Locale locale() {
        return locale;
    }

    public void locale(Locale locale) {
        this.locale = locale;
    }

    public Currency currency() {
        return currency;
    }

    public void currency(Currency currency) {
        this.currency = currency;
    }

    public void add(ContractExtension extension) {
        contractExtensions.add(extension);
    }

    public void remove(ContractExtension extension) {
        contractExtensions.remove(extension);
    }

    public List<ContractExtension> contractExtensions() {
        return Collections.unmodifiableList(contractExtensions);
    }

    public void contractExtensions(@NotNull Iterable<ContractExtension> extensions) {
        contractExtensions.clear();
        extensions.forEach(contractExtensions::add);
    }

    @Override
    public boolean validate() {
        return Objects.nonNull(range()) && Objects.nonNull(bankConnection()) && Objects.nonNull(seller()) && Objects.nonNull(sellee()) &&
            (Objects.requireNonNull(amountWithoutVat()).compareTo(BigDecimal.ZERO) > 0) && Objects.nonNull(currency()) && Objects.nonNull(locale());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Contract o) && super.equals(o) && Objects.equals(address(), o.address()) &&
            Objects.equals(bankConnection(), o.bankConnection()) &&
            Objects.equals(amountWithoutVat(), o.amountWithoutVat()) && Objects.equals(seller(), o.seller()) && Objects.equals(sellee(), o.sellee()) &&
            Objects.equals(locale(), o.locale()) && Objects.equals(currency(), o.currency());
    }

    @Override
    public String toString() {
        return """
            Contract[oid=%s, id=%s, name=%s, fromDate=%s, toDate=%s, text=%s, locale=%s, currency=%s, address=%s, bankConnection=%s, amountWithoutVat=%s, seller=%s, sellee=%s, tags=%s]
            """.formatted(oid(), id(), name(), from(), to(), text(), locale(), currency(), address(), bankConnection(), amountWithoutVat(), seller(), sellee(),
            tags());
    }
}
