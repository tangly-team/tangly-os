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
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

import net.tangly.bus.core.Address;
import net.tangly.bus.core.EntityImp;

/**
 * A legal contract between two parties, one being the seller and one being the sellee. The properties of the entity defines the identifiers of the contract and
 * the time interval.
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
public class Contract extends EntityImp implements CrmEntity {
    private static final long serialVersionUID = 1L;
    private Address address;
    private BankConnection bankConnection;
    private BigDecimal amountWithoutVat;
    private LegalEntity seller;
    private LegalEntity sellee;
    private Locale locale;
    private Currency currency;

    public Address address() {
        return address;
    }

    public void address(Address address) {
        this.address = address;
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

    public boolean isValid() {
        return Objects.nonNull(bankConnection) && Objects.nonNull(seller) && Objects.nonNull(sellee) &&
                (Objects.requireNonNull(amountWithoutVat).compareTo(BigDecimal.ZERO) > 0) && Objects.nonNull(currency);
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Contract[oid=%s, id=%s, name=%s, fromDate=%s, toDate=%s, text=%s, locale=%s, currency=%s, address=%s, bankConnection=%s, " +
                        "amountWithoutVat=%s, seller=%s, sellee=%s, tags=%s]", oid(), id(), name(), fromDate(), toDate(), text(), locale(), currency(),
                address(), bankConnection(), amountWithoutVat(), seller(), sellee(), tags());
    }
}
