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

package net.tangly.bus.products;

import java.time.LocalDate;
import java.time.LocalDateTime;

import net.tangly.bus.core.HasOid;

/**
 * An duration defines work performed on a project. It is possible to specify an additional contract to identify the financial
 * link to the duration. An duration is always performed by one employee. Administrative data specify the duration, the date and
 * an optional description. Efforts can be used to generate work ports such as monthly ports and used to calculate the spend
 * time on a contract or a project.
 */
public class Effort implements HasOid {
    private long oid;
    private String text;
    private LocalDate date;
    private int durationInMinutes;
    private Assignment assignment;
    private String contractId;

    @Override
    public long oid() {
        return 0;
    }

    public String text() {
        return text;
    }

    public void text(String text) {
        this.text = text;
    }

    public int duration() {
        return durationInMinutes;
    }

    public void duration(int effort) {
        this.durationInMinutes = effort;
    }

    public LocalDate date() {
        return date;
    }

    public void date(LocalDate date) {
        this.date = date;
    }

    public Assignment assignment() {
        return assignment;
    }

    public void assignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public String contractId() {
        return contractId;
    }

    public void contract(String contractId) {
        this.contractId = contractId;
    }
}
