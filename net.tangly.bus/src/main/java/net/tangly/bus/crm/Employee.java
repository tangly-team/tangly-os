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
import java.util.Locale;
import java.util.Objects;

import net.tangly.bus.core.EntityImp;
import org.jetbrains.annotations.NotNull;

/**
 * Defines an employee as a temporal work contract between a natural entity meaning a person and a legal entity meaning an organization or a company. The name
 * property of the employee is the name property of the natural person of this employee. The from and to date defines the duration of the employment . if the to
 * date is empty the employee is still legally working for the organization.
 */
public class Employee extends EntityImp implements CrmEntity {
    private static final long serialVersionUID = 1L;

    private NaturalEntity person;
    private LegalEntity organization;

    public NaturalEntity person() {
        return person;
    }

    public void person(@NotNull NaturalEntity person) {
        this.person = person;
    }

    public LegalEntity organization() {
        return organization;
    }

    public void organization(@NotNull LegalEntity organization) {
        this.organization = organization;
    }

    @Override
    public String name() {
        return Objects.nonNull(person()) ? person().name() : null;
    }

    @Override
    public void name(String name) {
        if (Objects.nonNull(person)) {
            person().name(name);
        }
    }

    public boolean isValid() {
        return Objects.nonNull(person) && Objects.nonNull(organization);
    }
    @Override
    public String toString() {
        return String.format(Locale.US, "Employee[oid=%s, id=%s, name=%s, fromDate=%s, toDate=%s, text=%s, person=%s, organization=%s, tags=%s]", oid(), id(),
                name(),
                fromDate(), toDate(), text(), person(), organization(), tags());
    }
}
