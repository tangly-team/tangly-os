/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.products.domain;

import net.tangly.core.HasDate;
import net.tangly.core.HasText;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An expense defines a monetary cost associated with a contract and implictly a product.
 */
public record Expense(@NotNull LocalDate date, @NotNull String contractId, String text, @NotNull BigDecimal amount) implements HasDate, HasText {
    public Expense {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount cannot be negative " + amount);
        }
    }
}
