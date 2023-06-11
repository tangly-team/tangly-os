/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.domain;

import net.tangly.core.codes.Code;

/**
 * Defines the status of a lead.
 * <dl>
 * <dt>Open</dt><dd>Indicates that a record requires follow up. This would be the state that the rep first interacts with the lead.</dd>
 * <dt>Contacting</dt><dd>This status indicates that the sales responsible has looked at the lead and confirmed that the data is real and that he or
 * she has decided to work the record.</dd>
 * <dt>Qualified</dt><dd>Indicates the leas was converted and information was added to the company and person directories.</dd>
 * <dt>Disqualified</dt><dd>This status is used to specify the end of an interaction. In most cases, you will want to document the reason in some text for
 * the disqualification.</dd>
 * </dl>
 */
public enum LeadCode implements Code {
    open, contacting, qualified, disqualified;

    @Override
    public int id() {
        return this.ordinal();
    }

    @Override
    public String code() {
        return this.name();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
