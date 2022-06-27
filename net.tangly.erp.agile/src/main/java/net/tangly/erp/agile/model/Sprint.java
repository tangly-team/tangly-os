/*
 * Copyright 2021-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.agile.model;

import net.tangly.core.ExternalEntity;
import net.tangly.core.ExternalEntityImp;
import net.tangly.core.HasInterval;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

public class Sprint extends ExternalEntityImp implements ExternalEntity, HasInterval {
    private LocalDate fromDate;
    private LocalDate toDate;

    public Sprint(@NotNull String id) {
        super(id);
    }

    // region HasInterval

    @Override
    public LocalDate from() {
        return fromDate;
    }

    public void from(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    @Override
    public LocalDate to() {
        return toDate;
    }

    public void to(LocalDate toDate) {
        this.toDate = toDate;
    }

    // endregion
}
