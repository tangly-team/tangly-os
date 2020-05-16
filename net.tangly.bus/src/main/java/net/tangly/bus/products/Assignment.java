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
import net.tangly.bus.crm.Employee;

import java.time.LocalDate;

/**
 * An assignment defines connection of an employee to a project for a duration. The start date shall be equal or greater to the start date of project.
 * The end date is optional, if defined it shall smaller or equal to the end date of the project.
 */
public class Assignment implements HasId {
    private long oid;
    private Employee employee;
    private Project project;
    private LocalDate fromDate;
    private LocalDate toDate;

    @Override
    public long oid() {
        return oid;
    }

    public void oid(long oid) {
        this.oid = oid;
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

    public LocalDate fromDate() {
        return fromDate;
    }

    public void fromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate toDate() {
        return toDate;
    }

    public void toDate(LocalDate toDate) {
        this.toDate = toDate;
    }
}


