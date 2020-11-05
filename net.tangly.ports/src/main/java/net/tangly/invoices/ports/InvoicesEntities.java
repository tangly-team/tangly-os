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

package net.tangly.invoices.ports;

import net.tangly.core.TagTypeRegistry;
import net.tangly.bus.invoices.Article;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoiceLegalEntity;
import net.tangly.bus.invoices.InvoicesRealm;
import net.tangly.bus.providers.RecordProvider;
import net.tangly.bus.providers.RecordProviderInMemory;
import org.jetbrains.annotations.NotNull;

public class InvoicesEntities implements InvoicesRealm {
    private final TagTypeRegistry registry;
    private final RecordProviderInMemory<Invoice> invoices;
    private final RecordProviderInMemory<Article> articles;
    private final RecordProviderInMemory<InvoiceLegalEntity> legalEntities;

    public InvoicesEntities(@NotNull TagTypeRegistry registry) {
        this.registry = registry;
        invoices = new RecordProviderInMemory<>();
        articles = new RecordProviderInMemory<>();
        legalEntities = new RecordProviderInMemory<>();
    }

    @Override
    public TagTypeRegistry tagTypeRegistry() {
        return registry;
    }

    @Override
    public RecordProvider<Article> articles() {
        return this.articles;
    }

    @Override
    public RecordProvider<Invoice> invoices() {
        return this.invoices;
    }

    @Override
    public RecordProviderInMemory<InvoiceLegalEntity> legalEntities() {
        return legalEntities;
    }
}
