/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.erp.crm;

import net.tangly.commons.models.Address;
import net.tangly.commons.models.EmailAddress;
import net.tangly.commons.models.HasTags;
import net.tangly.commons.models.PhoneNr;
import net.tangly.commons.models.Tag;

import java.net.URI;
import java.net.URL;
import java.util.Optional;

public interface CrmEntity extends HasTags {
    default void setTag(String kind, String value) {
        if (value != null) {
            replace(Tag.of(CrmTags.CRM, kind, value));
        } else {
            findBy(CrmTags.CRM, kind).ifPresent(this::remove);
        }
    }

    default Optional<PhoneNr> findPhoneNr(String kind) {
        return findBy(CrmTags.getPhoneTag(kind)).map(o -> PhoneNr.of(o.stringValue()));
    }

    default void setPhoneNr(String kind, String phoneNr) {
        if (phoneNr != null) {
            replace(Tag.of(CrmTags.getPhoneTag(kind), PhoneNr.of(phoneNr)));
        } else {
            removeTagNamed(CrmTags.getPhoneTag(kind));
        }
    }

    default Optional<String> findEmail(String kind) {
        return findBy(CrmTags.getEmailTag(kind)).map(Tag::stringValue);
    }

    default void setEmail(String kind, String email) {
        if (email != null) {
            replace(Tag.of(CrmTags.getEmailTag(kind), EmailAddress.of(email)));
        } else {
            removeTagNamed(CrmTags.getEmailTag(kind));
        }
    }

    default Optional<Address> findAddress(String kind) {
        return findBy(CrmTags.getAddressTag(kind)).map(o -> (Address) o.value());
    }

    default void setAddress(String kind, Address address) {
        if (address != null) {
            replace(Tag.of(CrmTags.getEmailTag(kind), address));
        } else {
            removeTagNamed(CrmTags.getEmailTag(kind));
        }
    }

    default void setIm(String kind, String reference) {
        if (reference != null) {
            replace(Tag.of(CrmTags.getImTag(kind), reference));
        } else {
            removeTagNamed(CrmTags.getImTag(kind));
        }
    }

    default Optional<URL> findSite(String kind) {
        return findBy(CrmTags.getSiteTag(kind)).map(o -> (URL) o.value());
    }

    default void setSite(String kind, URI site) {
        if (site != null) {
            replace(Tag.of(CrmTags.getSiteTag(kind), site));
        } else {
            removeTagNamed(CrmTags.getSiteTag(kind));
        }
    }
}
