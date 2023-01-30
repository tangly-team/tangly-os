/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core.crm;


import net.tangly.core.*;

import java.util.Objects;
import java.util.Optional;

/**
 * A natural entity is a person. A natural entity has an identity defined as the legal number of a person (e.g. the social security number, a name defined as
 * the last name and the first name separated by a comma, a life duration and a text describing it.
 * <p>A potential approach for a natural person identification is ISO 24366.</p>
 */
public class NaturalEntity extends NamedEntityImp implements CrmEntity {
    private String socialNr;
    private String firstname;
    private String lastname;
    private GenderCode gender;
    private Photo photo;

    public NaturalEntity() {
    }

    public String firstname() {
        return firstname;
    }

    public void firstname(String firstname) {
        this.firstname = firstname;
    }

    public String lastname() {
        return lastname;
    }

    public void lastname(String lastname) {
        this.lastname = lastname;
    }

    public String name() {
        return lastname() + ((firstname() != null) ? ", " + firstname() : "");
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
        return socialNr;
    }

    public void socialNr(String socialNr) {
        this.socialNr = socialNr;
    }

    public Optional<PhoneNr> phoneHome() {
        return findBy(CrmTags.CRM_PHONE_HOME).map(Tag::value).map(PhoneNr::of);
    }

    public Optional<PhoneNr> phoneMobile() {
        return findBy(CrmTags.CRM_PHONE_MOBILE).map(Tag::value).map(PhoneNr::of);
    }

    @Override
    public Optional<Address> address() {
        return findBy(CrmTags.CRM_ADDRESS_HOME).map(Tag::value).map(Address::of);
    }

    @Override
    public boolean check() {
        return !Strings.isNullOrBlank(lastname()) && (gender() != null);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof NaturalEntity o) && super.equals(o) && Objects.equals(socialNr(), o.socialNr()) && Objects.equals(firstname(), o.firstname()) &&
            Objects.equals(lastname(), o.lastname()) && Objects.equals(gender(), o.gender());
    }

    @Override
    public String toString() {
        return """
            NaturalEntity[oid=%s, fromDate=%s, toDate=%s, text=%s, socialNr=%s, firstname=%s, lastname=%s, gender=%s, tags=%s, comments=%s]
            """.formatted(oid(), from(), to(), text(), socialNr(), firstname(), lastname(), gender(), tags(), comments());
    }
}
