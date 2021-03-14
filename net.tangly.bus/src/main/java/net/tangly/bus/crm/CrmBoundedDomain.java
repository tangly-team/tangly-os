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

import net.tangly.core.TagType;
import net.tangly.core.TypeRegistry;
import net.tangly.core.codes.CodeType;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class CrmBoundedDomain extends BoundedDomain<CrmRealm, CrmBusinessLogic, CrmHandler, CrmPort> {
    public static final String DOMAIN = "crm";

    @Inject
    public CrmBoundedDomain(CrmRealm realm, CrmBusinessLogic logic, CrmHandler handler, CrmPort port, TypeRegistry registry) {
        super(DOMAIN, realm, logic, handler, port, registry);
    }

    @Override
    protected void initialize() {
        CrmTags.registerTags(registry());
        registry().register(CodeType.of(ActivityCode.class));
        registry().register(CodeType.of(GenderCode.class));
        registry().register(CodeType.of(LeadCode.class));
        registry().register(CodeType.of(InteractionCode.class));
    }

    @Override
    public Map<TagType<?>, Integer> countTags(@NotNull Map<TagType<?>, Integer> counts) {
        addTagCounts(registry(), realm().naturalEntities(), counts);
        addTagCounts(registry(), realm().legalEntities(), counts);
        addTagCounts(registry(), realm().employees(), counts);
        addTagCounts(registry(), realm().interactions(), counts);
        addTagCounts(registry(), realm().subjects(), counts);
        return counts;
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>("crm", NaturalEntity.class, realm().naturalEntities()),
            new DomainEntity<>(DOMAIN, LegalEntity.class, realm().legalEntities()), new DomainEntity<>(DOMAIN, Employee.class, realm().employees()),
            new DomainEntity<>(DOMAIN, Interaction.class, realm().interactions()), new DomainEntity<>(DOMAIN, Contract.class, realm().contracts()),
            new DomainEntity<>(DOMAIN, Subject.class, realm().subjects()));
    }
}
