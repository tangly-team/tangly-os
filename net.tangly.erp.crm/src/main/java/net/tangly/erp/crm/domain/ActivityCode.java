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
 * Defines the kinds af activities between natural persons.
 * <dl>
 *     <dt>talk</dt><dd>A personal and informal interaction with customer representatives.</dd>
 *     <dt>meeting</dt><dd>A scheduled meeting with customer representatives. THe activity text can be used as meeting protocol.</dd>
 *     <dt>email</dt><dd>An electronic message sent to customer representatives.</dd>
 *     <dt>letter</dt><dd>A physical message sent to customer representatives. Digitalization should reduce letter to a minimum.</dd>
 *     <dt>audiocall</dt><dd>An audio call such as a phone call or a Slack call with customer representatives.</dd>
 *     <dt>videocall</dt><dd>An video call such as a team call or a Slack call with customer representatives.</dd>
 *     <dt>chat</dt><dd>A textual chat interaction through asynchronous communication channel</dd>
 *     <dt>campaign</dt><dd>A direct communication through a compaign. For example a flier, an event invitation or a whitepater is sent to a group of
 *     prospects.</dd>
 * </dl>
 */
public enum ActivityCode implements Code {
    talk, meeting, email, letter, audiocall, videocall, chat, campaign;

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
