/*
 * Copyright 2006-2020 Marcel Baumann
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

package net.tangly.bus.products;

import net.tangly.bus.core.HasId;
import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An duration defines work performed on a project. It is possible to specify an additional contract to identify the financial
 * link to the duration. An duration is always performed by one employee. Administrative data specify the duration, the date and
 * an optional description. Efforts can be used to generate work ports such as monthly ports and used to calculate the spend
 * time on a contract or a project.
 */
public class Effort implements HasId {
    private static final long serialVersionUID = 1L;
    private long oid;

    private BigDecimal duration;

    private LocalDate startedOn;

    private String text;

    private Employee employee;

    private Project project;

    private Contract contract;

    @Override
    public long oid() {
        return oid;
    }

    public void oid(long oid) {
        this.oid = oid;
    }

    public BigDecimal duration() {
        return duration;
    }

    public void duration(BigDecimal effort) {
        this.duration = effort;
    }

    public LocalDate startedOn() {
        return startedOn;
    }

    public void startedOn(LocalDate worked) {
        this.startedOn = worked;
    }

    public String text() {
        return text;
    }

    public void text(String text) {
        this.text = text;
    }

    public Employee employee() {
        return employee;
    }

    public void employee(Employee employee) {
        this.employee = employee;
    }

    public Project project() {
        return project;
    }

    public void project(Project project) {
        this.project = project;
    }

    public Contract contract() {
        return contract;
    }

    public void contract(Contract contract) {
        this.contract = contract;
    }
}
