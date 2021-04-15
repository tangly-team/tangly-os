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
import javax.inject.Inject;

import net.tangly.commons.utilities.DateUtilities;
import org.jetbrains.annotations.NotNull;

public class ProductsBusinessLogic {
    private final ProductsRealm realm;

    @Inject
    public ProductsBusinessLogic(@NotNull ProductsRealm realm) {
        this.realm = realm;
    }

    public ProductsRealm realm() {
        return realm;
    }

    public List<Effort> collect(@NotNull Assignment assignment, LocalDate from, LocalDate to) {
        return realm().efforts().items().stream().filter(o -> Objects.equals(o.assignment(), assignment))
            .filter(o -> DateUtilities.isWithinRange(o.date(), from, to)).toList();
    }
}
