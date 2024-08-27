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

package net.tangly.erp.ledger.services;

import net.tangly.core.TagType;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.TenantDirectory;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.domain.AccountEntry;
import net.tangly.erp.ledger.domain.Transaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class LedgerBoundedDomain extends BoundedDomain<LedgerRealm, LedgerBusinessLogic, LedgerPort> {
    public static final String DOMAIN = "transactions";

    public LedgerBoundedDomain(LedgerRealm realm, LedgerBusinessLogic logic, LedgerPort port, TenantDirectory directory) {
        super(DOMAIN, realm, logic, port, directory);
        port.importConfiguration(this, registry());
    }

    @Override
    public Map<TagType<?>, Integer> countTags(@NotNull Map<TagType<?>, Integer> counts) {
        realm().accounts().items().stream().map(Account::entries).forEach(o -> addTagCounts(registry(), o, counts));
        return counts;
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, Account.class, realm().accounts()), new DomainEntity<>(DOMAIN, AccountEntry.class, realm().entries()),
            new DomainEntity<>(DOMAIN, Transaction.class, realm().transactions()));
    }
}
