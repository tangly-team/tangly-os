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

package net.tangly.crm.ports;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import net.fortuna.ical4j.vcard.Group;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import net.tangly.bus.core.Address;
import net.tangly.bus.core.EmailAddress;
import net.tangly.bus.core.PhoneNr;
import net.tangly.bus.crm.GenderCode;
import org.jetbrains.annotations.NotNull;

public class VCard2 {
    private final VCard card;

    public VCard2(@NotNull VCard card) {
        this.card = card;
    }

    public String name() {
        return property(Property.Id.KIND);
    }

    public String firstname() {
        return property(Property.Id.FN);
    }

    public String lastname() {
        return property(Property.Id.N);
    }

    public String formattedName() {
        return property(Property.Id.FN);
    }

    public EmailAddress homeEmail() {
        String email = property(Group.HOME, Property.Id.EMAIL);
        return null;
    }

    public EmailAddress workEmail() {
        String email = property(Group.WORK, Property.Id.EMAIL);
        return null;
    }

    public PhoneNr homePhone() {
        String email = property(Group.HOME, Property.Id.TEL);
        return null;
    }

    public PhoneNr workPhone() {
        String email = property(Group.WORK, Property.Id.TEL);
        return null;
    }

    public String title() {
        return property(Property.Id.TITLE);
    }

    public String organization() {
        return property(Property.Id.ORG);
    }

    public String note() {
        return property(Property.Id.NOTE);
    }

    public LocalDate birthday() {
        return date(Property.Id.BDAY);
    }

    public LocalDate deathday() {
        return date(Property.Id.DDAY);
    }

    public GenderCode gender() {
        String gender = property(Property.Id.GENDER);
        return switch (gender.charAt(0)) {
            case 'M' -> GenderCode.male;
            case 'F' -> GenderCode.female;
            case 'O' -> GenderCode.other;
            default -> GenderCode.unspecified;
        };
    }

    public Address homeAddress() {
        return address(Group.HOME);
    }

    public byte[] photo() {
        //        PHOTO;MEDIATYPE=image/jpeg:http://example.org/photo.jpg
        //        PHOTO:data:image/jpeg;base64,[base64-data]
        return null;
    }

    private LocalDate date(Property.Id id) {
        String date = property(id);
        if (date != null) {
            try {
                return LocalDate.parse(date);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }

    private Address address(Group group) {
        String text = property(group, Property.Id.ADR);
        if (text != null) {
            var parts = Objects.requireNonNull(text).split(";", -1);
            Objects.checkFromIndexSize(0, parts.length, 7);
            return Address.builder().poBox(parts[0]).extended(parts[1]).street(parts[2]).locality(parts[3]).region(parts[4]).postcode(parts[5])
                    .country(parts[6]).build();
        } else {
            return null;
        }
    }

    private String property(Property.Id id) {
        Property property = card.getProperty(id);
        return (property != null) ? property.getValue() : null;
    }

    private String property(Group group, Property.Id id) {
        return card.getProperties(id).stream().filter(o -> o.getGroup().equals(group)).findAny().map(Property::getValue).orElse(null);
    }
}
