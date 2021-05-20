/*
 * Copyright 2006-2021 Marcel Baumann
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

import net.tangly.core.codes.Code;

/**
 * Defines the product categories we offer in our shop. Categories are handled differently in invoices and accounting. For example expenses are added at the end
 * of an invoice and do not carry any VAT tax because VAT was already paid when buying the expense related item like a transportation ticket.
 * <ul>
 *     <li>work is a regular product code for services</li>
 *     <li>material is a regular product code for goods</li>
 *     <li>guaranty is a service performed under guaranty and should not be invoiced to the customer</li>
 *     <li>licenses is a bought software product invoiced to the customer</li>
 *     <li>expenses are travel expenses invoiced to the customer one to one without additional VAT</li>
 * </ul>
 */
public enum ArticleCode implements Code {
    work, material, guaranty, licenses, expenses;

    @Override
    public int id() {
        return this.ordinal();
    }

    @Override
    public String code() {
        return this.name();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
