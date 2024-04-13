/*
 * Copyright 2022-2024 Marcel Baumann
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

package net.tangly.erp.products.ports;

import net.tangly.core.providers.Provider;
import net.tangly.erp.ports.TsvHdl;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.domain.Product;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.erp.products.services.ProductsRealm;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.tangly.core.tsv.TsvHdlCore.TEXT;
import static net.tangly.erp.ports.TsvHdl.convertFoidTo;
import static net.tangly.erp.ports.TsvHdl.createTsvEntityFields;

public class ProductsTsvHdl {
    public static final String PRODUCTS_TSV = "products.tsv";
    public static final String ASSIGNMENTS_TSV = "assignments.tsv";
    public static final String EFFORTS_TSV = "efforts.tsv";

    private final ProductsRealm realm;

    public ProductsTsvHdl(@NotNull ProductsRealm realm) {
        this.realm = realm;
    }

    public void importProducts(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(ProductsBoundedDomain.DOMAIN, reader, source, createTsvProduct(), realm.products());
    }

    public void exportProducts(@NotNull Path path) {
        TsvHdl.exportEntities(ProductsBoundedDomain.DOMAIN, path, createTsvProduct(), realm.products());
    }

    public void importAssignments(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(ProductsBoundedDomain.DOMAIN, reader, source, createTsvAssignment(), realm.assignments());
    }

    public void exportAssignments(@NotNull Path path) {
        TsvHdl.exportEntities(ProductsBoundedDomain.DOMAIN, path, createTsvAssignment(), realm.assignments());
    }

    public void importEfforts(@NotNull Reader reader, String source) {
        TsvHdl.importEntities(ProductsBoundedDomain.DOMAIN, reader, source, createTsvEffort(), realm.efforts());
    }

    public void exportEfforts(@NotNull Path path) {
        TsvHdl.exportEntities(ProductsBoundedDomain.DOMAIN, path, createTsvEffort(), realm.efforts());
    }

    public Optional<Product> findProductByOid(String identifier) {
        return (identifier != null) ? Provider.findByOid(realm.products(), Long.parseLong(identifier)) : Optional.empty();
    }

    public Optional<Assignment> findAssignmentByOid(String identifier) {
        return (identifier != null) ? Provider.findByOid(realm.assignments(), Long.parseLong(identifier)) : Optional.empty();
    }

    private static TsvEntity<Product> createTsvProduct() {
        List<TsvProperty<Product, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.of("contractIds", Product::contractIds, Product::contractIds, e -> Arrays.asList(e.split(",", -1)), e -> String.join(",", e)));
        return TsvHdl.of(Product.class, fields, Product::new);
    }

    private TsvEntity<Assignment> createTsvAssignment() {
        List<TsvProperty<Assignment, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.ofString("collaboratorId", Assignment::collaboratorId, Assignment::collaboratorId));
        fields.add(TsvProperty.of("productOid", Assignment::product, Assignment::product, e -> findProductByOid(e).orElse(null), convertFoidTo()));
        return TsvHdl.of(Assignment.class, fields, Assignment::new);
    }

    private TsvEntity<Effort> createTsvEffort() {
        List<TsvProperty<Effort, ?>> fields =
            List.of(TsvProperty.of("date", Effort::date, Effort::date, TsvProperty.CONVERT_DATE_FROM), TsvProperty.ofInt("durationInMinutes", Effort::duration, Effort::duration),
                TsvProperty.ofString(TEXT, Effort::text, Effort::text),
                TsvProperty.of("assignmentOid", Effort::assignment, Effort::assignment, e -> findAssignmentByOid(e).orElse(null), convertFoidTo()),
                TsvProperty.ofString("contractId", Effort::contractId, Effort::contractId));
        return TsvEntity.of(Effort.class, fields, Effort::new);
    }
}
