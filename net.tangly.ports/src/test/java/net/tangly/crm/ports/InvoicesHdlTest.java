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

package net.tangly.crm.ports;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.bus.invoices.BusinessLogicInvoices;
import net.tangly.bus.invoices.RealmInvoices;
import net.tangly.invoices.ports.InvoicesEntities;
import net.tangly.invoices.ports.InvoicesHdl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InvoicesHdlTest {
    public static final String CONTRACT_HSLU_2015 = "HSLU-2015";

    @Test
        // @Tag("localTest")
    void testCompanyTsvInvoices() {
        InvoicesHdl invoicesHdl = new InvoicesHdl(new InvoicesEntities());
        invoicesHdl.importEntities(Path.of("/Users/Shared/tangly/", "invoices"));
    }

    @Test
    void testTsvInvoices() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            CrmAndLedgerStore store = new CrmAndLedgerStore(fs);
            store.createCrmAndLedgerRepository();

            InvoicesHdl invoicesHdl = new InvoicesHdl(new InvoicesEntities());
            invoicesHdl.importEntities(store.invoicesRoot());

            verifyArticles(invoicesHdl.realm());
            verifyInvoices(invoicesHdl.realm());

            verifyBusinessLogic(invoicesHdl.realm());

            invoicesHdl.exportEntities(store.invoicesRoot());

            invoicesHdl = new InvoicesHdl(new InvoicesEntities());
            invoicesHdl.importEntities(store.invoicesRoot());
            verifyArticles(invoicesHdl.realm());
        }
    }

    private void verifyInvoices(@NotNull RealmInvoices realm) {
        assertThat(realm.invoices().items().isEmpty()).isFalse();
        realm.invoices().items().forEach(o -> assertThat(o.isValid()).isTrue());
    }

    private void verifyArticles(@NotNull RealmInvoices realm) {
        assertThat(realm.articles().items().isEmpty()).isFalse();
    }

    private void verifyBusinessLogic(@NotNull RealmInvoices realm) {
        BusinessLogicInvoices logic = new BusinessLogicInvoices(realm);
        assertThat(logic.invoicedAmountWithoutVatForContract(CONTRACT_HSLU_2015, null, null))
                .isEqualByComparingTo(logic.paidAmountWithoutVatForContract(CONTRACT_HSLU_2015, null, null));
        assertThat(logic.expensesForContract(CONTRACT_HSLU_2015, null, null)).isNotNegative();
    }
}
