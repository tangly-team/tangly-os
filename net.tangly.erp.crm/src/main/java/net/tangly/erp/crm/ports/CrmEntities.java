/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.crm.ports;

import net.tangly.commons.generator.IdGenerator;
import net.tangly.commons.generator.LongIdGenerator;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderHasOid;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import net.tangly.erp.crm.domain.*;
import net.tangly.erp.crm.services.CrmRealm;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Defines the customer relationship management <i>CRM</i> subsystem. The major abstractions are
 * <ul>
 *     <li>Natural entities</li>
 *     <li>Legal Entities</li>
 *     <li>Employees - defines a relation between a natural entity and a legal entity</li>
 *     <li>Contract - defines a legal contractual obligation between two legal entities</li>
 *     <li>Opportunity - defines an opportunity between your company and a legal entity</li>
 *     <li>Activity - defines a specific event part of an opportunity</li>
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
        private final List<Opportunity> opportunities;
        private final List<Activity> activities;

        Data() {
            leads = new ArrayList<>();
            naturalEntities = new ArrayList<>();
            legalEntities = new ArrayList<>();
            employees = new ArrayList<>();
            contracts = new ArrayList<>();
            opportunities = new ArrayList<>();
            activities = new ArrayList<>();
        }
    }

    private static final long OID_SEQUENCE_START = 1000;
    private final Data data;
    private final Provider<Lead> leads;
    private final Provider<NaturalEntity> naturalEntities;
    private final Provider<LegalEntity> legalEntities;
    private final Provider<Employee> employees;
    private final Provider<Contract> contracts;
    private final Provider<Opportunity> interactions;
    private final Provider<Activity> activities;
    private final IdGenerator generator;
    private final EmbeddedStorageManager storageManager;

    public CrmEntities(@NotNull Path path) {
        this.data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        generator = generator();

        leads = ProviderPersistence.of(storageManager, data.leads);
        naturalEntities = ProviderHasOid.of(generator, storageManager, data.naturalEntities);
        legalEntities = ProviderHasOid.of(generator, storageManager, data.legalEntities);
        employees = ProviderHasOid.of(generator, storageManager, data.employees);
        contracts = ProviderHasOid.of(generator, storageManager, data.contracts);
        interactions = ProviderHasOid.of(generator, storageManager, data.opportunities);
        activities = ProviderPersistence.of(storageManager, data.activities);
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
        interactions = ProviderHasOid.of(generator, data.opportunities);
        activities = ProviderInMemory.of(data.activities);
    }

    public void storeRoot() {
        storageManager.storeRoot();
    }

    @Override
    public void close() {
        if (Objects.nonNull(storageManager)) {
            storageManager.close();
        }
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
    public Provider<Opportunity> opportunities() {
        return this.interactions;
    }

    @Override
    public Provider<Activity> activities() {
        return this.activities;
    }

    private IdGenerator generator() {
        long oidCounter = Realm.maxOid(data.naturalEntities);
        oidCounter = Math.max(oidCounter, Realm.maxOid(data.legalEntities));
        oidCounter = Math.max(oidCounter, Realm.maxOid(data.employees));
        oidCounter = Math.max(oidCounter, Realm.maxOid(data.contracts));
        oidCounter = Math.max(oidCounter, Realm.maxOid(data.opportunities));
        return new LongIdGenerator(Math.max(oidCounter, OID_SEQUENCE_START));
    }
}
