/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.tangly.core.Entity;
import net.tangly.core.domain.Realm;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.ports.ProductsEntities;
import net.tangly.erp.products.ports.ProductsAdapter;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import net.tangly.erp.products.services.ProductsRealm;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static net.tangly.core.providers.Provider.findById;
import static org.assertj.core.api.Assertions.assertThat;

class ProductsHdlTest {
    private static final String CONTRACT_ID = "STG-2020";
    private static final String PRODUCT_ID = "STG-Agile";
    private static final String ASSIGNMENT_ID = "Assignment-Test-009";
    private static final String COLLABORATOR_ID = "756.5149.8825.64";

    @Test
    void testTsvCrm() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createRepository();

            var realm = new ProductsEntities();
            var handler = new ProductsAdapter(realm, new ProductsBusinessLogic(realm), store.productsRoot());
            handler.importEntities();
            verifyProducts(handler.realm());
            verifyAssignments(handler.realm(), 0, null);
            verifyEfforts(handler.realm(), 0, null);

            Assignment assignment = createAssignment(handler.realm());
            handler.realm().assignments().update(assignment);
            int nrOfAssignments = handler.realm().assignments().items().size();
            Effort effort = createEffort(handler.realm());
            handler.realm().efforts().update(effort);
            int nrOfEfforts = handler.realm().efforts().items().size();

            handler.exportEntities();

            handler = new ProductsAdapter(new ProductsEntities(), null, store.productsRoot());
            handler.importEntities();
            verifyProducts(handler.realm());
            verifyAssignments(handler.realm(), nrOfAssignments, assignment);
            verifyEfforts(handler.realm(), nrOfEfforts, effort);
        }
    }

    private void verifyProducts(@NotNull ProductsRealm realm) {
        assertThat(realm.products().items()).isNotEmpty();
        Realm.checkEntities(realm.products());
    }

    private void verifyAssignments(@NotNull ProductsRealm realm, int nrOfEntities, Assignment entity) {
        assertThat(realm.assignments().items()).isNotEmpty();
        Realm.checkEntities(realm.assignments());
        if (entity != null) {
            assertThat(realm.assignments().items()).hasSize(nrOfEntities);
            Optional<Assignment> copy = findById(realm.assignments(), entity.id());
            assertThat(copy).isNotEmpty();
            assertThat(copy).contains(entity);
        }
    }

    private void verifyEfforts(@NotNull ProductsRealm realm, int nrOfEntities, Effort entity) {
        assertThat(realm.efforts().items()).isNotEmpty();
        realm.efforts().items().forEach(o -> assertThat(o.check()).isTrue());
        if (entity != null) {
            assertThat(realm.efforts().items()).hasSize(nrOfEntities);
            Optional<Effort> copy = realm.efforts().findBy(Effort::assignment, entity.assignment());
            assertThat(copy).isNotEmpty();
            assertThat(copy).contains(entity);
        }
    }

    private Assignment createAssignment(@NotNull ProductsRealm realm) {
        Assignment entity = new Assignment(Entity.UNDEFINED_OID);
        entity.id(ASSIGNMENT_ID);
        entity.name("Assignment_Test");
        entity.from(LocalDate.of(2020, Month.JANUARY, 1));
        entity.to(LocalDate.of(2020, Month.DECEMBER, 31));
        entity.text("*This is a markdown comment for an assignment*");
        entity.collaboratorId(COLLABORATOR_ID);
        entity.product(findById(realm.products(), PRODUCT_ID).orElseThrow());
        return entity;
    }

    private Effort createEffort(@NotNull ProductsRealm realm) {
        Effort entity = new Effort();
        entity.date(LocalDate.of(2020, Month.MAY, 15));
        entity.duration(60);
        entity.contractId(CONTRACT_ID);
        entity.assignment(findById(realm.assignments(), ASSIGNMENT_ID).orElseThrow());
        entity.text("*This is a markdown comment for an effort*");
        return entity;
    }
}
