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

package net.tangly.crm.ports;

import javax.inject.Inject;

import net.tangly.core.TagTypeRegistry;
import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.CrmRealm;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.Interaction;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.bus.crm.Subject;
import net.tangly.bus.providers.InstanceProvider;
import net.tangly.bus.providers.InstanceProviderInMemory;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the customer relationship management <i>CRM</i> subsystem. The major abstractions are
 * <ul>
 *     <li>Natural entities</li>
 *     <li>Legal Entities</li>
 *     <li>Employees - defines a relation between a natural entity and a legal entity</li>
 *     <li>Contract - defines a legal contractual obligation between two legal entities</li>
 *     <li>Interaction - defines an interaction between your company and a set of legal and natural entities</li>
 *     <li>Activity - defines a specific event part of an interaction</li>
 *     <li>Subject - defines a registered user of the application</li>
 *     <li>CRM tags - defining an ontology in the business domain of customer relationships management</li>
 * </ul>
 * <p>The class is also the connection point between the CRM domain model and other ones. One such external domain is the invoices domain.</p>
 */
public class CrmEntities implements CrmRealm {
    private final InstanceProvider<NaturalEntity> naturalEntities;
    private final InstanceProvider<LegalEntity> legalEntities;
    private final InstanceProvider<Employee> employees;
    private final InstanceProvider<Contract> contracts;
    private final InstanceProvider<Interaction> interactions;
    private final InstanceProvider<Subject> subjects;
    private final TagTypeRegistry registry;

    @Inject
    public CrmEntities(@NotNull TagTypeRegistry registry) {
        naturalEntities = new InstanceProviderInMemory<>();
        legalEntities = new InstanceProviderInMemory<>();
        employees = new InstanceProviderInMemory<>();
        contracts = new InstanceProviderInMemory<>();
        interactions = new InstanceProviderInMemory<>();
        subjects = new InstanceProviderInMemory<>();
        this.registry = registry;
    }

    @Override
    public TagTypeRegistry tagTypeRegistry() {
        return registry;
    }

    @Override
    public InstanceProvider<NaturalEntity> naturalEntities() {
        return this.naturalEntities;
    }

    @Override
    public InstanceProvider<LegalEntity> legalEntities() {
        return this.legalEntities;
    }

    @Override
    public InstanceProvider<Employee> employees() {
        return this.employees;
    }

    @Override
    public InstanceProvider<Contract> contracts() {
        return this.contracts;
    }

    @Override
    public InstanceProvider<Interaction> interactions() {
        return this.interactions;
    }

    @Override
    public InstanceProvider<Subject> subjects() {
        return this.subjects;
    }
}
