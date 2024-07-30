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

import net.tangly.core.codes.Code;

/**
 * Defines the phases of an opportunity between the seller and sellee organizations. An opportunity is first a prospect, can evolve to lead, customer, and
 * completed contract or instead can be lost.
 * <dl>
 *     <dt>prospect</dt><dd>a contact was established and activities are initiated.</dd>
 *     <dt>lead</dt><dd>activities are pursued to create a contract with a legal entity.</dd>
 *     <dt>ordered</dt><dd>contract was signed and services shall be delivered to fulfill the contract.</dd>
 *     <dt>completed</dt><dd>All contractual requirements are fulfilled and the artifacts were delivered to the sellee.</dd>
 *     <dt>lost</dt><dd>No contract could be signed with the interested party and activities to pursue this contract are stopped.</dd>
 * </dl>
 */
public enum OpportunityCode implements Code {
    prospect, lead, ordered, completed, lost;

    @Override
    public int id() {
        return this.ordinal();
    }

    @Override
    public String code() {
        return this.name();
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
