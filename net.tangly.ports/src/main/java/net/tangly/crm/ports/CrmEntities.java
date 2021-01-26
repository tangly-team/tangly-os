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
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;

import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.CrmRealm;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.Interaction;
import net.tangly.bus.crm.Lead;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.bus.crm.Subject;
import net.tangly.commons.generator.IdGenerator;
import net.tangly.core.HasOid;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
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
    static class Data implements IdGenerator {
        private final List<Lead> leads;
        private final List<NaturalEntity> naturalEntities;
        private final List<LegalEntity> legalEntities;
        private final List<Employee> employees;
        private final List<Contract> contracts;
        private final List<Interaction> interactions;
        private final List<Subject> subjects;
        private long oidCounter;
        private transient final ReentrantLock lock;

        Data() {
            leads = new ArrayList<>();
            naturalEntities = new ArrayList<>();
            legalEntities = new ArrayList<>();
            employees = new ArrayList<>();
            contracts = new ArrayList<>();
            interactions = new ArrayList<>();
            subjects = new ArrayList<>();
            oidCounter = HasOid.UNDEFINED_OID;
            this.lock = new ReentrantLock();
        }

        @Override
        public long id() {
            lock.lock();
            try {
                return oidCounter++;
            } finally {
                lock.unlock();
            }
        }
    }

    private static final long OID_SEQUENCE_START = 1000;
    private final Data data;
    private final Provider<Lead> leads;
    private final Provider<NaturalEntity> naturalEntities;
    private final Provider<LegalEntity> legalEntities;
    private final Provider<Employee> employees;
    private final Provider<Contract> contracts;
    private final Provider<Interaction> interactions;
    private final Provider<Subject> subjects;
    private final EmbeddedStorageManager storageManager;

    @Inject
    public CrmEntities(@NotNull Path path) {
        this.data = new Data();
        storageManager = EmbeddedStorage.start(data, path);

        leads = new ProviderPersistence<>(storageManager, data.leads);
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
        leads = new ProviderInMemory<>(data.leads);
        naturalEntities = new ProviderInMemory<>(data.naturalEntities);
        legalEntities = new ProviderInMemory<>(data.legalEntities);
        employees = new ProviderInMemory<>(data.employees);
        contracts = new ProviderInMemory<>(data.contracts);
        interactions = new ProviderInMemory<>(data.interactions);
        subjects = new ProviderInMemory<>(data.subjects);

        long oidCounter = Realm.maxOid(naturalEntities());
        oidCounter = Math.max(oidCounter, Realm.maxOid(legalEntities()));
        oidCounter = Math.max(oidCounter, Realm.maxOid(employees()));
        oidCounter = Math.max(oidCounter, Realm.maxOid(contracts()));
        oidCounter = Math.max(oidCounter, Realm.maxOid(interactions()));
        oidCounter = Math.max(oidCounter, Realm.maxOid(subjects()));
        data.oidCounter = Math.max(oidCounter, OID_SEQUENCE_START);
    }

    public void storeRoot() {
        storageManager.storeRoot();
    }

    public void shutdown() {
        storageManager.shutdown();
    }

    @Override
    public <T extends HasOid> T registerOid(@NotNull T entity) {
        Realm.setOid(entity, data.id());
        storeRoot();
        return entity;
    }

    @Override
    public Provider<Lead> leads() {
        return this.leads;
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

    @Override
    public void close() {
        storageManager.close();
    }
}
