/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.erp.products.services;

import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.Operation;
import net.tangly.core.events.EntityChangedInternalEvent;
import net.tangly.core.providers.Provider;
import net.tangly.erp.crm.events.ContractSignedEvent;
import net.tangly.erp.products.domain.WorkContract;
import org.jetbrains.annotations.NotNull;

public class CrmEventsProcessor implements BoundedDomain.EventListener {
    ProductsBoundedDomain domain;

    public CrmEventsProcessor(@NotNull ProductsBoundedDomain domain) {
        this.domain = domain;
    }

    @Override
    public void onNext(@NotNull Object event) {
        if (event instanceof ContractSignedEvent contractSignedEvent) {
            Provider.findById(domain.realm().contracts(), contractSignedEvent.id()).ifPresentOrElse(
                _ -> {
                },
                () -> {
                    var workContract = new WorkContract(contractSignedEvent.id(), contractSignedEvent.mainContractId(),
                        contractSignedEvent.range(), contractSignedEvent.locale(), contractSignedEvent.budgetInHours().intValue());
                    domain.realm().contracts().update(workContract);
                    domain.internalChannel().submit(new EntityChangedInternalEvent(ProductsBoundedDomain.DOMAIN, WorkContract.class.getSimpleName(),
                        Operation.CREATE));
                });

        }
    }
}
