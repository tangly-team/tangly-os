/*
 * Copyright 2006-2018 Marcel Baumann
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

package net.tangly.commons.activerecords;

import net.tangly.commons.models.Comment;
import net.tangly.commons.models.EntityImp;
import net.tangly.commons.models.HasOid;
import net.tangly.commons.models.Tag;
import org.flywaydb.core.Flyway;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TableTest {
    static class Entity extends EntityImp {
        private Entity reference;

        public Entity() {
        }

        public Entity reference() {
            return reference;
        }

        public void reference(Entity reference) {
            this.reference = reference;
        }
    }

    private static String dbUrl = "jdbc:hsqldb:mem:commons;sql.syntax_mys=true";
    private static String username = "SA";
    private static String password = "";

    private static JDBCDataSource dataSource;

    @BeforeAll
    static void tearUp() {
        dataSource = new JDBCDataSource();
        dataSource.setDatabase(dbUrl);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        var flyway = Flyway.configure().dataSource(dataSource).load();
        flyway.migrate();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement stmt = connection.createStatement()) {
            stmt.execute("shutdown");
        }
    }

    @Test
    void testTable() throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException {
        Table<Entity> entities = new Table.Builder<>("tangly", "entities", Entity.class, dataSource).ofOid().ofString("id").ofString("name")
                .ofDate("fromDate").ofDate("toDate").ofString("text").ofTags("tags").ofOne2One("reference").build();

        Table<Comment> comments = new Table.Builder<>("tangly", "comments", Comment.class, dataSource).ofOid().ofFid("foid").ofDateTime("created")
                .ofString("text").ofTags("tags").build();

        Entity reference = new Entity();
        reference.id("Test Entity 2");
        reference.name("Test Entity Name 2");
        reference.fromDate(LocalDate.parse("2000-01-01"));
        reference.toDate(LocalDate.parse("2020-12-31"));
        reference.add(Tag.of("Test", "reference-1"));
        assertThat(reference.oid()).isEqualTo(HasOid.UNDEFINED_OID);
        entities.update(reference);
        assertThat(reference.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);
        long referenceOid = reference.oid();

        Entity entity = new Entity();
        entity.id("Test Entity 1");
        entity.name("Test Entity Name 1");
        entity.fromDate(LocalDate.parse("2000-01-01"));
        entity.toDate(LocalDate.parse("2020-12-31"));
        entity.add(Tag.of("Test", "test-1", "value"));
        entity.add(Tag.of("Test", "test-2"));
        entity.reference(reference);
        assertThat(entity.oid()).isEqualTo(HasOid.UNDEFINED_OID);
        entities.update(entity);
        assertThat(entity.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);
        long oid = entity.oid();

        Optional<Entity> retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity);

        entities.clearCache();
        retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity);

        final String updatedText = "Text Entity 1 updated";
        entity.text(updatedText);
        entities.update(entity);
        retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity);
        assertThat(retrievedEntity.get().text()).isEqualTo("Text Entity 1 updated");

        entities.clearCache();
        retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity);

        entities.delete(oid);
        retrievedEntity = entities.find(oid);
        assertThat(retrievedEntity.isEmpty()).isTrue();

        entities.clearCache();
        assertThat(retrievedEntity.isEmpty()).isTrue();
        retrievedEntity = entities.find(referenceOid);
        assertThat(retrievedEntity.isPresent()).isTrue();
    }

    private void testEntity(Optional<Entity> entity) {
        assertThat(entity.isPresent()).isTrue();
        assertThat(entity.get().id()).isEqualTo("Test Entity 1");
        assertThat(entity.get().name()).isEqualTo("Test Entity Name 1");
        assertThat(entity.get().fromDate()).isEqualTo(LocalDate.parse("2000-01-01"));
        assertThat(entity.get().toDate()).isEqualTo(LocalDate.parse("2020-12-31"));
        assertThat(entity.get().tags().size()).isEqualTo(2);
        assertThat(entity.get().findBy("Test", "test-1").isPresent()).isTrue();
        assertThat(entity.get().findBy("Test", "test-2").isPresent()).isTrue();
        assertThat(entity.get().reference()).isNotNull();
    }
}
