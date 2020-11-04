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

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;

import net.tangly.commons.utilities.DateUtilities;
import org.jetbrains.annotations.NotNull;

public class ProductsBusinessLogic {
    private final ProductsRealm realm;
    private final ProductsHandler handler;
    private final ProductsPort port;

    @Inject
    public ProductsBusinessLogic(@NotNull ProductsRealm realm, ProductsHandler handler, ProductsPort port) {
        this.realm = realm;
        this.handler = handler;
        this.port = port;
    }

    public ProductsRealm realm() {
        return realm;
    }

    public ProductsHandler handler() {
        return handler;
    }

    public ProductsPort port() {
        return port;
    }

    public List<Effort> collect(Assignment assignment, LocalDate from, LocalDate to) {
        return realm().efforts().items().stream().filter(o -> Objects.equals(o.assignment(), assignment))
            .filter(o -> DateUtilities.isWithinRange(o.date(), from, to)).collect(Collectors.toList());
    }
}
