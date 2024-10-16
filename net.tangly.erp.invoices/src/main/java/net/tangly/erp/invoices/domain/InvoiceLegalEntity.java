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

import net.tangly.core.Address;
import net.tangly.core.EmailAddress;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import org.jetbrains.annotations.NotNull;

/**
 * Define a legal entity referenced in an invoice.
 * <p>The entity allows addressing an invoice to a recipient per post-mail or per electronic mail. Invoicing workflow is more and more
 * digitalized and requires the exchange of information and documents over the electronic way.</p>
 * <p>Beware that legal entity identifier is a murky business. For example in Switzerland state companies do not always have a registered identifier.
 * The zefix platform offers a fuzzy search to find such organizations. European community has introduced an European identifier for all companies. Sadly they do not provide a
 * search platform to find out that the identifier of a company is.</p>
 *
 * @param id      identifier of the legal entity as defined the register of legal organization for the related geographical entity
 * @param name    name of the entity as a human-readable name
 * @param vatNr   VAT number of the legal entity if defined
 * @param address legal address of the entity
 * @param email   email defining the target point to send electronic information to the legal entity
 */
public record InvoiceLegalEntity(@NotNull String id, String name, String vatNr, @NotNull Address address, EmailAddress email) implements HasId, HasName {
}
