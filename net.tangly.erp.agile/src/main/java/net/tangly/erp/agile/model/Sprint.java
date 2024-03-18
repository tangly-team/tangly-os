/*
 * Copyright 2021-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.agile.model;

import net.tangly.core.DateRange;
import net.tangly.core.ExternalEntity;
import net.tangly.core.ExternalEntityImp;
import net.tangly.core.HasDateRange;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Objects;

public class Sprint extends ExternalEntityImp implements ExternalEntity, HasDateRange {
    private DateRange interval;

    public Sprint(@NotNull String id) {
        super(id);
        interval = DateRange.INFINITE;
    }

    // region HasInterval

    @Override
    public DateRange range() {
        return interval;
    }

    @Override
    public void range(@NotNull DateRange range) {
        this.interval = Objects.nonNull(range) ? range : DateRange.INFINITE;
    }

    @Override
    public void from(LocalDate from) {
        interval = interval.from(from);
    }

    @Override
    public void to(LocalDate to) {
        interval = interval.to(to);
    }
    // endregion
}
