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


import java.util.Optional;

import net.tangly.core.Address;
import net.tangly.core.QualifiedEntityImp;
import net.tangly.core.Strings;
import net.tangly.core.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * A legal entity is legally recognized organization able to underwrite contracts and hire employees. A legal entity has a name, an identity defined as the
 * legal number of an organization (e.g. zefix UID number in Switzerland, EUID in Europe), and a text describing it.
 */
public class LegalEntity extends QualifiedEntityImp implements CrmEntity {
    public LegalEntity() {
    }

    /**
     * Create a link to the Zefix page for the organization - makes only sense for Swiss companies -.
     *
     * @param entity organization which Zefix information should be displayed
     * @return link to Zefix information
     */
    public static String organizationZefixUrl(@NotNull LegalEntity entity) {
        return "https://www.zefix.ch/en/search/entity/list?name=" + entity.id() + "&searchType=exact";
    }

    @Override
    public Optional<Address> address() {
        return findBy(CrmTags.CRM_ADDRESS_WORK).map(Tag::value).map(Address::of);
    }

    /**
     * Returns the VAT identifying number of the legal entity.
     *
     * @return the VAT identifying number
     */
    public String vatNr() {
        return findBy(CrmTags.CRM_VAT_NUMBER).map(Tag::value).orElse(null);
    }

    /**
     * Sets the VAT identifying number of the legal entity.
     *
     * @param vatNr new VAT identifying number
     */
    public void vatNr(String vatNr) {
        update(CrmTags.CRM_VAT_NUMBER, vatNr);
    }

    @Override
    public boolean check() {
        return !Strings.isNullOrBlank(id()) && !Strings.isNullOrBlank(name());
    }

    @Override
    public String toString() {
        return """
            LegalEntity[oid=%s, id=%s, name=%s, fromDate=%s, toDate=%s, text=%s, vatNr=%s, tags=%s, comments=%s]
            """.formatted(oid(), id(), name(), fromDate(), toDate(), text(), vatNr(), tags(), comments());
    }
}
