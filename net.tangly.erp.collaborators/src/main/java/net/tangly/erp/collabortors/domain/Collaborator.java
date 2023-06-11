/*
 * Copyright 2021-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.collabortors.domain;

import lombok.Builder;
import net.tangly.core.Address;
import net.tangly.core.HasId;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

/**
 * Defines the collaborator entity as needed from the perspective of the human resources department.
 * The new social security number can be used as an identifier in Switzerland.
 * The European Union has an european identifier for natural persons.
 * The address ownership is outside this bounded domain and is owned by the CRM bounded domain.
 *
 * @param id                      identifier is the social security number of the collaborator.
 * @param oldSocialSecurityNumber old social security number. It is an idiosyncrasy of Switzerland
 * @param birthday                birthday of the collaborator
 * @param fullname                fullname as displayed on an official document
 * @param address                 address of the collaborator
 */
@Builder
public record Collaborator(@NotNull String id,
                           String oldSocialSecurityNumber,
                           @NotNull LocalDate birthday,
                           @NotNull String fullname,
                           @NotNull Address address) implements HasId {
    public String socialSecurityNumber() {
        return id();
    }
}
