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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.CrmRealm;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.Interaction;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.bus.crm.Subject;
import net.tangly.core.HasOid;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

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
    static class Data {
        private List<NaturalEntity> naturalEntities;
        private List<LegalEntity> legalEntities;
        private List<Employee> employees;
        private List<Contract> contracts;
        private List<Interaction> interactions;
        private List<Subject> subjects;
        private long oidCounter;
        private Map<String, String> configuration;

        Data() {
            naturalEntities = new ArrayList<>();
            legalEntities = new ArrayList<>();
            employees = new ArrayList<>();
            contracts = new ArrayList<>();
            interactions = new ArrayList<>();
            subjects = new ArrayList<>();
            oidCounter = HasOid.UNDEFINED_OID;
            configuration = new HashMap<>();
        }
    }

    private final Data data;
    private final Provider<NaturalEntity> naturalEntities;
    private final Provider<LegalEntity> legalEntities;
    private final Provider<Employee> employees;
    private final Provider<Contract> contracts;
    private final Provider<Interaction> interactions;
    private final Provider<Subject> subjects;
    private final EmbeddedStorageManager storageManager;

    @Inject
    public CrmEntities(Path path) {
        this.data = new Data();
        storageManager = EmbeddedStorage.start(data, path);

        naturalEntities = new ProviderPersistence<>(storageManager, data.naturalEntities);
        legalEntities = new ProviderPersistence<>(storageManager, data.legalEntities);
        employees = new ProviderPersistence<>(storageManager, data.employees);
        contracts = new ProviderPersistence<>(storageManager, data.contracts);
        interactions = new ProviderPersistence<>(storageManager, data.interactions);
        subjects = new ProviderPersistence<>(storageManager, data.subjects);
    }

    public CrmEntities() {
        this.data = new Data();
        storageManager = null;
        naturalEntities = new ProviderInMemory<>(data.naturalEntities);
        legalEntities = new ProviderInMemory<>(data.legalEntities);
        employees = new ProviderInMemory<>(data.employees);
        contracts = new ProviderInMemory<>(data.contracts);
        interactions = new ProviderInMemory<>(data.interactions);
        subjects = new ProviderInMemory<>(data.subjects);
    }

    public void storeRoot() {
        storageManager.storeRoot();
    }

    public void shutdown() {
        storageManager.shutdown();
    }

    @Override
    public Provider<NaturalEntity> naturalEntities() {
        return this.naturalEntities;
    }

    @Override
    public Provider<LegalEntity> legalEntities() {
        return this.legalEntities;
    }

    @Override
    public Provider<Employee> employees() {
        return this.employees;
    }

    @Override
    public Provider<Contract> contracts() {
        return this.contracts;
    }

    @Override
    public Provider<Interaction> interactions() {
        return this.interactions;
    }

    @Override
    public Provider<Subject> subjects() {
        return this.subjects;
    }
}
