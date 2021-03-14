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

import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Handles the customer relationship management <i>CRM</i> subsystem entities. A realm provides access to the instances of the CRM abstractions. The major
 * abstractions are:
 * <ul>
 *     <li>Natural entities are persons</li>
 *     <li>Legal Entities are legal recognized organizations</li>
 *     <li>Employee defines a relation between a natural entity and a legal entity when a person has a contractual agreement with an organization</li>
 *     <li>Contract defines a legal contractual obligation between two legal entities</li>
 *     <li>Interaction defines an interaction between your company and a set of legal and natural entities</li>
 *     <li>Activity defines a specific event part of an interaction</li>
 *     <li>Subject defines a registered user of the application</li>
 *     <li>CRM tags defining an ontology in the business domain of customer relationships management</li>
 * </ul>
 */
public interface CrmRealm extends Realm {
    Provider<Lead> leads();

    Provider<NaturalEntity> naturalEntities();

    Provider<LegalEntity> legalEntities();

    Provider<Employee> employees();

    Provider<Contract> contracts();

    Provider<Interaction> interactions();

    Provider<Subject> subjects();


    /**
     * Returns all the past and present employees of a legal organization.
     *
     * @param entity entity which employees should be retrieved
     * @return the list of employees
     */
    default List<Employee> employeesFor(@NotNull LegalEntity entity) {
        return employees().items().stream().filter(o -> entity.oid() == o.organization().oid()).collect(Collectors.toList());
    }

    /**
     * Returns all the employee positions a person had and has.
     *
     * @param entity entity which employees should be retrieved
     * @return the list of employees
     */
    default List<Employee> employeesFor(@NotNull NaturalEntity entity) {
        return employees().items().stream().filter(o -> entity.oid() == o.person().oid()).collect(Collectors.toList());
    }

    default List<Contract> contractsFor(@NotNull LegalEntity entity) {
        return contracts().items().stream().filter(o -> entity.oid() == o.sellee().oid()).collect(Collectors.toList());
    }

    default List<Activity> collectActivities(@NotNull Predicate<Activity> predicate) {
        return interactions().items().stream().flatMap(o -> o.activities().stream()).filter(predicate).collect(Collectors.toList());
    }
}
