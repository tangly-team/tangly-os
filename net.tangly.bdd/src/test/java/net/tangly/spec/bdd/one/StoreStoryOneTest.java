/*
 * Copyright 2006-2024 Marcel Baumann
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

package net.tangly.spec.bdd.one;

import net.tangly.spec.bdd.Scenario;
import net.tangly.spec.bdd.Scene;
import net.tangly.spec.bdd.Story;
import net.tangly.spec.bdd.engine.StoryExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.tangly.spec.bdd.StoreUtilities.create;
import static net.tangly.spec.bdd.StoreUtilities.store;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BDD story test for the shirt store.
 */
// @start region="bdd-story-sell-black-sweaters"
@ExtendWith(StoryExtension.class)
@Story(value = "Buy Sweater",
    description = """
        As a store owner I want to update the stock when I am selling sweaters to customers.""",
    tags = {"Release 1.0"})
// @end
class StoreStoryOneTest {
    // @start region="bdd-scenario-sell-black-sweaters"
    @Scenario("Sell some black sweaters in stock to a customer")
    void sellBlackSweaters(@NotNull Scene scene) {
        final int nrBlueSweaters = 4;
        final int nrBlackSweaters = 5;
        final int nrSoldBlackSweaters = 3;
        scene.given("The store is stocked with sweaters", s -> create(s, nrBlackSweaters, nrBlueSweaters)).
            and("has 5 black sweaters in stock", s -> assertThat(store(s).blacks())
                .as("Store should carry 5 black sweaters").isEqualTo(nrBlackSweaters)).
            and("4 blue sweaters in stock", s -> assertThat(store(s).blues())
                .as("Store should carry 4 blue sweaters").isEqualTo(nrBlueSweaters)).

            when("The customer buys 3 black sweaters", s -> store(s).sellBlack(nrSoldBlackSweaters)).

            then("The store should have 2 black sweaters in stock", s -> assertThat(store(s).blacks())
                .as("Store should carry 1 black sweaters").isEqualTo(nrBlackSweaters - nrSoldBlackSweaters)).
            and("4 blue sweaters in stock", s -> assertThat(store(s).blues())
                .as("Store should carry 4 blue sweaters").isEqualTo(nrBlueSweaters)).
            run();
    }
    // @end

    @Scenario("Sell some blue sweaters in stock to a customer")
    void sellBlueSweaters(@NotNull Scene scene) {
        scene.given("The store is stocked with sweaters", s -> create(s, 5, 4)).
            and("has 5 black sweaters in stock", s -> assertThat(store(s).blacks())
                .as("Store should carry 5 black sweaters").isEqualTo(5)).
            and("4 blue sweaters in stock", s -> assertThat(store(s).blues())
                .as("Store should carry 4 blue sweaters").isEqualTo(4)).

            when("The customer buys 3 blue sweaters", s -> store(s).sellBlue(3)).

            then("The store should have 5 black sweaters in stock", s -> assertThat(store(s).blacks())
                .as("Store should carry 5 black sweaters").isEqualTo(5)).
            and("1 blue sweaters in stock", s -> assertThat(store(s).blues())
                .as("Store should carry 1 blue sweater").isEqualTo(1)).
            run();
    }

    @Scenario("Sell some blacks and blue sweaters in stock")
    void sellBlackAndBlueSweaters(@NotNull Scene scene) {
        scene.given("The store is stocked with sweaters", s -> create(s, 5, 4)).
            and("has 5 black sweaters in stock", s -> assertThat(store(s).blacks())
                .as("Store should carry 5 black sweaters").isEqualTo(5)).
            and("4 blue sweaters in stock", s -> assertThat(store(s).blues())
                .as("Store should carry 4 blue sweaters").isEqualTo(4)).

            when("The customer buys 3 black sweaters", s -> store(s).sellBlack(3)).
            and("2 blue sweaters", s -> store(s).sellBlue(2)).

            then("The store should have 2 black sweaters in stock", s -> assertThat(store(s).blacks())
                .as("Store should carry 2 black sweaters").isEqualTo(2)).
            and("2 blue sweaters in stock", s -> assertThat(store(s).blacks())
                .as("Store should carry 2 blue sweaters").isEqualTo(2)).
            run();
    }

    @Scenario("Refuse to sell more black sweaters than the store in stock")
    void tryToSellTooManyBlackSweaters(@NotNull Scene scene) {
        scene.given("The store is stocked with sweaters", s -> create(scene, 5, 4)).
            and("has 5 black sweaters in stock", s -> assertThat(store(s).blacks())
                .as("Store should carry 5 black sweaters").isEqualTo(5)).
            and("4 blue sweaters in stock", s -> assertThat(store(s).blues())
                .as("Store should carry 5 blue sweaters").isEqualTo(4)).

            when("The customer try to buy 6 blue sweaters, and an exception is thrown",
                s -> assertThatThrownBy(() -> store(s).sellBlack(6)).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("There aren't enough black garments in stock.")).

            then("the store should have 5 black sweaters in stock", s -> assertThat(store(s).blacks())
                .as("Store should carry 5 black sweaters").isEqualTo(5)).
            and("4 blue sweaters in stock", s -> assertThat(store(s).blues())
                .as("Store should carry 4 blue sweaters").isEqualTo(4)).run();
    }

    @Scenario("Refuse to sell more blue sweaters than the store in stock")
    void tryToSellTooManyBlueSweaters(@NotNull Scene scene) {
        scene.given("The store is stocked with sweaters", s -> create(scene, 5, 4)).
            and("has 5 black sweaters in stock", s -> assertThat(store(s).blacks())
                .as("Store should carry 5 black sweaters").isEqualTo(5)).
            and("4 blue sweaters in stock", s -> assertThat(store(s).blues())
                .as("Store should carry 5 blue sweaters").isEqualTo(4)).

            when("The customer try to buy 6 blue sweaters, and an exception is thrown",
                s -> assertThatThrownBy(() -> store(s).sellBlue(6)).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("There aren't enough blue garments in stock.")).

            then("the store should have 5 black sweaters in stock", s -> assertThat(store(s).blacks())
                .as("Store should carry 5 black sweaters").isEqualTo(5)).
            and("4 blue sweaters in stock", t -> assertThat(store(t).blues())
                .as("Store should carry 4 blue sweaters").isEqualTo(4)).run();
    }
}
