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

package net.tangly.erp.crm.ports;

import net.fortuna.ical4j.vcard.Group;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import net.tangly.commons.lang.Strings;
import net.tangly.core.Address;
import net.tangly.core.EmailAddress;
import net.tangly.core.PhoneNr;
import net.tangly.erp.crm.domain.GenderCode;
import net.tangly.erp.crm.domain.Photo;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

public class VCard2 {
    private final VCard card;

    public VCard2(@NotNull VCard card) {
        this.card = card;
    }

    public String firstname() {
        return card.getProperties(Property.Id.FN).stream().map(Property::getValue).map(o -> o.split(";")[1]).findAny().orElse(null);
    }

    public String lastname() {
        return card.getProperties(Property.Id.N).stream().map(Property::getValue).map(o -> o.split(";")[0]).findAny().orElse(null);
    }

    public String formattedName() {
        return card.getProperties(Property.Id.N).stream().map(Property::getValue).findAny().orElse(null);
    }

    public Optional<EmailAddress> homeEmail() {
        return card.getProperties(Property.Id.EMAIL).stream()
            .filter(o -> containsParameterType(o, Parameter.Id.TYPE, "INTERNET") && containsParameterType(o, Parameter.Id.TYPE, "HOME"))
            .map(Property::getValue).map(EmailAddress::of).findAny();
    }

    public Optional<EmailAddress> workEmail() {
        return card.getProperties(Property.Id.EMAIL).stream()
            .filter(o -> containsParameterType(o, Parameter.Id.TYPE, "INTERNET") && containsParameterType(o, Parameter.Id.TYPE, "WORK"))
            .map(Property::getValue).map(EmailAddress::of).findAny();
    }

    public Optional<PhoneNr> homePhone() {
        return card.getProperties(Property.Id.TEL).stream()
            .filter(o -> containsParameterType(o, Parameter.Id.TYPE, "VOICE") && containsParameterType(o, Parameter.Id.TYPE, "HOME"))
            .map(Property::getValue).map(PhoneNr::of).findAny();
    }

    public Optional<PhoneNr> workPhone() {
        return card.getProperties(Property.Id.TEL).stream()
            .filter(o -> containsParameterType(o, Parameter.Id.TYPE, "VOICE") && containsParameterType(o, Parameter.Id.TYPE, "WORK"))
            .map(Property::getValue).map(PhoneNr::of).findAny();
    }

    public String title() {
        return card.getProperties(Property.Id.TITLE).stream().map(Property::getValue).findAny().orElse(null);
    }

    public String organization() {
        return card.getProperties(Property.Id.ORG).stream().map(Property::getValue).findAny().orElse(null);
    }

    public String note() {
        return card.getProperties(Property.Id.NOTE).stream().map(Property::getValue).findAny().orElse(null);
    }

    public LocalDate birthday() {
        return card.getProperties(Property.Id.BDAY).stream().map(Property::getValue).map(LocalDate::parse).findAny().orElse(null);
    }

    public LocalDate deathday() {
        return card.getProperties(Property.Id.DDAY).stream().map(Property::getValue).map(LocalDate::parse).findAny().orElse(null);
    }

    public GenderCode gender() {
        String gender = property(Property.Id.GENDER);
        return Strings.isNullOrBlank(gender) ? GenderCode.unspecified : switch (gender.charAt(0)) {
            case 'M' -> GenderCode.male;
            case 'F' -> GenderCode.female;
            case 'O' -> GenderCode.other;
            default -> GenderCode.unspecified;
        };
    }

    public Address homeAddress() {
        return address(Group.HOME);
    }

    public Optional<Photo> photo() {
        byte[] data = card.getProperties(Property.Id.PHOTO).stream().map(Property::getValue).map(o -> Base64.getDecoder().decode(o)).findAny().orElse(null);
        return (data != null) ? Optional.of(Photo.of(data)) : Optional.empty();
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

    private boolean containsParameterType(@NotNull Property property, @NotNull Parameter.Id id, @NotNull String value) {
        return property.getParameters(id).stream().anyMatch(o -> value.equals(o.getValue()));
    }

    private String property(@NotNull Property.Id id) {
        var property = card.getProperty(id);
        return (property != null) ? property.getValue() : null;
    }

    private String property(Group group, Property.Id id) {
        return card.getProperties(id).stream().filter(o -> o.getGroup().equals(group)).findAny().map(Property::getValue).orElse(null);
    }
}
