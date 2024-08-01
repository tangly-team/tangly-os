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

import net.tangly.core.EmailAddress;
import net.tangly.core.GenderCode;
import net.tangly.core.HasDate;
import net.tangly.core.PhoneNr;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

/**
 * Defines a lead in the customer relation domain. A lead is also often called a prospect. Once a lead was contacted, it should either be qualified and
 * integrated into an opportunity or disqualified. Leads are persisted to keep track of the disqualified ones over time.
 * <p>The goal is to transform a lead into a qualified prospect. Therefore we currently only provide the text field to track history.
 * If a lot of activities are associated with a lead, you should probably qualify her and transform her into natural and legal entities.</p>
 *
 * @param date      date when the lead was created
 * @param code      code of the lead defining the qualification status
 * @param firstname first name of the lead
 * @param lastname  last name of the lead
 * @param gender    gender of the lead
 * @param company   name of the company the lead is associated with
 * @param phoneNr   phone number of the lead
 * @param email     email address of the lead
 * @param linkedIn  linkedIn profile of the lead
 * @param text      history of the lead
 * @
 */
public record Lead(@NotNull LocalDate date, @NotNull LeadCode code, String firstname, @NotNull String lastname, GenderCode gender, String company,
                   PhoneNr phoneNr, EmailAddress email, String linkedIn, String text) implements HasDate {
}
