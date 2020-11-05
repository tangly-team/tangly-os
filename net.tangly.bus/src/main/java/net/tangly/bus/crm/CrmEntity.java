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

package net.tangly.bus.crm;

import java.util.Optional;

import net.tangly.core.Address;
import net.tangly.core.EmailAddress;
import net.tangly.core.HasTags;
import net.tangly.core.PhoneNr;
import net.tangly.core.Tag;

/**
 * A customer relation management mixin defines a set of operations useful for all customers. All information are stored as tags for future extensions.
 */
public interface CrmEntity extends HasTags {
    default Optional<PhoneNr> phoneNr(CrmTags.Type type) {
        return findBy(CrmTags.phoneTag(type.name())).map(o -> PhoneNr.of(o.value()));
    }

    default void phoneNr(CrmTags.Type type, String phoneNr) {
        tag(CrmTags.phoneTag(type.name()), phoneNr);
    }

    default Optional<EmailAddress> email(CrmTags.Type type) {
        return findBy(CrmTags.emailTag(type.name())).map(Tag::value).map(EmailAddress::of);
    }

    default void email(CrmTags.Type type, EmailAddress email) {
        email(type, email.text());
    }

    default void email(CrmTags.Type type, String email) {
        tag(CrmTags.emailTag(type.name()), email);
    }

    default Optional<Address> address(CrmTags.Type type) {
        return findBy(CrmTags.addressTag(type.name())).map(Tag::value).map(Address::of);
    }

    default void address(CrmTags.Type type, Address address) {
        tag(CrmTags.addressTag(type.name()), address.text());
    }

    default Optional<String> im(CrmTags.Type type) {
        return findBy(CrmTags.imTag(type.name())).map(Tag::value);
    }

    default void im(CrmTags.Type type, String reference) {
        tag(CrmTags.imTag(type.name()), reference);
    }

    default Optional<String> site(CrmTags.Type type) {
        return findBy(CrmTags.siteTag(type.name())).map(Tag::value);
    }

    default void site(CrmTags.Type type, String site) {
        tag(CrmTags.siteTag(type.name()), site);
    }
}
