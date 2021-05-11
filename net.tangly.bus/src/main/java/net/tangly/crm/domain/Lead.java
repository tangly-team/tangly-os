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

package net.tangly.crm.domain;

import java.time.LocalDate;

import net.tangly.core.EmailAddress;
import net.tangly.core.HasDate;
import net.tangly.core.PhoneNr;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a lead in the customer relation domain. A lead is also often called a prospect.
 */
public record Lead(@NotNull LocalDate date, @NotNull LeadCode code, String firstname, @NotNull String lastname, GenderCode gender, String company,
                   PhoneNr phoneNr, EmailAddress email, String linkedIn, @NotNull ActivityCode activity, String text) implements HasDate {
}
