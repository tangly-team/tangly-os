/*
 * Copyright 2023-2023 Marcel Baumann
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

package net.tangly.app.domain.model;

import net.tangly.core.Comment;
import net.tangly.core.Entity;
import net.tangly.core.EntityImp;
import net.tangly.core.HasComments;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasOid;
import net.tangly.core.HasTags;
import net.tangly.core.HasText;
import net.tangly.core.HasTimeInterval;
import net.tangly.core.Tag;
import net.tangly.core.TagType;
import net.tangly.core.TypeRegistry;
import net.tangly.core.codes.Code;
import net.tangly.core.codes.CodeType;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.Handler;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Defines a test bed for the vaadin user interface components library. The entity classes implement the whole entity interfaces to validate the visualization components. One2one
 * and One2Many relations are also tested.
 * <p>Read-only mode is also exercised for exhaustive validation of the user interface library.</p>
 */
public class BoundedDomainEntities
    extends BoundedDomain<BoundedDomainEntities.AppRealm, BoundedDomainEntities.AppBusinessLogic, BoundedDomainEntities.AppHandler, BoundedDomainEntities.AppPort> {
    public static final String DOMAIN = "Entities";
    public static final String TAG_MANDATORY_STRING_VALUE = "tag-mandatory-string-value";
    public static final String TAG_OPTIONAL_STRING_VALUE = "tag-optional-string-value";
    public static final String TAG_WITH_NO_VALUE = "tag-with-no-value";

    private BoundedDomainEntities(AppRealm realm, AppBusinessLogic logic, AppHandler handler, AppPort port) {
        super(DOMAIN, realm, logic, handler, port, new TypeRegistry());
        registry().register(TagType.ofMandatoryString(DOMAIN, TAG_MANDATORY_STRING_VALUE));
        registry().register(TagType.ofOptionalString(DOMAIN, TAG_OPTIONAL_STRING_VALUE));
        registry().register(TagType.ofWithNoValue(DOMAIN, TAG_WITH_NO_VALUE));
        registry().register(CodeType.of(ActivityCode.class));
    }

    public static BoundedDomainEntities create() {
        AppRealm realm = new AppRealm();
        return new BoundedDomainEntities(realm, new AppBusinessLogic(), new AppHandler(realm), new AppPort());
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, EntityThree.class, realm().threeEntities()), new DomainEntity<>(DOMAIN, EntityFour.class, realm().fourEntities()));
    }

    /**
     * Define a code to test and demonstrate the application code support features.
     */
    public enum ActivityCode implements Code {
        talk, meeting, email, letter, audiocall, videocall, chat, campaign;

        @Override
        public int id() {
            return this.ordinal();
        }

        @Override
        public String code() {
            return this.name();
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    /**
     * Entity Three demonstrates oid, id, name, time interval, text, comment and tag features.
     */
    public static class EntityThree extends EntityImp implements HasOid, HasId, HasName, HasTimeInterval, HasText, HasComments, HasTags, Entity {
        public EntityThree(long oid) {
            super(oid);
        }
    }

    /**
     * Entity Four demonstrates entity abstraction and code features. An entity has an oid, id, name, time interval, text, comments and tags. The one-to-one and one-to-many
     * relations are also demonstrated.
     */
    public static class EntityFour extends EntityImp implements Entity {
        private final List<EntityThree> one2many;
        private EntityThree one2one;
        private ActivityCode activity;

        public EntityFour(long oid) {
            super(oid);
            one2many = new ArrayList<>();
        }

        public EntityThree one2one() {
            return one2one;
        }

        public void one2one(EntityThree one2one) {
            this.one2one = one2one;
        }

        public ActivityCode activity() {
            return activity;
        }

        public void activity(ActivityCode activity) {
            this.activity = activity;
        }

        public List<EntityThree> one2many() {
            return Collections.unmodifiableList(one2many);
        }

        public void one2many(Collection<EntityThree> one2many) {
            this.one2many.clear();
            this.one2many.addAll(one2many);
        }
    }

    public static class AppRealm implements Realm {
        private final ProviderInMemory<EntityThree> providerThree;
        private final ProviderInMemory<EntityFour> providerFour;

        public AppRealm() {
            providerThree = new ProviderInMemory<>();
            providerFour = new ProviderInMemory<>();
            load();
        }

        public Provider<EntityThree> threeEntities() {
            return providerThree;
        }

        public Provider<EntityFour> fourEntities() {
            return providerFour;
        }

        private void load() {
            IntStream.rangeClosed(1, 100).forEach(o -> {
                var entity = createEntityThree(o, Integer.toString(o), "Entity three-" + o, LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2000, Month.DECEMBER, 31),
                    "Entity _three_ text-" + o);
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 12, 12), "John Doee", "First comment for _entity_ three " + o));
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 12, 15), "John Doee", "Second comment for _entity_ three " + o));
                entity.add(Tag.of(DOMAIN, TAG_MANDATORY_STRING_VALUE, "value" + o));
                entity.add(Tag.of(DOMAIN, TAG_OPTIONAL_STRING_VALUE, ((o % 2) == 0) ? "value-optional-" + o : null));
                entity.add(Tag.ofEmpty(DOMAIN, TAG_WITH_NO_VALUE));
                threeEntities().update(entity);
            });
            IntStream.rangeClosed(1, 100).forEach(o -> {
                var entity = createEntityFour(o, Integer.toString(o), "Entity four-" + o, LocalDate.of(2010, Month.JANUARY, 1), LocalDate.of(2010, Month.DECEMBER, 31),
                    "Entity _four_ text-" + o);
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 18, 18), "John Doe", "First comment for entity four " + o));
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 18, 18), "John Doe", "Second comment for entity four " + o));
                entity.add(Tag.of(DOMAIN, TAG_MANDATORY_STRING_VALUE, "value" + o));
                entity.add(Tag.of(DOMAIN, TAG_OPTIONAL_STRING_VALUE, ((o % 2) == 0) ? "value-optional-" + o : null));
                entity.add(Tag.ofEmpty(DOMAIN, TAG_WITH_NO_VALUE));
                entity.one2one(Provider.findByOid(threeEntities(), o).get());
                fourEntities().update(entity);
            });
        }

        private EntityThree createEntityThree(long oid, String id, String name, LocalDate from, LocalDate to, String text) {
            return EntityImp.init(new EntityThree(oid), id, name, from, to, text);
        }

        private EntityFour createEntityFour(long oid, String id, String name, LocalDate from, LocalDate to, String text) {
            EntityFour entity = EntityImp.init(new EntityFour(oid), id, name, from, to, text);
            entity.activity(ActivityCode.audiocall);
            return entity;
        }
    }

    public static class AppBusinessLogic {
    }

    public static class AppHandler implements Handler<AppRealm> {
        private final AppRealm realm;

        public AppHandler(AppRealm realm) {
            this.realm = realm;
        }

        public AppRealm realm() {
            return realm;
        }
    }

    public static class AppPort {
    }
}