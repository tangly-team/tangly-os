/*
 * Copyright 2006-2020 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.bdd.two;

import net.tangly.bdd.Scenario;
import net.tangly.bdd.Scene;
import net.tangly.bdd.StoreTest;
import net.tangly.bdd.Story;
import net.tangly.bdd.engine.StoryExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(StoryExtension.class)
@Story(value = "Replaced are switched form the stockpile", description = "As a store owner, in order to keep track of stock, I want to switch items" +
        "back to stock when they're returned and replaced.")
class StoreTestStoryReplace extends StoreTest {
    @Scenario("Replaced items should be returned to stock, and the new ones deduced from the stock")
    void replaceAndRestock(Scene scene) {
        scene.given("A customer previously bought a blue garment from the store having 3 black and 3 blue sweaters", t -> create(t, 3, 3).sellBlue(1))
                .
                        and("the store now has 2 blue garments in stock",
                                t -> assertThat(store(t).blues()).as("Store should carry 2 blue garments").isEqualTo(2)).
                and("the store has 3 black garments in stock",
                        t -> assertThat(store(t).blacks()).as("Store should carry 3 black garments").isEqualTo(3)).

                when("The customer returns one blue garment for a replacement with one in black", t -> store(t).refundBlue(1).sellBlack(1)).

                then("The store should have 3 blue garments in stock",
                        t -> assertThat(store(t).blues()).as("Store should carry 3 blue garments").isEqualTo(3)).
                and("the store should have 2 black garments in stock",
                        t -> assertThat(store(t).blacks()).as("Store should carry 2 black garments").isEqualTo(2)).
                run();
    }
}
