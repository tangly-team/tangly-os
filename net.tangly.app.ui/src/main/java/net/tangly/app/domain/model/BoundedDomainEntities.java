/*
 * Copyright 2023-2024 Marcel Baumann
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

import net.tangly.core.*;
import net.tangly.core.codes.Code;
import net.tangly.core.codes.CodeType;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.Port;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Defines a test bed for the vaadin user interface components library. The entity classes implement the whole entity interfaces to validate the visualization components. One2one
 * and One2Many relations are also tested.
 * <p>Read-only mode is also exercised for exhaustive validation of the user interface library.</p>
 */
public class BoundedDomainEntities
    extends BoundedDomain<BoundedDomainEntities.AppRealm, BoundedDomainEntities.AppBusinessLogic, BoundedDomainEntities.AppPort> {
    public static final String DOMAIN = "entities";
    public static final String TAG_MANDATORY_STRING_VALUE = "tag-mandatory-string-value";
    public static final String TAG_OPTIONAL_STRING_VALUE = "tag-optional-string-value";
    public static final String TAG_WITH_NO_VALUE = "tag-with-no-value";

    private BoundedDomainEntities(AppRealm realm, AppBusinessLogic logic, AppPort port) {
        super(DOMAIN, realm, logic, port, new TypeRegistry());
        registry().register(TagType.ofMandatoryString(DOMAIN, TAG_MANDATORY_STRING_VALUE));
        registry().register(TagType.ofOptionalString(DOMAIN, TAG_OPTIONAL_STRING_VALUE));
        registry().register(TagType.ofWithNoValue(DOMAIN, TAG_WITH_NO_VALUE));
        registry().register(CodeType.of(ActivityCode.class));
    }

    public static BoundedDomainEntities create() {
        AppRealm realm = new AppRealm();
        return new BoundedDomainEntities(realm, new AppBusinessLogic(), new AppPort(realm));
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, EntityThree.class, realm().threeEntities()), new DomainEntity<>(DOMAIN, EntityFour.class, realm().fourEntities()));
    }

    @Override
    public Map<TagType<?>, Integer> countTags(@NotNull Map<TagType<?>, Integer> counts) {
        addTagCounts(registry(), realm().threeEntities(), counts);
        addTagCounts(registry(), realm().fourEntities(), counts);
        return counts;
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
    public static class EntityThree extends EntityExtendedImp implements EntityExtended {
        public EntityThree(long oid) {
            super(oid);
        }
    }

    /**
     * Entity Four demonstrates entity abstraction and code features. An entity has an oid, id, name, time interval, text, comments and tags. The one-to-one and one-to-many
     * relations are also demonstrated.
     */
    public static class EntityFour extends EntityExtendedImp implements EntityExtended {
        private ActivityCode activity;
        private EntityThree one2one;
        private final List<EntityThree> one2many;

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

        public void addOne2Many(EntityThree entity) {
            one2many.add(entity);
        }

        public void removeOne2Many(EntityThree entity) {
            one2many.remove(entity);
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
            final int NR_OF_ITEMS = 100;
            IntStream.rangeClosed(1, NR_OF_ITEMS).forEach(o -> {
                var entity = createEntityThree(o, Integer.toString(o), STR."Entity three-\{o}", LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2000, Month.DECEMBER, 31),
                    STR."Entity _three_ text-\{o}");
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 12, 12), "John Doe", STR."First comment for _entity_ three \{o}"));
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 12, 15), "John Doe", STR."Second comment for _entity_ three \{o}"));
                entity.add(Tag.of(DOMAIN, TAG_MANDATORY_STRING_VALUE, STR."value\{o}"));
                entity.add(Tag.of(DOMAIN, TAG_OPTIONAL_STRING_VALUE, ((o % 2) == 0) ? STR."value-optional-\{o}" : null));
                entity.add(Tag.ofEmpty(DOMAIN, TAG_WITH_NO_VALUE));
                threeEntities().update(entity);
            });
            IntStream.rangeClosed(1, NR_OF_ITEMS).forEach(o -> {
                var entity = createEntityFour(o, Integer.toString(o), STR."Entity four-\{o}", LocalDate.of(2010, Month.JANUARY, 1), LocalDate.of(2010, Month.DECEMBER, 31),
                    STR."Entity _four_ text-\{o}");
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 18, 18), "John Doe", STR."First comment for entity four \{o}"));
                entity.add(Comment.of(LocalDateTime.of(2000, Month.JANUARY, 1, 18, 18), "John Doe", STR."Second comment for entity four \{o}"));
                entity.add(Tag.of(DOMAIN, TAG_MANDATORY_STRING_VALUE, STR."value\{o}"));
                entity.add(Tag.of(DOMAIN, TAG_OPTIONAL_STRING_VALUE, ((o % 2) == 0) ? STR."value-optional-\{o}" : null));
                entity.add(Tag.ofEmpty(DOMAIN, TAG_WITH_NO_VALUE));
                entity.one2one(Provider.findByOid(threeEntities(), o).orElseThrow());
                entity.addOne2Many(Provider.findByOid(threeEntities(), o).orElseThrow());
                if (o < (NR_OF_ITEMS - 3)) {
                    entity.addOne2Many(Provider.findByOid(threeEntities(), o + 1).orElseThrow());
                    entity.addOne2Many(Provider.findByOid(threeEntities(), o + 2).orElseThrow());
                    entity.addOne2Many(Provider.findByOid(threeEntities(), o + 3).orElseThrow());
                }
                fourEntities().update(entity);
            });
        }

        private EntityThree createEntityThree(long oid, String id, String name, LocalDate from, LocalDate to, String text) {
            return EntityExtendedImp.init(new EntityThree(oid), id, name, from, to, text);
        }

        private EntityFour createEntityFour(long oid, String id, String name, LocalDate from, LocalDate to, String text) {
            EntityFour entity = EntityExtendedImp.init(new EntityFour(oid), id, name, from, to, text);
            entity.activity(ActivityCode.audiocall);
            return entity;
        }
    }

    public static class AppBusinessLogic {
    }

    public static class AppPort implements Port<AppRealm> {
        private final AppRealm realm;

        public AppPort(AppRealm realm) {
            this.realm = realm;
        }

        public AppRealm realm() {
            return realm;
        }
    }

}
