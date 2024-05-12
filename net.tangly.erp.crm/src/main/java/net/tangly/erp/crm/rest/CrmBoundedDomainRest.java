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

package net.tangly.erp.crm.rest;

import io.javalin.Javalin;
import net.tangly.app.api.BoundedDomainRest;
import net.tangly.erp.crm.services.CrmBoundedDomain;
import org.jetbrains.annotations.NotNull;

public class CrmBoundedDomainRest implements BoundedDomainRest {
    private final CrmBoundedDomain domain;
    private final LegalEntitiesRest legalEntitiesRest;
    private final NaturalEntitiesRest naturalEntitiesRest;

    public CrmBoundedDomainRest(CrmBoundedDomain domain) {
        this.domain = domain;
        this.naturalEntitiesRest = new NaturalEntitiesRest(domain);
        this.legalEntitiesRest = new LegalEntitiesRest(domain);
    }

    @Override
    public String name() {
        return domain.name();
    }

    @Override
    public void registerEndPoints(@NotNull Javalin javalin) {
        naturalEntitiesRest.registerEndPoints(javalin);
        legalEntitiesRest.registerEndPoints(javalin);
    }
}
