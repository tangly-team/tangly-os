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

package net.tangly.erp.products.ui;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import net.tangly.app.Application;
import net.tangly.core.TypeRegistry;
import net.tangly.erp.products.ports.ProductsAdapter;
import net.tangly.erp.products.ports.ProductsEntities;
import net.tangly.erp.products.services.ProductsBoundedDomain;
import net.tangly.erp.products.services.ProductsBusinessLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

@Tag("IntegrationTest")
class ProductsTest {
    @BeforeEach
    void setup() {
        var realm = new ProductsEntities();
        var logic = new ProductsBusinessLogic(realm);
        var domain = new ProductsBoundedDomain(realm, logic, new ProductsAdapter(realm, null, null, null), new TypeRegistry());
        Application.instance().registerBoundedDomain(domain);
        MockVaadin.setup();
    }

}