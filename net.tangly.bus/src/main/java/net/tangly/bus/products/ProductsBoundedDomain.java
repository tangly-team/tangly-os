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

package net.tangly.bus.products;

import java.util.Map;

import net.tangly.core.TagType;
import net.tangly.core.TagTypeRegistry;
import net.tangly.core.app.BoundedDomain;
import org.jetbrains.annotations.NotNull;

public class ProductsBoundedDomain extends BoundedDomain<ProductsRealm, ProductsBusinessLogic, ProductsHandler, ProductsPort> {
    public static final String PRODUCTS_OID_VALUE = "products-oid-value";

    public ProductsBoundedDomain(ProductsRealm realm, ProductsBusinessLogic logic, ProductsHandler handler, ProductsPort port, TagTypeRegistry registry,
                                 @NotNull Map<String, String> configuration) {
        super(realm, logic, handler, port, registry, configuration);
    }

    @Override
    public Map<TagType<?>, Integer> countTags(@NotNull Map<TagType<?>, Integer> counts) {
        addTagCounts(registry(), realm().products().items(), counts);
        addTagCounts(registry(), realm().assignments().items(), counts);
        return counts;
    }
}
