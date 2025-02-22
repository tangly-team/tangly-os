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

package net.tangly.erp.ledger.domain;

import net.tangly.core.TagType;
import net.tangly.core.TypeRegistry;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

/**
 * The tag ontology of the ledger bounded domain is defined here.
 * <dl>
 *     <dt>fin:expected-date</dt><dd>expected date when a payment is performed and the transaction can be booked. This information is useful for creditors
 *     and debtors accounting in the ledger domain.</dd>
 *     <dt>fin:segment</dt><dd>the project to which the transaction belongs. The information is useful for profit centers and segments consolidation.</dd>
 *     <dt>fin:customer</dt><dd>Segmentation of account entries per customer for enhanced balance sheet reporting.</dd>
 *     <dt>fin:collaborator</dt><dd>Segmentation of account entries per collaborator for enhanced balance sheet reporting.</dd>
 *     <dt>fin:location</dt><dd>Segmentation of account entries per location for enhanced balance sheet reporting.</dd>
 * </dl>
 */
public final class LedgerTags {
    public static final String EXPECTED_DATE = "expected-date";

    private LedgerTags() {
    }

    public static void registerTags(@NotNull TypeRegistry registry) {
        registry.register(TagType.ofMandatory(AccountEntry.FINANCE, EXPECTED_DATE, LocalDate.class, LocalDate::parse));
        registry.register(TagType.ofMandatoryString(AccountEntry.FINANCE, AccountEntry.PROJECT));
        registry.register(TagType.ofMandatoryString(AccountEntry.FINANCE, AccountEntry.SEGMENT));
    }
}
