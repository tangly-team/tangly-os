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

import java.util.Objects;

import net.tangly.core.EntityImp;
import net.tangly.core.HasName;
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

    public Employee() {
    }

    public String name() {
        return inferName(person) + " - " + inferName(organization) + " : " + fromDate();
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
    public boolean isValid() {
        return Objects.nonNull(person) && Objects.nonNull(organization);
    }

    @Override
    public String toString() {
        return """
                Employee[oid=%s, fromDate=%s, toDate=%s, text=%s, person=%s, organization=%s, tags=%s]
                """.formatted(oid(), fromDate(), toDate(), text(), person(), organization(), tags());
    }

    private static String inferName(HasName entity) {
        return (entity != null) ? entity.name() : "UNKNOWN";
    }
}
