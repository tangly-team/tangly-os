/*
 * Copyright 2006-$year Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.orm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.tangly.bus.core.HasOid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class DaoHrormTest extends DaoTest {
    public record Author(Long id, String name) {
    }

    public record Ingredient(Long id, String name, long amount) {
    }

    public class Recipe {
        Long id;
        String name;
        Author author;
        List<Ingredient> ingredients;

        void name(String name) {
            this.name = name;
        }

        void author(Author author) {
            this.author = author;
        }

        void ingredients(List<Ingredient> ingredients) {
            ingredients = new ArrayList<>(ingredients);
        }
    }

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        setUpDatabase();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection connection = datasource().getConnection(); Statement stmt = connection.createStatement()) {
            stmt.execute("shutdown");
        }
    }

    private Recipe createBeefStew() {
        Author juliaChild = new Author(HasOid.UNDEFINED_OID, "Julia Child");
        Recipe beefStew = new Recipe();
        beefStew.name("Beef Stew");
        beefStew.author(juliaChild);
        Ingredient carrots = new Ingredient(HasOid.UNDEFINED_OID, "Carrots", 4L);
        Ingredient onions = new Ingredient(HasOid.UNDEFINED_OID, "Onions", 2L);
        Ingredient beef = new Ingredient(HasOid.UNDEFINED_OID, "Cow", 1L);
        Ingredient wine = new Ingredient(HasOid.UNDEFINED_OID, "Red Wine", 10L);
        beefStew.ingredients(Arrays.asList(carrots, onions, beef, wine));
        return beefStew;
    }
}
