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

package net.tangly.commons.orm;

import net.tangly.bus.codes.Code;
import net.tangly.bus.codes.CodeType;
import net.tangly.bus.core.Comment;
import net.tangly.bus.core.EntityImp;
import net.tangly.bus.core.HasOid;
import net.tangly.bus.core.Tag;
import net.tangly.commons.lang.Reference;
import org.flywaydb.core.Flyway;
import org.hsqldb.jdbc.JDBCDataSource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DaoTest {
    /**
     * Enumeration type extended to support the code interface.
     */
    private enum EntityCode implements Code {
        CODE_TEST_0, CODE_TEST_1, CODE_TEST_2;

        @Override
        public int id() {
            return this.ordinal();
        }

        @Override
        public String code() {
            return this.toString();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    static class Value {
        private int intValue;
        private String stringValue;

        public Value() {
        }

        public Value(int intValue, String stringValue) {
            this.intValue = intValue;
            this.stringValue = stringValue;
        }

        int intValue() {
            return intValue;
        }

        String stringValue() {
            return stringValue;
        }
    }

    static class Entity extends EntityImp {
        private Entity owner;
        private EntityCode code;
        private List<Entity> owned;
        private Long ownedBy;
        private Set<Value> valuess;

        public Entity() {
            owned = new ArrayList<>();
            valuess = new HashSet<>();
        }

        EntityCode code() {
            return code;
        }

        void code(EntityCode code) {
            this.code = code;
        }

        Entity owner() {
            return owner;
        }

        void owner(Entity owner) {
            this.owner = owner;
        }

        List<Entity> owned() {
            return owned;
        }

        void addOwned(Entity entity) {
            owned.add(entity);
        }

        void removeOwned(Entity entity) {
            owned.remove(entity);
        }

        void addValue(int intValue, String stringValue) {
            valuess.add(new Value(intValue, stringValue));
        }

        Set<Value> values() {
            return Collections.unmodifiableSet(valuess);
        }
    }

    private static String dbUrl = "jdbc:hsqldb:mem:tangly;sql.syntax_mys=true";
    private static String username = "SA";
    private static String password = "";

    private static JDBCDataSource datasource;
    private Dao<Entity> entities;
    private Dao<Comment> comments;

    @BeforeEach
    void setup() throws NoSuchMethodException {
        datasource = new JDBCDataSource();
        datasource.setDatabase(dbUrl);
        datasource.setUser(username);
        datasource.setPassword(password);
        var flyway = Flyway.configure().dataSource(datasource).load();
        flyway.migrate();
        comments = new DaoBuilder<Comment>(Comment.class).withOid().withDateTime("created").withText("author").withText("text").withTags("tags")
                .withFid("ownedBy").build("tangly", "comments", datasource);
        entities = new DaoBuilder<Entity>(Entity.class).withOid().withString("id").withString("name").withDate("fromDate").withDate("toDate")
                .withString("text").withTags("tags").withCode("code", CodeType.of(EntityCode.class, Arrays.asList(EntityCode.values())))
                .withOne2One("owner").withFid("ownedBy").withOne2Many("owned", "ownedBy")
                .withOne2Many("comments", "ownedBy", new Reference<>(comments)).withJson("valuess", Value.class)
                .build("tangly", "entities", datasource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection connection = datasource.getConnection(); Statement stmt = connection.createStatement()) {
            stmt.execute("shutdown");
        }
    }

    @Test
    void testCreateUpdateDeleteEntity() throws NoSuchMethodException {
        Entity entity = create(1, "2000-01-01", "2020-12-31");

        assertThat(entity.oid()).isEqualTo(HasOid.UNDEFINED_OID);
        entities.update(entity);
        assertThat(entity.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);
        long oid = entity.oid();

        Optional<Entity> retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity.orElseThrow());

        entities.clearCache();
        retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity.orElseThrow());

        final String updatedText = "Text Entity 1 updated";
        entity.text(updatedText);
        entities.update(entity);
        retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity.orElse(null));
        assertThat(retrievedEntity.get().text()).isEqualTo("Text Entity 1 updated");

        entities.clearCache();
        retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity.orElseThrow());
        assertThat(retrievedEntity.get().text()).isEqualTo("Text Entity 1 updated");

        entities.delete(oid);
        retrievedEntity = entities.find(oid);
        assertThat(retrievedEntity.isEmpty()).isTrue();

        entities.clearCache();
        assertThat(retrievedEntity.isEmpty()).isTrue();
    }

    @Test
    void testTags() throws NoSuchFieldException, NoSuchMethodException {
        Entity entity = create(10, "2020-01-01", "2020-12-31");
        entities.update(entity);
        long oid = entity.oid();
        entities.clearCache();
        Optional<Entity> retrieved = entities.find(oid);
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().tags().size()).isEqualTo(3);

        entity = retrieved.get();
        entity.removeTagNamed("test-C");
        entities.update(entity);
        entities.clearCache();
        retrieved = entities.find(oid);

        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().tags().size()).isEqualTo(2);

        entity = retrieved.get();
        entity.remove(entity.tags().stream().findAny().orElse(null));
        entities.update(entity);
        entities.clearCache();
        retrieved = entities.find(oid);

        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().tags().size()).isEqualTo(1);


        entity.clearTags();
        entities.update(entity);
        entities.clearCache();
        retrieved = entities.find(oid);

        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().tags().isEmpty()).isTrue();
    }

    @Test
    void testJson() {
        // Given
        Entity entity = create(10, "2020-01-01", "2020-12-31");
        entity.addValue(101, "Value 101");
        entity.addValue(102, "Value 102");
        entity.addValue(103, "Value 103");

        // when
        entities.update(entity);

        // then
        assertThat(entity.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);

        // when
        long oid = entity.oid();
        entities.clearCache();
        Optional<Entity> retrieved = entities.find(oid);

        // then
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().values().size()).isEqualTo(3);
        assertThat(retrieved.get().values().stream().anyMatch(o -> o.intValue() == 101)).isTrue();
        assertThat(retrieved.get().values().stream().anyMatch(o -> o.intValue() == 102)).isTrue();
        assertThat(retrieved.get().values().stream().anyMatch(o -> o.intValue() == 103)).isTrue();
        assertThat(retrieved.get().values().stream().anyMatch(o -> o.stringValue().equals("Value 101"))).isTrue();
        assertThat(retrieved.get().values().stream().anyMatch(o -> o.stringValue().equals("Value 102"))).isTrue();
        assertThat(retrieved.get().values().stream().anyMatch(o -> o.stringValue().equals("Value 103"))).isTrue();
    }

    @Test
    void testComments() throws NoSuchFieldException, NoSuchMethodException {
        // Given
        Entity entity = create(10, "2020-01-01", "2020-12-31");
        entity.add(Comment.of("John Doe", "This is text of comment 1"));
        entity.add(Comment.of("John Doe", "This is text of comment 2"));
        entity.add(Comment.of("John Doe", "This is text of comment 3"));

        // when
        entities.update(entity);

        // then
        assertThat(entity.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);

        // when
        long oid = entity.oid();
        entities.clearCache();
        Optional<Entity> retrieved = entities.find(oid);

        // then
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().comments().size()).isEqualTo(3);
        assertThat(retrieved.get().comments().stream().anyMatch(o -> o.oid() == HasOid.UNDEFINED_OID)).isFalse();
    }

    @Test
    void testOne2OneProperty() throws NoSuchFieldException, NoSuchMethodException {
        Entity owner = create(2, "2000-01-01", "2020-12-31");
        Entity ownee = create(1, "2000-01-01", "2020-12-31");
        ownee.owner(owner);

        assertThat(ownee.oid()).isEqualTo(HasOid.UNDEFINED_OID);
        entities.update(ownee);
        assertThat(ownee.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);
        assertThat(owner.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);
        long ownerOid = owner.oid();
        long oid = ownee.oid();

        Optional<Entity> retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity.orElseThrow());
        assertThat(ownee.owner()).isNotNull();
        entities.clearCache();

        retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity.orElseThrow());
        assertThat(ownee.owner()).isNotNull();

        entities.delete(oid);
        retrievedEntity = entities.find(oid);
        assertThat(retrievedEntity.isEmpty()).isTrue();
        retrievedEntity = entities.find(ownerOid);
        assertThat(retrievedEntity.isPresent()).isTrue();
    }

    @Test
    void testOne2ManyProperty() throws NoSuchFieldException, NoSuchMethodException {
        final int OWNED_NR = 5;
        Entity entity = create(100, "2000-01-01", "2020-12-31");
        for (int i = 0; i < OWNED_NR; i++) {
            entity.addOwned(create(i, "2000-01-01", "2020-12-31"));
        }

        assertThat(entity.oid()).isEqualTo(HasOid.UNDEFINED_OID);
        entities.update(entity);
        assertThat(entity.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);
        assertThat(entity.owned.stream().filter(o -> o.oid() == HasOid.UNDEFINED_OID).findAny().isEmpty()).isTrue();
        long oid = entity.oid();

        entities.clearCache();
        Optional<Entity> retrieved = entities.find(oid);
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().owned().size()).isEqualTo(OWNED_NR);

        entity = retrieved.get();
        entity.removeOwned(entity.owned().get(0));
        entity.removeOwned((entity.owned().get(0)));
        entity.removeOwned((entity.owned().get(0)));
        entity.addOwned(create(OWNED_NR + 1, "2000-01-01", "2020-12-31"));
        entities.update(entity);
        entities.clearCache();
        retrieved = entities.find(oid);
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().owned().size()).isEqualTo(OWNED_NR - 3 + 1);
    }

    @Test
    void testCodeProperty() throws NoSuchFieldException, NoSuchMethodException {
        Entity entity = create(20, "2000-01-01", "2020-12-31");
        entity.code(EntityCode.CODE_TEST_1);
        entities.update(entity);
        assertThat(entity.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);
        assertThat(entity.code()).isEqualTo(EntityCode.CODE_TEST_1);
        long oid = entity.oid();

        entities.clearCache();
        Optional<Entity> retrieved = entities.find(oid);
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().code()).isEqualTo(EntityCode.CODE_TEST_1);
    }

    private void testEntity(Entity entity) {
        assertThat(entity.id()).isEqualTo("Test Entity 1");
        assertThat(entity.name()).isEqualTo("Test Entity Name 1");
        assertThat(entity.fromDate()).isEqualTo(LocalDate.parse("2000-01-01"));
        assertThat(entity.toDate()).isEqualTo(LocalDate.parse("2020-12-31"));
        assertThat(entity.tags().size()).isEqualTo(3);
        assertThat(entity.findBy("test", "test-A").isPresent()).isTrue();
        assertThat(entity.findBy("test", "test-B").isPresent()).isTrue();
        assertThat(entity.findBy(null, "test-C").isPresent()).isTrue();
    }

    private Entity create(int number, @NotNull String fromDate, @NotNull String toDate) {
        Entity entity = new Entity();
        entity.id("Test Entity " + number);
        entity.name("Test Entity Name " + number);
        entity.fromDate(LocalDate.parse(fromDate));
        entity.toDate(LocalDate.parse(toDate));
        entity.add(Tag.of("test", "test-A", Integer.toString(number)));
        entity.add(Tag.ofEmpty("test", "test-B"));
        entity.add(Tag.ofEmpty("test-C"));
        assertThat(entity.oid()).isEqualTo(HasOid.UNDEFINED_OID);
        return entity;
    }
}
