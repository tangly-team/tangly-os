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

import net.tangly.commons.models.Entity;
import net.tangly.commons.models.EntityImp;
import net.tangly.commons.models.Tag;

public class LegalEntity extends EntityImp implements Entity, CrmEntity {

    public static LegalEntity of(long oid, String id, String name) {
        return new LegalEntity(oid, id, name);
    }

    public LegalEntity(long oid, String id, String name) {
        super(oid, id, name);
    }

    public String vatNr() {
        var value = findBy(CrmTags.CRM_VAT_NUMBER);
        return value.map(Tag::stringValue).orElse(null);
    }

    public void vatNr(String vatNr) {
        replace(Tag.of(CrmTags.CRM_VAT_NUMBER, vatNr));
    }

    public String companyNr() {
        return id();
    }

    public void companyNr(String companyNr) {
        setId(companyNr);
    }
}
