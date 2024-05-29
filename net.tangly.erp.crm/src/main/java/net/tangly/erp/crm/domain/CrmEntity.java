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

package net.tangly.erp.crm.domain;

import net.tangly.core.*;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A customer relation management mixin defines a set of operations useful for all CRM abstractions. All information is stored as tags for future extensions.
 * Key attributes are address or locations, phone numbers, social channels and internet presence.
 * <p>The tag approach provides flexibility required by the evolution of values to represent similar information. For example an address, a geographical
 * location or an universal Plus code model similar information. Examples of CRM entities are natural and legal entities.</p>
 */
public interface CrmEntity extends HasMutableTags, HasLocation {
    @Override
    default Optional<PlusCode> plusCode() {
        return findBy(CrmTags.GEO_PLUSCODE).map(Tag::value).map(PlusCode::of);
    }

    @Override
    default Optional<GeoPosition> position() {
        var longitude = findBy(CrmTags.GEO_LONGITUDE);
        var latitude = findBy(CrmTags.GEO_LATITUDE);
        return (longitude.isPresent() && latitude.isPresent()) ?
            Optional.of(GeoPosition.of(Double.parseDouble(longitude.get().value()), Double.parseDouble(latitude.get().value()))) : Optional.empty();
    }

    default Optional<PhoneNr> phoneNr(VcardType type) {
        return findBy(CrmTags.phoneTag(type.name())).map(Tag::value).map(PhoneNr::of);
    }

    default void phoneNr(@NotNull VcardType type, String phoneNr) {
        var contact = PhoneNr.of(phoneNr);
        if (contact == null) {
            removeTagNamed(CrmTags.phoneTag(type.name()));
        } else {
            update(CrmTags.phoneTag(type.name()), contact.number());
        }
    }

    default Optional<EmailAddress> email(@NotNull VcardType type) {
        return findBy(CrmTags.emailTag(type.name())).map(Tag::value).map(EmailAddress::of);
    }

    default void email(VcardType type, String email) {
        var contact = EmailAddress.of(email);
        if (contact == null) {
            removeTagNamed(CrmTags.emailTag(type.name()));
        } else {
            update(CrmTags.emailTag(type.name()), contact.text());
        }
    }

    default Optional<Address> address(VcardType type) {
        return findBy(CrmTags.addressTag(type.name())).map(Tag::value).map(Address::of);
    }

    default void address(VcardType type, Address address) {
        if (address != null) {
            update(CrmTags.addressTag(type.name()), address.text());
        } else {
            removeTagNamed(CrmTags.addressTag(type.name()));
        }
    }

    default Optional<String> im(VcardType type) {
        return findBy(CrmTags.imTag(type.name())).map(Tag::value);
    }

    default void im(VcardType type, String reference) {
        update(CrmTags.imTag(type.name()), reference);
    }

    default Optional<String> site(VcardType type) {
        return findBy(CrmTags.siteTag(type.name())).map(Tag::value);
    }

    default void site(VcardType type, String site) {
        update(CrmTags.siteTag(type.name()), site);
    }
}
