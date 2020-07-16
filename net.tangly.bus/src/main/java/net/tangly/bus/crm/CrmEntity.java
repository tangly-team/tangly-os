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

import net.tangly.bus.core.Address;
import net.tangly.bus.core.HasTags;
import net.tangly.bus.core.PhoneNr;
import net.tangly.bus.core.Tag;

/**
 * A customer relation management entity defines a set of operations useful for all customers. All information are stored as tags for future extensions.
 */
public interface CrmEntity extends HasTags {
    default Optional<PhoneNr> phoneNr(String kind) {
        return findBy(CrmTags.phoneTag(kind)).map(o -> PhoneNr.of(o.value()));
    }

    default void phoneNr(String kind, String phoneNr) {
        tag(CrmTags.phoneTag(kind), phoneNr);
    }

    default Optional<String> email(String kind) {
        return findBy(CrmTags.emailTag(kind)).map(Tag::value);
    }

    default void email(String kind, String email) {
        tag(CrmTags.emailTag(kind), email);
    }

    default Optional<Address> address(String kind) {
        return findBy(CrmTags.addressTag(kind)).map(Tag::value).map(Address::of);
    }

    default void address(String kind, Address address) {
        tag(CrmTags.addressTag(kind), address.text());
    }

    default Optional<String> im(String kind) {
        return findBy(CrmTags.imTag(kind)).map(Tag::value);
    }

    default void im(String kind, String reference) {
        tag(CrmTags.imTag(kind), reference);
    }

    default Optional<String> site(String kind) {
        return findBy(CrmTags.siteTag(kind)).map(Tag::value);
    }

    default void site(String kind, String site) {
        tag(CrmTags.siteTag(kind), site);
    }
}
