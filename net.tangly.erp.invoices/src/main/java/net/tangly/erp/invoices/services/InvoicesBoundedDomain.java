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

package net.tangly.erp.invoices.services;

import net.tangly.core.codes.CodeType;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.Document;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.TenantDirectory;
import net.tangly.erp.invoices.domain.Article;
import net.tangly.erp.invoices.domain.ArticleCode;
import net.tangly.erp.invoices.domain.Invoice;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InvoicesBoundedDomain extends BoundedDomain<InvoicesRealm, InvoicesBusinessLogic, InvoicesPort> {
    public static final String DOMAIN = "invoices";

    public InvoicesBoundedDomain(@NotNull InvoicesRealm realm, InvoicesBusinessLogic logic, InvoicesPort port, TenantDirectory directory) {
        super(DOMAIN, realm, logic, port, directory);
        registry().register(CodeType.of(ArticleCode.class));

    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, Article.class, realm().articles()), new DomainEntity<>(DOMAIN, Invoice.class, realm().invoices()),
            new DomainEntity<>(DOMAIN, Document.class, realm().documents()));
    }
}
