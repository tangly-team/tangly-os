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

import java.util.Objects;
import java.util.Optional;

/**
 * A natural entity is a person. A natural entity has an identity defined as the legal number of a person (e.g., the social security number, a name defined as the last name and the
 * first name separated by a comma, a life duration and a text describing him or her.
 * <p>A potential approach for a natural person identification is ISO 24366.</p>
 * <p>The attributes of a natural entity shall specify private or home capabilities. Work related information shall be stored in an employee entity to acknowledge the situation
 * when a person has multiple employment relations. </p>
 * <p><em>A natural entity has an internal object identifier because universal identifier for natural persons are currently not available in our world.
 * Switzerland allows since 2023 to use the Swiss security social number. This identifier is not compatible with the EU standards.
 * Europe has a person identifier. Sadly both identifiers are not easily accessible
 * .</em></p>
 */
public class NaturalEntity extends EntityExtendedImp implements EntityExtended, CrmEntity {
    private String firstname;
    private GenderCode gender;
    private Photo photo;

    public NaturalEntity(long oid) {
        super(oid);
    }

    public String firstname() {
        return firstname;
    }

    public void firstname(String firstname) {
        this.firstname = firstname;
    }

    public String lastname() {
        return name();
    }

    public void lastname(String lastname) {
        name(lastname);
    }

    public String fullname() {
        return name() + ((firstname() != null) ? STR.", \{firstname()}" : "");
    }

    public GenderCode gender() {
        return gender;
    }

    public void gender(GenderCode gender) {
        this.gender = gender;
    }

    public Photo photo() {
        return photo;
    }

    public void photo(Photo photo) {
        this.photo = photo;
    }

    public boolean hasPhoto() {
        return photo != null;
    }

    public String socialNr() {
        return id();
    }

    public void socialNr(String socialNr) {
        id(socialNr);
    }

    public Optional<PhoneNr> phoneHome() {
        return findBy(CrmTags.CRM_PHONE_HOME).map(Tag::value).map(PhoneNr::of);
    }

    public Optional<PhoneNr> phoneMobile() {
        return findBy(CrmTags.CRM_PHONE_MOBILE).map(Tag::value).map(PhoneNr::of);
    }

    public Optional<EmailAddress> privateEmail() {
        return findBy(CrmTags.CRM_EMAIL_HOME).map(Tag::value).map(EmailAddress::of);
    }

    @Override
    public Optional<Address> address() {
        return findBy(CrmTags.CRM_ADDRESS_HOME).map(Tag::value).map(Address::of);
    }

    @Override
    public boolean validate() {
        return !Strings.isNullOrBlank(name()) && Objects.nonNull(gender()) && Objects.nonNull(range());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof NaturalEntity o) && super.equals(o) && Objects.equals(socialNr(), o.socialNr()) && Objects.equals(firstname(), o.firstname()) &&
            Objects.equals(name(), o.name()) && Objects.equals(gender(), o.gender());
    }

    @Override
    public String toString() {
        return """
            NaturalEntity[oid=%s, fromDate=%s, toDate=%s, text=%s, socialNr=%s, firstname=%s, name=%s, gender=%s, tags=%s, comments=%s]
            """.formatted(oid(), from(), to(), text(), socialNr(), firstname(), name(), gender(), tags(), comments());
    }
}
