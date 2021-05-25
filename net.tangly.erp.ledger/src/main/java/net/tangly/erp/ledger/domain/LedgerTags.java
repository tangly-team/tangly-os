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

package net.tangly.erp.ledger.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import net.tangly.core.TagType;
import net.tangly.core.TypeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * The tags ontology of the ledger bounded domain is defined here.
 * <dl>
 *     <dt>fin:expected-date</dt><dd>expected date when a payment is performed and the transaction can be booked. This information is useful for creditors
 *     and debtors accounting in the ledger domain.</dd>
 *     <dt>fin:vat</dt><dd>VAT rate added on an invoice and set to an transaction amount. The information is used for VAT due to government computation.</dd>
 *     <dt>fin:vat-due</dt><dd>VAT due to the government and set to an transaction amount. In Switzerland one VAT model defines the VAT rate for a
 *     transaction and the VAT due to the government to simplify VAT computation and payment. The information is used for VAT due to government computation.</dd>
 *     <dt>fin:segment</dt><dd>the project to which the transaction belongs. The information is useful for profit centers and segments consolidation.</dd>
 *     <dt>fin:customer</dt><dd>Segmentation of account entries per customer for enhanced balance sheet reporting.</dd>
 *     <dt>fin:collaborator</dt><dd>Segmentation of account entries per collaborator for enhanced balance sheet reporting.</dd>
 *     <dt>fin:location</dt><dd>Segmentation of account entries per location for enhanced balance sheet reporting.</dd>
 * </dl>
 */
public final class LedgerTags {
    public static final String LEDGER = "fin";
    public static final String VAT = "vat";
    public static final String VAT_DUE = "vat-due";
    public static final String CUSTOMER = "customer";
    public static final String COLLABORATOR = "collaborator";
    public static final String LOCATION = "location";
    public static final String EXPECTED_DATE = "expected-date";

    private LedgerTags() {
    }

    public static void registerTags(@NotNull TypeRegistry registry) {
        registry.register(TagType.ofMandatory(LEDGER, EXPECTED_DATE, LocalDate.class, LocalDate::parse));
        registry.register(TagType.ofMandatory(LEDGER, VAT, BigDecimal.class, BigDecimal::new));
        registry.register(TagType.ofMandatory(LEDGER, VAT_DUE, BigDecimal.class, BigDecimal::new));
        registry.register(TagType.ofMandatoryString(LEDGER, CUSTOMER));
        registry.register(TagType.ofMandatoryString(LEDGER, COLLABORATOR));
        registry.register(TagType.ofMandatoryString(LEDGER, LOCATION));
    }
}
