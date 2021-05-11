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

package net.tangly.commons.products.ui;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import net.tangly.products.services.ProductsBoundedDomain;
import net.tangly.products.services.ProductsBusinessLogic;
import net.tangly.core.TypeRegistry;
import net.tangly.products.ports.ProductsAdapter;
import net.tangly.products.ports.ProductsEntities;
import net.tangly.products.ports.ProductsHdl;
import org.junit.jupiter.api.BeforeEach;

class ProductsItTest {
    static ProductsBoundedDomain ofDomain() {
        var realm = new ProductsEntities();
        var logic = new ProductsBusinessLogic(realm);
        return new ProductsBoundedDomain(realm, logic, new ProductsHdl(realm, null), new ProductsAdapter(logic, null), new TypeRegistry());
    }

    @BeforeEach
    void setup() {
        MockVaadin.setup();
    }

}
