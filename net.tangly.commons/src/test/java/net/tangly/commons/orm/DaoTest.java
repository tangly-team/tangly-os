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

package net.tangly.commons.orm;

import net.tangly.commons.codes.Code;
import net.tangly.commons.codes.CodeType;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DaoTest {
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

    static class Entity extends EntityImp {
        private Entity owner;

        private EntityCode code;
        private List<Entity> owned;
        private Long ownedBy;

        public Entity() {
            owned = new ArrayList<>();
        }

        public EntityCode code() {
            return code;
        }

        public void code(EntityCode code) {
            this.code = code;
        }

        public Entity owner() {
            return owner;
        }

        public void owner(Entity owner) {
            this.owner = owner;
        }

        public List<Entity> owned() {
            return owned;
        }

        public void addOwned(Entity entity) {
            owned.add(entity);
        }

        public void removeOwned(Entity entity) {
            owned.remove(entity);
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
    void testCreateUpdateDeleteEntity() throws NoSuchFieldException, NoSuchMethodException {
        Dao<Entity> entities = createTable();

        Entity entity = create(1, "2000-01-01", "2020-12-31");

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
        assertThat(retrievedEntity.get().text()).isEqualTo("Text Entity 1 updated");

        entities.delete(oid);
        retrievedEntity = entities.find(oid);
        assertThat(retrievedEntity.isEmpty()).isTrue();

        entities.clearCache();
        assertThat(retrievedEntity.isEmpty()).isTrue();
    }

    @Test
    public void testTags() throws NoSuchFieldException, NoSuchMethodException {
        Dao<Entity> entities = createTable();

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
    public void testJsonProperty() throws NoSuchFieldException, NoSuchMethodException {
        Dao<Entity> entities = createTable();

        Entity entity = create(10, "2020-01-01", "2020-12-31");
        entity.add(Comment.of("John Doe", "This is text of comment 1"));
        entity.add(Comment.of("John Doe", "This is text of comment 2"));
        entity.add(Comment.of("John Doe", "This is text of comment 3"));
        entities.update(entity);
        assertThat(entity.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);
        long oid = entity.oid();
        entities.clearCache();
        Optional<Entity> retrieved = entities.find(oid);
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().comments().size()).isEqualTo(3);
    }

    @Test
    public void testOne2OneProperty() throws NoSuchFieldException, NoSuchMethodException {
        Dao<Entity> entities = createTable();

        Entity owner = create(2, "2000-01-01", "2020-12-31");
        Entity entity = create(1, "2000-01-01", "2020-12-31");
        entity.owner(owner);

        assertThat(entity.oid()).isEqualTo(HasOid.UNDEFINED_OID);
        entities.update(entity);
        assertThat(entity.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);
        assertThat(owner.oid()).isNotEqualTo(HasOid.UNDEFINED_OID);
        long ownerOid = owner.oid();
        long oid = entity.oid();

        Optional<Entity> retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity);
        assertThat(entity.owner()).isNotNull();
        entities.clearCache();

        retrievedEntity = entities.find(oid);
        testEntity(retrievedEntity);
        assertThat(entity.owner()).isNotNull();

        entities.delete(oid);
        retrievedEntity = entities.find(oid);
        assertThat(retrievedEntity.isEmpty()).isTrue();
        retrievedEntity = entities.find(ownerOid);
        assertThat(retrievedEntity.isPresent()).isTrue();
    }

    @Test
    public void testOne2ManyProperty() throws NoSuchFieldException, NoSuchMethodException {
        final int OWNED_NR = 5;
        Dao<Entity> entities = createTable();

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
    public void testCodeProperty() throws NoSuchFieldException, NoSuchMethodException {
        Dao<Entity> entities = createTable();

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

    private Dao<Entity> createTable() throws NoSuchMethodException {
        return new Dao.Builder<>("tangly", "entities", Entity.class, dataSource).withOid().withString("id").withString("name").withDate("fromDate")
                .withDate("toDate").withString("text").withTags("tags").withJson("comments", Comment.class, true)
                .withCode("code", CodeType.of(EntityCode.class, Arrays.asList(EntityCode.values()))).withOne2One("owner").withFid("ownedBy")
                .withOne2Many("owned", "ownedBy").build();
    }


    private void testEntity(Optional<Entity> entity) {
        assertThat(entity.isPresent()).isTrue();
        assertThat(entity.get().id()).isEqualTo("Test Entity 1");
        assertThat(entity.get().name()).isEqualTo("Test Entity Name 1");
        assertThat(entity.get().fromDate()).isEqualTo(LocalDate.parse("2000-01-01"));
        assertThat(entity.get().toDate()).isEqualTo(LocalDate.parse("2020-12-31"));
        assertThat(entity.get().tags().size()).isEqualTo(3);
        assertThat(entity.get().findBy("test", "test-A").isPresent()).isTrue();
        assertThat(entity.get().findBy("test", "test-B").isPresent()).isTrue();
        assertThat(entity.get().findBy(null, "test-C").isPresent()).isTrue();
    }

    private Entity create(int number, String fromDate, String toDate) {
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
