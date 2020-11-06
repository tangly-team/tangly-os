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

import net.tangly.core.TagTypeRegistry;
import net.tangly.core.app.BoundedDomain;
import net.tangly.core.app.IdGenerator;

public class ProductsBoundedDomain extends BoundedDomain<ProductsRealm, ProductsBusinessLogic, ProductsHandler, ProductsPort> {
    private IdGenerator idGenerator;

    public ProductsBoundedDomain(ProductsRealm realm, ProductsBusinessLogic logic, ProductsHandler handler, ProductsPort port, TagTypeRegistry registry) {
        super(realm, logic, handler, port, registry);
        idGenerator = new IdGenerator(1000);
    }

    public IdGenerator idGenerator() {
        return idGenerator;
    }
}
