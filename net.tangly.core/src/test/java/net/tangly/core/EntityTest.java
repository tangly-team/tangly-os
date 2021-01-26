/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityTest {
    static class Entity extends QualifiedEntityImp {
        private Address address;
        private EmailAddress email;
        private PhoneNr phoneNr;

        static Entity of() {
            return new Entity();
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

    @Test
    void testHasInterval() {
        var entity = Entity.of();
        entity.fromDate(LocalDate.of(2000, Month.JANUARY, 1));
        entity.toDate(LocalDate.of(2000, Month.DECEMBER, 31));

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
        var entity = Entity.of();
        entity.address(Address.builder().country(COUNTRY).region("ZG").locality(LOCALITY).postcode("6300").street(STREET).poBox(POBOX).build());
        assertThat(entity.address().country()).isEqualTo(COUNTRY);
        assertThat(entity.address().poBox()).isEqualTo(POBOX);
        assertThat(entity.address().street()).isEqualTo(STREET);
        assertThat(entity.address().locality()).isEqualTo(LOCALITY);
        assertThat(Address.of(entity.address().text())).isEqualTo(entity.address());
    }

    @Test
    void testPhoneNr() {
        var entity = Entity.of();
        entity.phoneNr(PhoneNr.of("+41 79 123 45 78"));
        assertThat(PhoneNr.of(entity.phoneNr().toString())).isEqualTo(entity.phoneNr());
    }

    @Test
    void testModifyTags() {
        // Given
        var item = Entity.of();
        var tag = new Tag("namespace", "tag", "format");
        // When
        item.add(tag);
        // Then
        assertThat(item.tags().size()).isEqualTo(1);
        assertThat(item.tags().contains(tag)).isTrue();
        assertThat(item.findByNamespace("namespace").size()).isEqualTo(1);
        assertThat(item.findBy("namespace", "tag").isPresent()).isTrue();
        assertThat(item.findBy("namespace:tag").isPresent()).isTrue();
        assertThat(item.containsTag("namespace", "tag")).isTrue();
        assertThat(item.containsTag("namespace:tag")).isTrue();
        assertThat(item.value("namespace:tag").orElseThrow()).isEqualTo("format");

        // When
        item.remove(tag);
        // Then
        assertThat(item.tags().size()).isEqualTo(0);
        assertThat(item.findByNamespace("namespace").isEmpty()).isTrue();
        assertThat(item.findBy("namespace", "tag").isPresent()).isFalse();
        assertThat(item.value("namespace:tag").isEmpty()).isTrue();
    }

    @Test
    void testUpdateTags() {
        // Given
        var item = Entity.of();
        var tag = new Tag("namespace", "tag", "format");

        // When - Then
        assertThat(item.tags().size()).isEqualTo(0);
        item.update(tag);
        assertThat(item.tags().size()).isEqualTo(1);
        item.update(tag);
        assertThat(item.tags().size()).isEqualTo(1);
        item.update("namespace:tag", "format");
    }

    @Test
    void testSerializeTags() {
        var item = Entity.of();
        item.add(Tag.of("namespace", "tag", "format"));
        item.add(Tag.of("gis", "longitude", "0.0"));
        item.add(Tag.of("gis", "latitude", "0.0"));
        String rawTags = item.rawTags();
        Set<Tag> tags = item.tags();
        item.clearTags();
        assertThat(item.tags().isEmpty()).isTrue();
        item.rawTags(rawTags);
        assertThat(item.tags().isEmpty()).isFalse();
        assertThat(item.tags().size()).isEqualTo(3);
        assertThat(item.findByNamespace("gis").size()).isEqualTo(2);
    }

    @Test
    void testModifyComments() {
        // Given
        var item = Entity.of();
        var comment = new Comment("John Doe", "This is a comment");
        // When
        item.add(comment);
        // Then
        assertThat(item.comments().size()).isEqualTo(1);
        assertThat(item.comments().contains(comment)).isTrue();
        // When
        item.remove(comment);
        assertThat(item.comments().isEmpty()).isTrue();
    }

    @Test
    void testFilterComments() {
        // Given
        var item = Entity.of();
        var comment = Comment.of(LocalDateTime.of(1800, Month.JANUARY, 1, 0, 0), "John Doe", "This is comment 1");
        comment.add(new Tag("gis", "longitude", "0.0"));
        comment.add(new Tag("gis", "latitude", "0.0"));
        item.add(comment);
        comment = Comment.of(LocalDateTime.of(1900, Month.JANUARY, 1, 0, 0), "John Doe", "This is comment 2");
        comment.add(new Tag("gis", "longitude", "0.0"));
        item.add(comment);
        comment = Comment
            .of(LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0), HasOid.UNDEFINED_OID, "John Doe", "This is comment 3", Tag.of("gis", "longitude", "0.0"),
                Tag.of("gis", "latitude", "0.0"));
        item.add(comment);
        // When
        assertThat(item.findByAuthor("John Doe").size()).isEqualTo(3);

        assertThat(item.findByTime(LocalDateTime.MIN, LocalDateTime.MAX).size()).isEqualTo(3);
        assertThat(item.findByTime(LocalDateTime.of(1700, 1, 1, 0, 0), LocalDateTime.of(2100, 1, 1, 0, 0)).size()).isEqualTo(3);
        assertThat(item.findByTime(LocalDateTime.of(1799, 1, 1, 0, 0), LocalDateTime.of(2100, 1, 1, 0, 0)).size()).isEqualTo(3);
        assertThat(item.findByTime(LocalDateTime.of(1800, 1, 1, 0, 0), LocalDateTime.of(2000, 1, 1, 0, 0)).size()).isEqualTo(3);
        assertThat(item.findByTime(LocalDateTime.of(1900, 1, 1, 0, 0), LocalDateTime.of(2000, 1, 1, 0, 0)).size()).isEqualTo(2);
        assertThat(item.findByTime(LocalDateTime.of(1900, 1, 1, 0, 0), LocalDateTime.of(1990, 1, 1, 0, 0)).size()).isEqualTo(1);

        assertThat(item.findByTag("gis", "longitude").size()).isEqualTo(3);
        assertThat(item.findByTag("gis", "latitude").size()).isEqualTo(2);
        assertThat(item.findByTag("gis", "none").isEmpty()).isTrue();
    }
}
