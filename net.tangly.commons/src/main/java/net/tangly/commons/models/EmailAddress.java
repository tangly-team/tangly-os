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

package net.tangly.commons.models;

import java.io.Serializable;
import java.util.Objects;

public class EmailAddress implements Serializable {
    private final String recipient;
    private final String domain;

    public EmailAddress(String recipient, String domain) {
        this.recipient = Objects.requireNonNull(recipient);
        this.domain = Objects.requireNonNull(domain);
    }

    @Override
    public String toString() {
        return recipient + "@" + domain;
    }
}