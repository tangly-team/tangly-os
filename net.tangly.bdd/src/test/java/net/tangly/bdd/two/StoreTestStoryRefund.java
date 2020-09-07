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

package net.tangly.bdd.two;

import net.tangly.bdd.Scenario;
import net.tangly.bdd.Scene;
import net.tangly.bdd.Store;
import net.tangly.bdd.StoreTest;
import net.tangly.bdd.Story;
import net.tangly.bdd.engine.StoryExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD story test refund for shirts store.
 */
@ExtendWith(StoryExtension.class)
@Story(value = "Refunded items go back to the stockpile",
       description = "As a store owner, in order to keep track of stock, I want to add items back to stock when they're refunded.", tags = {"Release 2.0"})
class StoreTestStoryRefund extends StoreTest {
    @Scenario("Refunded items from the customer should be returned to stock")
    void refundAndRestock(Scene scene) {
        scene.given("A customer bought a black sweater from the store having 4 black and 0 blue sweaters", t -> t.put("store", new Store(4, 0).sellBlack(1))).
                and("the store has now 3 black sweaters in stock",
                        t -> assertThat(store(t).blacks()).as("the store should carry 3 black sweaters").isEqualTo(3))
                .and("0 blue sweaters in stock", t -> assertThat(store(t).blues()).as("the store should carry 0 blue sweaters").isEqualTo(0)).

                when("The customer returns one black sweater for a refund", t -> store(t).refundBlack(1)).

                then("the store should have 4 black sweaters in stock",
                        t -> assertThat(store(t).blacks()).as("the store should carry 4 black sweaters").isEqualTo(4)).
                and("0 blue sweaters in stock", t -> assertThat(store(t).blues()).as("the store should carry 0 blue sweaters").isEqualTo(0)).
                run();
    }
}
