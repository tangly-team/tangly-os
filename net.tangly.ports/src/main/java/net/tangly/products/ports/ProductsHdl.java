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

package net.tangly.products.ports;

import java.nio.file.Path;
import java.util.List;

import net.tangly.bus.products.Assignment;
import net.tangly.bus.products.Effort;
import net.tangly.bus.products.Product;
import net.tangly.bus.products.RealmProducts;
import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import net.tangly.ports.TsvHdl;
import org.jetbrains.annotations.NotNull;

import static net.tangly.ports.TsvHdl.OID;
import static net.tangly.ports.TsvHdl.TEXT;
import static net.tangly.ports.TsvHdl.createTsvEntityFields;

public class ProductsHdl {
    public static final String PRODUCTS_TSV = "products.tsv";
    public static final String ASSIGNMENTS_TSV = "assignments.tsv";
    public static final String EFFORTS_TSV = "efforts.tsv";

    private final RealmProducts realm;
    private final Path folder;

    public ProductsHdl(@NotNull RealmProducts realm, @NotNull Path folder) {
        this.realm = realm;
        this.folder = folder;
    }

    public void importEntities() {
        TsvHdl.importEntities(folder.resolve(PRODUCTS_TSV), createTsvProduct(), realm.products());
        TsvHdl.importEntities(folder.resolve(ASSIGNMENTS_TSV), createTsvAssignment(), realm.assignements());
        TsvHdl.importEntities(folder.resolve(EFFORTS_TSV), createTsvEffort(), realm.efforts());
    }

    public void exportEntities() {
        TsvHdl.exportEntities(folder.resolve(PRODUCTS_TSV), createTsvProduct(), realm.products());
        TsvHdl.exportEntities(folder.resolve(ASSIGNMENTS_TSV), createTsvAssignment(), realm.assignements());
        TsvHdl.exportEntities(folder.resolve(EFFORTS_TSV), createTsvEffort(), realm.efforts());
    }

    TsvEntity<Product> createTsvProduct() {
        List<TsvProperty<Product, ?>> fields = createTsvEntityFields();
        // TODO implements
        //                        fields.add(TsvProperty.of("contractIds", Product::contractIds, Product::contractIds,
        //                                (String e) -> Arrays.asList(e.split(",", -1)),
        //                                (List<String> e) -> String.join(",", e)));
        return TsvEntity.of(Product.class, fields, Product::new);
    }

    TsvEntity<Assignment> createTsvAssignment() {
        List<TsvProperty<Assignment, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.ofString("collaboratorName", Assignment::collaboratorName, Assignment::collaboratorName));
        // TODO add foid to product
        return TsvEntity.of(Assignment.class, fields, Assignment::new);
    }

    TsvEntity<Effort> createTsvEffort() {
        List<TsvProperty<Effort, ?>> fields =
                List.of(TsvProperty.of(OID, Effort::oid, (entity, value) -> ReflectionUtilities.set(entity, OID, value), Long::parseLong),
                        TsvProperty.ofString(TEXT, Effort::text, Effort::text),
                        TsvProperty.of("date", Effort::date, Effort::date, TsvProperty.CONVERT_DATE_FROM),
                        TsvProperty.ofInt("durationInMinutes", Effort::duration, Effort::duration));
        return TsvEntity.of(Effort.class, fields, Effort::new);
    }
}
