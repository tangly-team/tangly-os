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
import java.util.Collections;
import java.util.List;

import net.tangly.bus.core.HasOid;
import net.tangly.commons.lang.Reference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DaoHrormTest extends DaoTest {
    public static class Author implements HasOid {
        private Long oid;
        private String name;

        public static Author of(String name) {
            Author author = new Author();
            author.name = name;
            return author;
        }

        public long oid() {
            return oid;
        }

    }

    public static class Ingredient implements HasOid {
        private Long oid;
        private String name;
        private long amount;

        public static Ingredient of(String name, long amount) {
            Ingredient ingredient = new Ingredient();
            ingredient.name = name;
            ingredient.amount = amount;
            return ingredient;
        }

        public long oid() {
            return oid;
        }
    }

    public static class Recipe implements HasOid {
        Long oid;
        String name;
        Author author;
        List<Ingredient> ingredients;

        public long oid() {
            return oid;
        }

        void name(String name) {
            this.name = name;
        }

        void author(Author author) {
            this.author = author;
        }

        List<Ingredient> ingredients() {
            return Collections.unmodifiableList(ingredients);
        }

        void add(Ingredient ingredient) {
            ingredients.add(ingredient);
        }

        void remove(Ingredient ingredient) {
            ingredients.remove(ingredient);
        }
    }

    private Dao<Author> authors;
    private Dao<Recipe> recipes;
    private Dao<Ingredient> ingredients;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        setUpDatabase();
        authors = new DaoBuilder<Author>(Author.class).withOid().withString("name").build("hrorm", "authors", datasource());
        ingredients = new DaoBuilder<Ingredient>(Ingredient.class).withOid().withString("name").withLong("amount").withFid("recipeFid")
                .build("hrorm", "ingredients", datasource());
        recipes = new DaoBuilder<Recipe>(Recipe.class).withOid().withString("name").withOne2One("author", Reference.of(authors), false)
                .withOne2Many("ingredients", "recipeFid", Reference.of(ingredients), true).build("hrorm", "recipes", datasource());
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection connection = datasource().getConnection(); Statement stmt = connection.createStatement()) {
            stmt.execute("shutdown");
        }
    }

    private Recipe createBeefStew() {
        Author juliaChild = Author.of("Julia Child");
        Recipe beefStew = new Recipe();
        beefStew.name("Beef Stew");
        beefStew.author(juliaChild);
        beefStew.add(Ingredient.of("Carrots", 4L));
        beefStew.add(Ingredient.of("Onions", 2L));
        beefStew.add(Ingredient.of("Cow", 1L));
        beefStew.add(Ingredient.of("Red Wine", 10L));
        return beefStew;
    }

    @Test
    void testPersistRecipe() {
        // given
        Recipe recipe = createBeefStew();

        // when
        recipes.update(recipe);

        // then
        assertThat(recipe.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);
        assertThat(recipes.find("TRUE").size()).isEqualTo(1);
        assertThat(authors.find("TRUE").size()).isEqualTo(1);
        assertThat(ingredients.find("TRUE").size()).isEqualTo(4);
    }

    @Test
    void testUpdateRecipe() {
        // given
        Recipe recipe = createBeefStew();
        recipes.update(recipe);

        // when
        recipe.remove(recipe.ingredients().get(0));
        recipe.remove(recipe.ingredients().get(0));
        recipe.author(null);
        recipes.update(recipe);

        // then
        assertThat(recipes.find("TRUE").size()).isEqualTo(1);
        assertThat(authors.find("TRUE").size()).isEqualTo(1);
        assertThat(ingredients.find("TRUE").size()).isEqualTo(2);
    }

    @Test
    void testDeleteRecipe() {
        // given
        Recipe recipe = createBeefStew();
        recipes.update(recipe);

        // when
        recipes.delete(recipe);

        // then
        assertThat(recipes.find("TRUE").size()).isEqualTo(0);
        assertThat(authors.find("TRUE").size()).isEqualTo(1);
        assertThat(ingredients.find("TRUE").size()).isEqualTo(0);
    }
}
