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

package net.tangly.bus.ledger;

import java.util.Map;
import javax.inject.Inject;

import net.tangly.core.TagType;
import net.tangly.core.TagTypeRegistry;
import net.tangly.core.app.BoundedDomain;
import org.jetbrains.annotations.NotNull;

public class LedgerBoundedDomain extends BoundedDomain<LedgerRealm, LedgerBusinessLogic, LedgerHandler, LedgerPort> {
    public static final String LEDGER_OID_VALUE = "ledger-oid-value";

    @Inject
    public LedgerBoundedDomain(LedgerRealm realm, LedgerBusinessLogic logic, LedgerHandler handler, LedgerPort port, TagTypeRegistry registry,
                               @NotNull Map<String, String> configuration) {
        super(realm, logic, handler, port, registry, configuration);
    }

    @Override
    protected void initialize() {
        LedgerTags.registerTags(registry());
    }

    @Override
    public Map<TagType<?>, Integer> countTags(@NotNull Map<TagType<?>, Integer> counts) {
        realm().accounts().items().stream().map(Account::entries).forEach(o -> addTagCounts(registry(), o, counts));
        return counts;
    }
}
