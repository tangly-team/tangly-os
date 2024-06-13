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

package net.tangly.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EntityTest {
    static class NamedEntity extends MutableEntityExtendedImp {
        private Address address;
        private EmailAddress email;
        private PhoneNr phoneNr;

        static NamedEntity of(long oid) {
            return new NamedEntity(oid);
        }

        NamedEntity(long oid) {
            super(oid);
        }

        public Address address() {
            return address;
        }

        public void address(Address address) {
            this.address = address;
        }

        public PhoneNr phoneNr() {
            return phoneNr;
        }

        public void phoneNr(PhoneNr phoneNr) {
            this.phoneNr = phoneNr;
        }
    }

    static class ExternalEntity extends MutalbeExternalEntityImp {
        static ExternalEntity of(String id) {
            return new ExternalEntity(id);
        }

        ExternalEntity(String id) {
            super(id);
        }
    }

    @Test
    void testHasInterval() {
        var entity = NamedEntity.of(MutableEntity.UNDEFINED_OID);
        entity.range(DateRange.of(LocalDate.of(2000, Month.JANUARY, 1), LocalDate.of(2000, Month.DECEMBER, 31)));
        assertThat(entity.isActive(LocalDate.of(1999, Month.DECEMBER, 31))).isFalse();
        assertThat(entity.isActive(LocalDate.of(2000, Month.JANUARY, 1))).isTrue();
        assertThat(entity.isActive(LocalDate.of(2000, Month.JUNE, 1))).isTrue();
        assertThat(entity.isActive(LocalDate.of(2000, Month.DECEMBER, 31))).isTrue();
        assertThat(entity.isActive(LocalDate.of(2001, Month.JANUARY, 1))).isFalse();
        assertThat(entity.isActive()).isFalse();
    }

    @Test
    void testAddress() {
        final String COUNTRY = "Switzerland";
        final String LOCALITY = "Zug";
        final String POBOX = "Postfach 101";
        final String STREET = "Rigistrasse 1";
        var entity = NamedEntity.of(MutableEntity.UNDEFINED_OID);
        entity.address(Address.builder().country(COUNTRY).region("ZG").locality(LOCALITY).postcode("6300").street(STREET).poBox(POBOX).build());
        assertThat(entity.address().country()).isEqualTo(COUNTRY);
        assertThat(entity.address().poBox()).isEqualTo(POBOX);
        assertThat(entity.address().street()).isEqualTo(STREET);
        assertThat(entity.address().locality()).isEqualTo(LOCALITY);
        assertThat(Address.of(entity.address().text())).isEqualTo(entity.address());
    }

    @Test
    void testPhoneNr() {
        var entity = NamedEntity.of(MutableEntity.UNDEFINED_OID);
        entity.phoneNr(PhoneNr.of("+41 79 123 45 78"));
        assertThat(PhoneNr.of(entity.phoneNr().toString())).isEqualTo(entity.phoneNr());
    }

    @Test
    void testModifyTags() {
        testModifyTags(NamedEntity.of(MutableEntity.UNDEFINED_OID));
        testModifyTags(ExternalEntity.of("id"));
    }

    private void testModifyTags(HasMutableTags hasTags) {
        // Given
        var tag = new Tag("namespace", "tag", "format");
        // When
        hasTags.add(tag);
        // Then
        assertThat(hasTags.tags()).hasSize(1);
        assertThat(hasTags.tags()).contains(tag);
        assertThat(hasTags.findByNamespace("namespace").size()).isEqualTo(1);
        assertThat(hasTags.findBy("namespace", "tag")).isPresent();
        assertThat(hasTags.findBy("namespace:tag")).isPresent();
        assertThat(hasTags.containsTag("namespace", "tag")).isTrue();
        assertThat(hasTags.containsTag("namespace:tag")).isTrue();
        assertThat(hasTags.value("namespace:tag").orElseThrow()).isEqualTo("format");

        // When
        hasTags.remove(tag);
        // Then
        assertThat(hasTags.tags()).isEmpty();
        assertThat(hasTags.findByNamespace("namespace")).isEmpty();
        assertThat(hasTags.findBy("namespace", "tag")).isNotPresent();
        assertThat(hasTags.value("namespace:tag")).isEmpty();
    }


    @Test
    void testUpdateTags() {
        testUpdateTags(NamedEntity.of(MutableEntity.UNDEFINED_OID));
        testUpdateTags(ExternalEntity.of("id"));
    }

    void testUpdateTags(HasMutableTags hasTags) {
        // Given
        var tag = new Tag("namespace", "tag", "format");

        // When - Then
        assertThat(hasTags.tags()).isEmpty();
        hasTags.update(tag);
        assertThat(hasTags.tags()).hasSize(1);
        hasTags.update(tag);
        assertThat(hasTags.tags()).hasSize(1);
        hasTags.update("namespace:tag", "format");
    }

    @Test
    void testSerializeTags() {
        testSerializeTags(NamedEntity.of(MutableEntity.UNDEFINED_OID));
        testSerializeTags(ExternalEntity.of("id"));
    }

    private void testSerializeTags(HasMutableTags hasTags) {
        hasTags.add(Tag.of("namespace", "tag", "format"));
        hasTags.add(Tag.of("gis", "longitude", "0.0"));
        hasTags.add(Tag.of("gis", "latitude", "0.0"));
        String rawTags = hasTags.rawTags();
        Collection<Tag> tags = hasTags.tags();
        hasTags.clear();
        assertThat(hasTags.tags()).isEmpty();
        hasTags.rawTags(rawTags);
        assertThat(hasTags.tags()).isNotEmpty();
        assertThat(hasTags.tags()).hasSize(3);
        assertThat(hasTags.findByNamespace("gis")).hasSize(2);
    }

    @Test
    void testModifyComments() {
        testModifyComments(NamedEntity.of(MutableEntity.UNDEFINED_OID));
        testModifyComments(ExternalEntity.of("id"));
    }

    private void testModifyComments(HasMutableComments hasComments) {
        // Given
        var comment = Comment.of("John Doe", "This is a comment");
        // When
        hasComments.add(comment);
        // Then
        assertThat(hasComments.comments()).hasSize(1);
        assertThat(hasComments.comments()).contains(comment);
        // When
        hasComments.remove(comment);
        assertThat(hasComments.comments()).isEmpty();
    }

    @Test
    void testFilterComments() {
        testFilterComments(NamedEntity.of(MutableEntity.UNDEFINED_OID));
        testFilterComments(ExternalEntity.of("id"));
    }

    private void testFilterComments(HasMutableComments hasComments) {
        // Given
        var comment = new Comment(LocalDateTime.of(1800, Month.JANUARY, 1, 0, 0), "John Doe", "This is comment 1",
            Set.of(Tag.of("gis", "longitude", "0.0"), Tag.of("gis", "latitude", "0.0")));
        hasComments.add(comment);
        comment = new Comment(LocalDateTime.of(1900, Month.JANUARY, 1, 0, 0), "John Doe", "This is comment 2", Set.of(Tag.of("gis", "longitude", "0.0")));
        hasComments.add(comment);
        comment = new Comment(LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0), "John Doe", "This is comment 3",
            Set.of(Tag.of("gis", "longitude", "0.0"), Tag.of("gis", "latitude", "0.0")));
        hasComments.add(comment);
        // When
        assertThat(hasComments.findByAuthor("John Doe")).hasSize(3);

        assertThat(hasComments.findByTime(LocalDateTime.MIN, LocalDateTime.MAX)).hasSize(3);
        assertThat(hasComments.findByTime(LocalDateTime.of(1700, 1, 1, 0, 0), LocalDateTime.of(2100, 1, 1, 0, 0))).hasSize(3);
        assertThat(hasComments.findByTime(LocalDateTime.of(1799, 1, 1, 0, 0), LocalDateTime.of(2100, 1, 1, 0, 0))).hasSize(3);
        assertThat(hasComments.findByTime(LocalDateTime.of(1800, 1, 1, 0, 0), LocalDateTime.of(2000, 1, 1, 0, 0))).hasSize(3);
        assertThat(hasComments.findByTime(LocalDateTime.of(1900, 1, 1, 0, 0), LocalDateTime.of(2000, 1, 1, 0, 0))).hasSize(2);
        assertThat(hasComments.findByTime(LocalDateTime.of(1900, 1, 1, 0, 0), LocalDateTime.of(1990, 1, 1, 0, 0))).hasSize(1);

        assertThat(hasComments.findByTag("gis", "longitude")).hasSize(3);
        assertThat(hasComments.findByTag("gis", "latitude")).hasSize(2);
        assertThat(hasComments.findByTag("gis", "none")).isEmpty();
    }
}
