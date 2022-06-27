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

package net.tangly.erp.crm.domain;

import net.tangly.core.NamedEntityImp;
import net.tangly.core.crm.CrmEntity;
import net.tangly.core.crm.LegalEntity;
import net.tangly.core.crm.NaturalEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Defines an employee as a temporal work contract between a natural entity meaning a person and a legal entity meaning an organization or a company. The name
 * property of the employee is the name property of the natural person of this employee. The from and to date defines the duration of the employment . if the to
 * date is empty the employee is still legally working for the organization.
 */
public class Employee extends NamedEntityImp implements CrmEntity {
    private NaturalEntity person;
    private LegalEntity organization;

    public Employee() {
    }

    @Override
    public String name() {
        String personName = (person != null) ? person.lastname() + ", " + person.firstname() : "UNKNOWN";
        String organizationName = (organization != null) ? organization.name() : "UNKNOWN";
        return personName + " - " + organizationName + " : " + from();
    }

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
    public boolean check() {
        return Objects.nonNull(person) && Objects.nonNull(organization);
    }

    @Override
    public String toString() {
        return """
            Employee[oid=%s, fromDate=%s, toDate=%s, text=%s, person=%s, organization=%s, tags=%s]
            """.formatted(oid(), from(), to(), text(), person(), organization(), tags());
    }
}
