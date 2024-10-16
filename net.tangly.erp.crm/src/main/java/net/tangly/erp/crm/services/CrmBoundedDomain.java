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

package net.tangly.erp.crm.services;

import net.tangly.core.GenderCode;
import net.tangly.core.TagType;
import net.tangly.core.codes.CodeType;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.TenantDirectory;
import net.tangly.erp.crm.domain.*;
import net.tangly.erp.crm.ports.CrmAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class CrmBoundedDomain extends BoundedDomain<CrmRealm, CrmBusinessLogic, CrmAdapter> {
    public static final String DOMAIN = "customers";

    public CrmBoundedDomain(CrmRealm realm, CrmBusinessLogic logic, CrmAdapter port, TenantDirectory directory) {
        super(DOMAIN, realm, logic, port, directory);
        CrmTags.registerTags(registry());
        registry().register(CodeType.of(ActivityCode.class));
        registry().register(CodeType.of(GenderCode.class));
        registry().register(CodeType.of(LeadCode.class));
        registry().register(CodeType.of(OpportunityCode.class));
    }

    @Override
    public Map<TagType<?>, Integer> countTags(@NotNull Map<TagType<?>, Integer> counts) {
        addTagCounts(registry(), realm().naturalEntities(), counts);
        addTagCounts(registry(), realm().legalEntities(), counts);
        addTagCounts(registry(), realm().employees(), counts);
        addTagCounts(registry(), realm().opportunities(), counts);
        return counts;
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, Lead.class, realm().leads()), new DomainEntity<>(DOMAIN, NaturalEntity.class, realm().naturalEntities()),
            new DomainEntity<>(DOMAIN, LegalEntity.class, realm().legalEntities()), new DomainEntity<>(DOMAIN, Employee.class, realm().employees()),
            new DomainEntity<>(DOMAIN, Opportunity.class, realm().opportunities()), new DomainEntity<>(DOMAIN, Contract.class, realm().contracts()),
            new DomainEntity<>(DOMAIN, Activity.class, realm().activities()));
    }
}
