/*
 * Copyright 2006-2023 Marcel Baumann
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

package net.tangly.spec.bdd.two;

import net.tangly.spec.bdd.Scenario;
import net.tangly.spec.bdd.Scene;
import net.tangly.spec.bdd.Store;
import net.tangly.spec.bdd.StoreUtilities;
import net.tangly.spec.bdd.Story;
import net.tangly.spec.bdd.engine.StoryExtension;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * BDD story test refund for the shirt store.
 */
@ExtendWith(StoryExtension.class)
@Story(value = "Refunded items go back to the stockpile", description = "As a store owner, in order to keep track of stock, I want to add items back to stock when they're " +
    "refunded.", tags = {"Release 2.0"})
class StoreStoryRefundTest {
    @Scenario("Refunded items from the customer should be returned to stock")
    void refundAndRestock(Scene scene) {
        scene.given("A customer bought a black sweater from the store having 4 black and 0 blue sweaters", t -> t.put("store", new Store(4, 0).sellBlack(1)))
            .and("the store has now 3 black sweaters in stock",
                t -> Assertions.assertThat(StoreUtilities.store(t).blacks()).as("the store should carry 3 black sweaters").isEqualTo(3))
            .and("0 blue sweaters in stock", t -> Assertions.assertThat(StoreUtilities.store(t).blues()).as("the store should carry 0 blue sweaters").isZero()).

                when("The customer returns one black sweater for a refund", t -> StoreUtilities.store(t).refundBlack(1)).

                then("the store should have 4 black sweaters in stock",
                t -> Assertions.assertThat(StoreUtilities.store(t).blacks()).as("the store should carry 4 black sweaters").isEqualTo(4))
            .and("0 blue sweaters in stock", t -> Assertions.assertThat(StoreUtilities.store(t).blues()).as("the store should carry 0 blue sweaters").isZero()).run();
    }
}
