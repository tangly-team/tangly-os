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

package net.tangly.orm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.tangly.core.HasOid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DaoOwnedAndReferencedEntitiesTest extends DaoTest {
    static class Entity implements HasOid {
        private long oid;
        private Long referencedBy;
        private Long ownedBy;

        private String name;
        private Entity owner;
        private Entity ownedOne;
        private Entity referencedOne;
        private final List<Entity> ownedOnes;
        private final List<Entity> referencedOnes;

        public Entity() {
            referencedOnes = new ArrayList<>();
            ownedOnes = new ArrayList<>();
        }

        static Entity of(String name) {
            Entity entity = new Entity();
            entity.name(name);
            return entity;
        }

        @Override
        public long oid() {
            return oid;
        }

        String name() {
            return name;
        }

        void name(String name) {
            this.name = name;
        }

        Entity owner() {
            return owner;
        }

        void owner(Entity owner) {
            this.owner = owner;
        }

        Entity referencedOne() {
            return referencedOne;
        }

        void referencedOne(Entity owned) {
            this.referencedOne = owned;
        }

        Entity ownedOne() {
            return ownedOne;
        }

        void ownedOne(Entity owned) {
            this.ownedOne = owned;
        }

        List<Entity> referencedOnes() {
            return Collections.unmodifiableList(referencedOnes);
        }

        void addReferenced(Entity entity) {
            referencedOnes.add(entity);
        }

        void removeReferenced(Entity entity) {
            referencedOnes.remove(entity);
        }

        List<Entity> ownedOnes() {
            return Collections.unmodifiableList(ownedOnes);
        }

        void addOwned(Entity entity) {
            ownedOnes.add(entity);
        }

        void removeOwned(Entity entity) {
            ownedOnes.remove(entity);
        }
    }

    private Dao<Entity> entities;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        setUpDatabase();
        DaoBuilder<Entity> entitiesBuilder = new DaoBuilder<>(Entity.class);
        entities = entitiesBuilder.withOid().withString("name").withOne2One("referencedOne", entitiesBuilder.self(), false)
                .withOne2One("ownedOne", entitiesBuilder.self(), true).withOne2Many("referencedOnes", "referencedBy", entitiesBuilder.self(), false)
                .withFid("referencedBy").withOne2Many("ownedOnes", "ownedBy", entitiesBuilder.self(), true).withFid("ownedBy")
                .build("ownedvsreferenced", "entities", datasource());
    }

    @Test
    public void testReferencedOne() {
        // given
        Entity root = Entity.of("root");
        Entity referencedOne = Entity.of("referenced by root");

        // when
        root.referencedOne(referencedOne);
        entities.update(root);

        // then
        long rootOid = root.oid;
        long referencedOid = root.referencedOne().oid();
        assertThat(rootOid).isNotEqualTo(HasOid.UNDEFINED_OID);
        assertThat(referencedOid).isNotEqualTo(HasOid.UNDEFINED_OID);
        assertThat(entities.find("TRUE").size()).isEqualTo(2);

        // when
        entities.clearCache();
        Optional<Entity> item = entities.find(rootOid);

        // then
        assertThat(item.isPresent()).isTrue();
        assertThat(entities.find("TRUE").size()).isEqualTo(2);

        // when
        root = entities.find(rootOid).orElse(null);
        root.referencedOne(null);
        entities.update(root);
        entities.clearCache();
        item = entities.find(rootOid);

        // then
        assertThat(item.isPresent()).isTrue();
        assertThat(entities.find("TRUE").size()).isEqualTo(2);

        // when
        root = entities.find(rootOid).orElse(null);
        root.referencedOne(entities.find(referencedOid).orElse(null));
        assertThat(root.referencedOne()).isNotNull();
        entities.update(root);
        assertThat(entities.find("TRUE").size()).isEqualTo(2);

        // then
        entities.delete(rootOid);
        entities.clearCache();
        assertThat(entities.find("TRUE").size()).isEqualTo(1);
    }

    @Test
    public void testOwnedOne() {
        // given
        Entity root = Entity.of("root");
        Entity ownedOne = Entity.of("owned by root");

        // when
        root.ownedOne(ownedOne);
        entities.update(root);

        // then
        long rootOid = root.oid;
        long onwedoid = root.ownedOne().oid();
        assertThat(rootOid).isNotEqualTo(HasOid.UNDEFINED_OID);
        assertThat(onwedoid).isNotEqualTo(HasOid.UNDEFINED_OID);

        // when
        entities.clearCache();
        Optional<Entity> item = entities.find(rootOid);

        // then
        assertThat(item.isPresent()).isTrue();
        assertThat(entities.find("TRUE").size()).isEqualTo(2);

        // when
        root = entities.find(rootOid).orElse(null);
        root.ownedOne(null);
        entities.update(root);
        entities.clearCache();
        item = entities.find(rootOid);

        // then
        assertThat(item.isPresent()).isTrue();
        assertThat(entities.find("TRUE").size()).isEqualTo(2);

        // when
        root = entities.find(rootOid).orElse(null);
        root.ownedOne(entities.find(onwedoid).orElse(null));
        assertThat(root.ownedOne()).isNotNull();
        entities.update(root);

        // then
        entities.delete(rootOid);
        entities.clearCache();
        assertThat(entities.find("TRUE").size()).isEqualTo(0);
    }

    @Test
    public void testReferencedOnes() {
        // given
        Entity root = Entity.of("root");
        root.addReferenced(Entity.of("referenced one by root"));
        root.addReferenced(Entity.of("referenced two by root"));
        root.addReferenced(Entity.of("referenced three by root"));

        // when
        entities.update(root);

        // then
        long rootOid = root.oid;
        assertThat(rootOid).isNotEqualTo(HasOid.UNDEFINED_OID);
        assertThat(entities.find("TRUE").size()).isEqualTo(4);

        // when
        entities.clearCache();
        Optional<Entity> item = entities.find(rootOid);

        // then
        assertThat(item.isPresent()).isTrue();
        assertThat(entities.find("TRUE").size()).isEqualTo(4);

        // when
        root = entities.find(rootOid).orElse(null);
        List.copyOf(root.referencedOnes()).forEach(root::removeReferenced);
        entities.update(root);
        entities.clearCache();
        item = entities.find(rootOid);

        // then
        assertThat(item.isPresent()).isTrue();
        assertThat(entities.find("TRUE").size()).isEqualTo(4);

        // when
        final Entity theRoot = entities.find(rootOid).orElse(null);
        entities.find("TRUE").forEach(t -> {
            if (theRoot.oid() != rootOid) {
                theRoot.addReferenced(t);
            }
        });
        entities.update(root);
        assertThat(entities.find("TRUE").size()).isEqualTo(4);

        // then
        entities.delete(theRoot);
        entities.clearCache();
        assertThat(entities.find("TRUE").size()).isEqualTo(3);

    }
}
