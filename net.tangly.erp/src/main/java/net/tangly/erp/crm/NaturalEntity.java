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


import net.tangly.commons.models.EntityImp;

public class NaturalEntity extends EntityImp implements CrmEntity {
    private static final long serialVersionUID = 1L;

    private String firstname;

    private String lastname;

    public static NaturalEntity of(long oid, String socialNumber, String lastname, String firstname) {
        var entity = new NaturalEntity(oid, socialNumber);
        entity.lastname(lastname);
        entity.firstname(firstname);
        entity.socialNr(socialNumber);
        return entity;
    }

    public NaturalEntity(long oid, String id) {
        super(oid, id);
    }

    public String firstname() {
        return firstname;
    }

    public void firstname(String firstname) {
        this.firstname = firstname;
        updateName();
    }

    public String lastname() {
        return lastname;
    }

    public void lastname(String lastname) {
        this.lastname = lastname;
        updateName();
    }

    public String socialNr() {
        return id();
    }

    public void socialNr(String socialNr) {
        setId(socialNr);
    }

    private void updateName() {
        name(lastname() + ", " + firstname());
    }
}
