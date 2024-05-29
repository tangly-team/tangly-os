/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.erp.ledger.services;

import java.math.BigDecimal;

public enum VatCode {
    F1(new BigDecimal("0.080"), new BigDecimal("0.061")),
    F2(new BigDecimal("0.080"), new BigDecimal("0.061")),
    F3(new BigDecimal("0.077"), new BigDecimal("0.065")),
    F4(new BigDecimal("0.081"), new BigDecimal("0.069"));

    private final BigDecimal vatRate;
    private final BigDecimal vatDueRate;

    VatCode(BigDecimal vatRate, BigDecimal vatDueRate) {
        this.vatRate = vatRate;
        this.vatDueRate = vatDueRate;
    }

    public BigDecimal vatRate() {
        return vatRate;
    }

    public BigDecimal vatDueRate() {
        return vatDueRate;
    }

    public static VatCode of(String code) {
        return switch (code) {
            case null -> null;
            case "F1" -> F1;
            case "F2" -> F2;
            case "F3" -> F3;
            case "F4" -> F4;
            default -> null;
        };
    }
}
