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
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;

public class BusinessLogicProducts {
    private final RealmProducts realm;

    @Inject
    public BusinessLogicProducts(@NotNull RealmProducts realm) {
        this.realm = realm;
    }

    public RealmProducts realm() {
        return realm;
    }

    public List<Assignment> collect(Assignment assignment, LocalDate from, LocalDate to) {
        return Collections.emptyList();
    }
}
