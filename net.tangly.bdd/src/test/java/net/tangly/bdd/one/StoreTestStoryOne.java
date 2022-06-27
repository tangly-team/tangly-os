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

package net.tangly.bdd.one;

import net.tangly.bdd.Scenario;
import net.tangly.bdd.Scene;
import net.tangly.bdd.Story;
import net.tangly.bdd.engine.StoryExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.tangly.bdd.StoreUtilities.create;
import static net.tangly.bdd.StoreUtilities.store;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BDD story test for shirts store.
 */
@ExtendWith(StoryExtension.class)
@Story(value = "Buy Sweater", description = "As a store owner I want to update the stock when I am selling sweaters to customers.", tags = {"Release 1.0"})
class StoreTestStoryOne  {
    @Scenario("Sell some black sweaters in stock")
    void sellBlackSweaters(Scene scene) {
        scene.given("The store is stocked with sweaters", t -> create(t, 5, 4)).
                and("has 5 black sweaters in stock", t -> assertThat(store(t).blacks()).as("Store should carry 4 black sweaters").isEqualTo(5)).
                and("4 blue sweaters in stock", t -> assertThat(store(t).blues()).as("Store should carry 4 blue sweaters").isEqualTo(4)).

                when("The customer buys 3 black sweaters", t -> store(t).sellBlack(3)).

                then("The store should have 2 black sweaters in stock",
                        t -> assertThat(store(t).blacks()).as("Store should carry 1 black sweaters").isEqualTo(2)).
                and("4 blue sweaters in stock", t -> assertThat(store(t).blues()).as("Store should carry 4 blue sweaters").isEqualTo(4)).
                run();
    }

    @Scenario("Sell some blue sweaters in stock")
    void sellBlueSweaters(Scene scene) {
        scene.given("The store is stocked with sweaters", t -> create(t, 5, 4)).
                and("has 5 black sweaters in stock", t -> assertThat(store(t).blacks()).as("Store should carry 5 black sweaters").isEqualTo(5)).
                and("4 blue sweaters in stock", t -> assertThat(store(t).blues()).as("Store should carry 4 blue sweaters").isEqualTo(4)).

                when("The customer buys 3 blue sweaters", t -> store(t).sellBlue(3)).

                then("The store should have 5 black sweaters in stock",
                        t -> assertThat(store(t).blacks()).as("Store should carry 5 black sweaters").isEqualTo(5)).
                and("1 blue sweaters in stock", t -> assertThat(store(t).blues()).as("Store should carry 1 blue sweaters").isEqualTo(1)).
                run();
    }

    @Scenario("Sell some blacks and blue sweaters in stock")
    void sellBlackAndBlueSweaters(Scene scene) {
        scene.given("The store is stocked with sweaters", t -> create(t, 5, 4)).
                and("has 5 black sweaters in stock", t -> assertThat(store(t).blacks()).as("Store should carry 5 black sweaters").isEqualTo(5)).
                and("4 blue sweaters in stock", t -> assertThat(store(t).blues()).as("Store should carry 4 blue sweaters").isEqualTo(4)).

                when("The customer buys 3 black sweaters", t -> store(t).sellBlack(3)).
                and("2 blue sweaters", t -> store(t).sellBlue(2)).

                then("The store should have 2 black sweaters in stock",
                        t -> assertThat(store(t).blacks()).as("Store should carry 2 black sweaters").isEqualTo(2)).
                and("2 blue sweaters in stock", t -> assertThat(store(t).blacks()).as("Store should carry 2 blue sweaters").isEqualTo(2)).
                run();
    }

    @Scenario("Refuse to sell more black sweaters than the store in stock")
    void tryToSellTooManyBlackSweaters(Scene scene) {
        scene.given("The store is stocked with sweaters", t -> create(scene, 5, 4)).
                and("has 5 black sweaters in stock", t -> assertThat(store(t).blacks()).as("Store should carry 5 black sweaters").isEqualTo(5)).
                and("4 blue sweaters in stock", t -> assertThat(store(t).blues()).as("Store should carry 5 blue sweaters").isEqualTo(4)).

                when("The customer try to buy 6 blue sweaters, and an exception is thrown",
                        t -> assertThatThrownBy(() -> store(t).sellBlack(6)).isInstanceOf(RuntimeException.class)
                                .hasMessageContaining("There aren't enough black garments in stock.")).

                then("the store should have 5 black sweaters in stock",
                        t -> assertThat(store(t).blacks()).as("Store should carry 5 black sweaters").isEqualTo(5)).
                and("4 blue sweaters in stock", t -> assertThat(store(t).blues()).as("Store should carry 4 blue sweaters").isEqualTo(4)).run();
    }

    @Scenario("Refuse to sell more blue sweaters than the store in stock")
    void tryToSellTooManyBlueSweaters(Scene scene) {
        scene.given("The store is stocked with sweaters", t -> create(scene, 5, 4)).
                and("has 5 black sweaters in stock", t -> assertThat(store(t).blacks()).as("Store should carry 5 black sweaters").isEqualTo(5)).
                and("4 blue sweaters in stock", t -> assertThat(store(t).blues()).as("Store should carry 5 blue sweaters").isEqualTo(4)).

                when("The customer try to buy 6 blue sweaters, and an exception is thrown",
                        t -> assertThatThrownBy(() -> store(t).sellBlue(6)).isInstanceOf(RuntimeException.class)
                                .hasMessageContaining("There aren't enough blue garments in stock.")).

                then("the store should have 5 black sweaters in stock",
                        t -> assertThat(store(t).blacks()).as("Store should carry 5 black sweaters").isEqualTo(5)).
                and("4 blue sweaters in stock", t -> assertThat(store(t).blues()).as("Store should carry 4 blue sweaters").isEqualTo(4)).run();
    }
}
