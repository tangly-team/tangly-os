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

package net.tangly.erpr.crm.ports;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import net.tangly.commons.generator.IdGenerator;
import net.tangly.commons.generator.LongIdGenerator;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderHasOid;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import net.tangly.erp.crm.domain.Activity;
import net.tangly.erp.crm.domain.Contract;
import net.tangly.erp.crm.domain.Employee;
import net.tangly.erp.crm.domain.Interaction;
import net.tangly.erp.crm.domain.Lead;
import net.tangly.erp.crm.domain.LegalEntity;
import net.tangly.erp.crm.domain.NaturalEntity;
import net.tangly.erp.crm.domain.Subject;
import net.tangly.erp.crm.services.CrmRealm;
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
    static class Data {
        private final List<Lead> leads;
        private final List<NaturalEntity> naturalEntities;
        private final List<LegalEntity> legalEntities;
        private final List<Employee> employees;
        private final List<Contract> contracts;
        private final List<Interaction> interactions;
        private final List<Activity> activities;
        private final List<Subject> subjects;

        Data() {
            leads = new ArrayList<>();
            naturalEntities = new ArrayList<>();
            legalEntities = new ArrayList<>();
            employees = new ArrayList<>();
            contracts = new ArrayList<>();
            interactions = new ArrayList<>();
            activities = new ArrayList<>();
            subjects = new ArrayList<>();
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
    private final Provider<Activity> activities;
    private final Provider<Subject> subjects;
    private final IdGenerator generator;
    private final EmbeddedStorageManager storageManager;

    @Inject
    public CrmEntities(@NotNull Path path) {
        this.data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        generator = generator();

        leads = ProviderPersistence.of(storageManager, data.leads);
        naturalEntities = ProviderHasOid.of(generator, storageManager, data.naturalEntities);
        legalEntities = ProviderHasOid.of(generator, storageManager, data.legalEntities);
        employees = ProviderHasOid.of(generator, storageManager, data.employees);
        contracts = ProviderHasOid.of(generator, storageManager, data.contracts);
        interactions = ProviderHasOid.of(generator, storageManager, data.interactions);
        activities = ProviderPersistence.of(storageManager, data.activities);
        subjects = ProviderHasOid.of(generator, storageManager, data.subjects);
    }

    public CrmEntities() {
        this.data = new Data();
        storageManager = null;
        generator = new LongIdGenerator(OID_SEQUENCE_START);
        leads = ProviderInMemory.of(data.leads);
        naturalEntities = ProviderHasOid.of(generator, data.naturalEntities);
        legalEntities = ProviderHasOid.of(generator, data.legalEntities);
        employees = ProviderHasOid.of(generator, data.employees);
        contracts = ProviderHasOid.of(generator, data.contracts);
        interactions = ProviderHasOid.of(generator, data.interactions);
        activities = ProviderInMemory.of(data.activities);
        subjects = ProviderHasOid.of(generator, data.subjects);
    }

    public void storeRoot() {
        storageManager.storeRoot();
    }

    public void shutdown() {
        storageManager.shutdown();
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
    public Provider<Activity> activities() {
        return this.activities;
    }

    @Override
    public Provider<Subject> subjects() {
        return this.subjects;
    }

    @Override
    public void close() {
        storageManager.close();
    }

    private IdGenerator generator() {
        long oidCounter = Realm.maxOid(data.naturalEntities);
        oidCounter = Math.max(oidCounter, Realm.maxOid(data.legalEntities));
        oidCounter = Math.max(oidCounter, Realm.maxOid(data.employees));
        oidCounter = Math.max(oidCounter, Realm.maxOid(data.contracts));
        oidCounter = Math.max(oidCounter, Realm.maxOid(data.interactions));
        oidCounter = Math.max(oidCounter, Realm.maxOid(data.subjects));
        return new LongIdGenerator(Math.max(oidCounter, OID_SEQUENCE_START));
    }
}
