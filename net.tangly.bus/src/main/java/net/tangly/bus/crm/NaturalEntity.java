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


import net.tangly.bus.core.EntityImp;

/**
 * A natural entity is a person. A natural entity has an identity defined as the legal number of a person (e.g. the social security number0, a name
 * defined as the last name and the first name separated by a comma, a life duration and a text describing it.
 */
public class NaturalEntity extends EntityImp implements CrmEntity {
    private static final long serialVersionUID = 1L;

    private String firstname;

    private String lastname;

    public NaturalEntity() {
        // default constructor
    }

    public static NaturalEntity of(long oid, String socialNumber, String lastname, String firstname) {
        var entity = new NaturalEntity();
        entity.oid(oid);
        entity.id(socialNumber);
        entity.lastname(lastname);
        entity.firstname(firstname);
        entity.socialNr(socialNumber);
        return entity;
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

    @Override
    public void name(String name) {
    }

    @Override
    public String name() {
        return lastname() + ", " + firstname();
    }

    public String socialNr() {
        return id();
    }

    public void socialNr(String socialNr) {
        id(socialNr);
    }
}
