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

package net.tangly.erp;

import com.google.common.jimfs.Jimfs;
import net.tangly.erp.invoices.ports.InvoicesAdapter;
import net.tangly.erp.invoices.ports.InvoicesEntities;
import net.tangly.erp.invoices.services.InvoicesBoundedDomain;
import net.tangly.erp.invoices.services.InvoicesBusinessLogic;
import net.tangly.erp.invoices.services.InvoicesRealm;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicesBusinessLogicTest {
    public static final String CONTRACT_HSLU_2015 = "HSLU-2015";

    @Test
    void testTsvInvoices() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var handler = new InvoicesAdapter(new InvoicesEntities(),
                store.dataRoot().resolve(InvoicesBoundedDomain.DOMAIN), store.reportsRoot().resolve(InvoicesBoundedDomain.DOMAIN));
            handler.importEntities(store);

            verifyBusinessLogic(handler.realm());

            handler.exportEntities(store);

            handler = new InvoicesAdapter(new InvoicesEntities(),
                store.dataRoot().resolve(InvoicesBoundedDomain.DOMAIN), store.reportsRoot().resolve(InvoicesBoundedDomain.DOMAIN));
            handler.importEntities(store);
        }
    }

    private void verifyBusinessLogic(@NotNull InvoicesRealm realm) {
        var logic = new InvoicesBusinessLogic(realm);
        assertThat(logic.invoicedAmountWithoutVatForContract(CONTRACT_HSLU_2015, null, null))
            .isEqualByComparingTo(logic.paidAmountWithoutVatForContract(CONTRACT_HSLU_2015, null, null));
        assertThat(logic.expensesForContract(CONTRACT_HSLU_2015, null, null)).isNotNegative();
    }
}
