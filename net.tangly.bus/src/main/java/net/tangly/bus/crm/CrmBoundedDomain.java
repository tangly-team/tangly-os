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

import java.util.Map;
import javax.inject.Inject;

import net.tangly.core.TagType;
import net.tangly.core.TagTypeRegistry;
import net.tangly.core.app.BoundedDomain;
import org.jetbrains.annotations.NotNull;

public class CrmBoundedDomain extends BoundedDomain<CrmRealm, CrmBusinessLogic, CrmHandler, CrmPort> {
    public static final String CRM_OID_VALUE = "crm-oid-value";

    @Inject
    public CrmBoundedDomain(CrmRealm realm, CrmBusinessLogic logic, CrmHandler handler, CrmPort port, TagTypeRegistry registry,
                            @NotNull Map<String, String> configuration) {
        super(realm, logic, handler, port, registry, configuration);
    }

    @Override
    protected void initialize() {
        CrmTags.registerTags(registry());
    }

    @Override
    public Map<TagType<?>, Integer> countTags(@NotNull Map<TagType<?>, Integer> counts) {
        addTagCounts(registry(), realm().naturalEntities().items(), counts);
        addTagCounts(registry(), realm().legalEntities().items(), counts);
        addTagCounts(registry(), realm().employees().items(), counts);
        addTagCounts(registry(), realm().interactions().items(), counts);
        addTagCounts(registry(), realm().subjects().items(), counts);
        return counts;
    }
}
