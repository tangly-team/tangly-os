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

import net.tangly.core.Address;
import net.tangly.core.EmailAddress;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasText;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a legal entity addressed in an invoice. The entity allows to address an invoice to a recipient per post mail or per electronic mail. Invoicing
 * workflows are more and more digitalized and requires the exchange of information and documents over the electronic way.
 *
 * @param id      identifier of the legal entity as defined the register of legal organization of the related geographical entity
 * @param name    name of the entity as human-readable name
 * @param vatNr   VAT number of the legal entitiy if defined
 * @param address legal address of the entity
 * @param email   email defining the target point to send electronically information to the legal entity
 */
public record InvoiceLegalEntity(@NotNull String id, String name, String vatNr, Address address, EmailAddress email) implements HasId, HasName {
}
