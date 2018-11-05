/*
 * Copyright 2006-2018 Marcel Baumann
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

package net.tangly.erp.crm;

import net.tangly.commons.models.Address;
import net.tangly.commons.models.Entity;
import net.tangly.commons.models.EntityImp;

import java.math.BigDecimal;

/**
 * A legal contract between two parties, a seller and a sellee.
 */
public class Contract extends EntityImp {

    private Address address;

    private BankConnection bankConnection;

    private BigDecimal amountWithoutVat;

    private LegalEntity seller;

    private LegalEntity sellee;

    public Contract(long oid, String id) {
        super(oid, id);
    }

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
}
