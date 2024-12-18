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

import net.fortuna.ical4j.model.Content;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.vcard.*;
import net.tangly.commons.lang.Strings;
import net.tangly.core.Address;
import net.tangly.core.EmailAddress;
import net.tangly.core.GenderCode;
import net.tangly.core.PhoneNr;
import net.tangly.erp.crm.domain.Photo;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VCard2 {
    private final VCard card;

    public VCard2(@NotNull VCard card) {
        this.card = card;
    }

    public String firstname() {
        return properties(PropertyName.NAME.FN).stream().map(Property::getValue).map(o -> o.split(";")[1]).findAny().orElse(null);
    }

    public String lastname() {
        return properties(PropertyName.N).stream().map(Property::getValue).map(o -> o.split(";")[0]).findAny().orElse(null);
    }

    public String formattedName() {
        return properties(PropertyName.N).stream().map(Property::getValue).findAny().orElse(null);
    }

    public Optional<EmailAddress> homeEmail() {
        return properties(PropertyName.EMAIL).stream()
            .filter(o -> containsParameterType(o, ParameterName.TYPE, "INTERNET") && containsParameterType(o, ParameterName.TYPE, "HOME"))
            .map(Property::getValue).map(EmailAddress::of).findAny();
    }

    public Optional<EmailAddress> workEmail() {
        return properties(PropertyName.EMAIL).stream()
            .filter(o -> containsParameterType(o, ParameterName.TYPE, "INTERNET") && containsParameterType(o, ParameterName.TYPE, "WORK"))
            .map(Property::getValue).map(EmailAddress::of).findAny();
    }

    public Optional<PhoneNr> homePhone() {
        return properties(PropertyName.TEL).stream()
            .filter(o -> containsParameterType(o, ParameterName.TYPE, "VOICE") && containsParameterType(o, ParameterName.TYPE, "HOME")).map(Property::getValue)
            .map(PhoneNr::of).findAny();
    }

    public Optional<PhoneNr> workPhone() {
        return properties(PropertyName.TEL).stream()
            .filter(o -> containsParameterType(o, ParameterName.TYPE, "VOICE") && containsParameterType(o, ParameterName.TYPE, "WORK")).map(Property::getValue)
            .map(PhoneNr::of).findAny();
    }

    public String title() {
        return properties(PropertyName.TITLE).stream().map(Property::getValue).findAny().orElse(null);
    }

    public String organization() {
        return properties(PropertyName.ORG).stream().map(Property::getValue).findAny().orElse(null);
    }

    public String note() {
        return properties(PropertyName.NOTE).stream().map(Property::getValue).findAny().orElse(null);
    }

    public LocalDate birthday() {
        return properties(PropertyName.BDAY).stream().map(Property::getValue).map(LocalDate::parse).findAny().orElse(null);
    }

    public LocalDate deathday() {
        return properties(PropertyName.DDAY).stream().map(Property::getValue).map(LocalDate::parse).findAny().orElse(null);
    }

    public GenderCode gender() {
        String gender = property(PropertyName.GENDER);
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
        byte[] data = properties(PropertyName.PHOTO).stream().map(Property::getValue).map(o -> Base64.getDecoder().decode(o)).findAny().orElse(null);
        return (data != null) ? Optional.of(Photo.of(data)) : Optional.empty();
    }

    private LocalDate date(PropertyName name) {
        String date = property(name);
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
        String text = property(group, PropertyName.ADR);
        if (text != null) {
            var parts = Objects.requireNonNull(text).split(";", -1);
            Objects.checkFromIndexSize(0, parts.length, 7);
            return Address.builder().poBox(parts[0]).extended(parts[1]).street(parts[2]).locality(parts[3]).region(parts[4]).postcode(parts[5])
                .country(parts[6]).build();
        } else {
            return null;
        }
    }

    private boolean containsParameterType(@NotNull Property property, @NotNull ParameterName name, @NotNull String value) {
        return property.getParameters(name.name()).stream().anyMatch(o -> value.equals(o.getValue()));
    }

    private <T extends Property> List<T> properties(@NotNull PropertyName name) {
        return card.getEntities().get(0).getProperties(name.name());
    }

    private String property(@NotNull PropertyName name) {
        return card.getEntities().get(0).getProperty(name).map(Content::getValue).orElse(null);
    }

    private String property(Group group, PropertyName name) {
        return card.getEntities().get(0).getProperties(name.name()).stream().filter(o -> (o instanceof GroupProperty gp) && gp.getGroup().equals(group))
            .findAny().map(Property::getValue).orElse(null);
    }
}
