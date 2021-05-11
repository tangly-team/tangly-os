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

package net.tangly.invoices.services;

import java.util.List;
import javax.inject.Inject;

import net.tangly.core.TypeRegistry;
import net.tangly.core.codes.CodeType;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.invoices.domain.Article;
import net.tangly.invoices.domain.ArticleCode;
import net.tangly.invoices.domain.Invoice;
import net.tangly.invoices.domain.InvoiceLegalEntity;

public class InvoicesBoundedDomain extends BoundedDomain<InvoicesRealm, InvoicesBusinessLogic, InvoicesHandler, InvoicesPort> {
    public static final String DOMAIN = "invoices";

    @Inject
    public InvoicesBoundedDomain(InvoicesRealm realm, InvoicesBusinessLogic logic, InvoicesHandler handler, InvoicesPort port, TypeRegistry registry) {
        super(DOMAIN, realm, logic, handler, port, registry);
    }

    @Override
    protected void initialize() {
        registry().register(CodeType.of(ArticleCode.class));
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, Article.class, realm().articles()), new DomainEntity<>(DOMAIN, Invoice.class, realm().invoices()),
            new DomainEntity<>(DOMAIN, InvoiceLegalEntity.class, realm().legalEntities()));
    }
}
