/*
 * Copyright 2006-2023 Marcel Baumann
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

package net.tangly.spec.bdd;

import org.jetbrains.annotations.Contract;

/**
 * The store class being under behavior driven test.
 */
public class Store {
    private int blues;
    private int blacks;

    @Contract(pure = true)
    public Store(int blacks, int blues) {
        this.blacks = blacks;
        this.blues = blues;
    }

    public int blues() {
        return blues;
    }

    public int blacks() {
        return blacks;
    }

    public Store sellBlack(int quantity) {
        if (canSell(quantity, blacks())) {
            blacks -= quantity;
        } else {
            throw new RuntimeException("There aren't enough black garments in stock.");
        }
        return this;
    }

    public Store refundBlack(int quantity) {
        if (canRefund(quantity)) {
            blacks += quantity;
        } else {
            throw new RuntimeException("One or more garments should be returned or exchanged.");
        }
        return this;
    }

    public Store sellBlue(int quantity) {
        if (canSell(quantity, blues())) {
            blues -= quantity;
        } else {
            throw new RuntimeException("There aren't enough blue garments in stock.");
        }
        return this;
    }

    public Store refundBlue(int quantity) {
        if (canRefund(quantity)) {
            blues += quantity;
        } else {
            throw new RuntimeException("One or more garments should be returned or exchanged.");
        }
        return this;
    }

    @Contract(pure = true)
    private boolean canSell(int purchaseQuantity, int inStockQuantity) {
        return inStockQuantity - purchaseQuantity >= 0;
    }

    @Contract(pure = true)
    private boolean canRefund(int refundQuantity) {
        return refundQuantity > 0;
    }
}
