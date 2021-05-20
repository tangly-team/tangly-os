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

package net.tangly.erp.invoices.services;

import java.util.List;

import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.Invoice;
import net.tangly.erp.invoices.domain.InvoiceLegalEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the invoices subsystem entities. A realm provides access to the instances of the CRM abstractions. The major abstractions are:
 * <ul>
 *     <li>Invoices are legal entity invoices</li>
 * </ul>
 **/
public interface InvoicesRealm extends Realm {
    Provider<Invoice> invoices();

    Provider<Article> articles();

    Provider<InvoiceLegalEntity> legalEntities();

    default List<Invoice> invoicesFor(@NotNull String contractId) {
        return invoices().items().stream().filter(o -> contractId.equals(o.contractId())).toList();
    }
}
