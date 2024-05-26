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

package net.tangly.erp.products.domain;

import net.tangly.core.HasDate;
import net.tangly.core.HasText;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Objects;

/**
 * An effort defines work performed on a project. It is possible to specify an additional contract to identify the financial link to the duration. A duration is always performed by
 * one employee. Administrative data specify the duration, the date and an optional description. Efforts can be used to generate work ports such as monthly ports and used to
 * calculate the spent time on a contract or a project.
 */
public class Effort implements HasDate, HasText {
    private Assignment assignment;
    private String contractId;
    private LocalDate date;
    private int durationInMinutes;
    private String text;
    private String minutes;
    public Effort() {
    }

    public Effort(@NotNull Assignment assignment, @NotNull String contractId, LocalDate date, int durationInMinutes, String text) {
        this.assignment = assignment;
        this.contractId = contractId;
        this.date = date;
        this.durationInMinutes = durationInMinutes;
        this.text = text;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public void text(String text) {
        this.text = text;
    }

    public String minutes() {
        return minutes;
    }

    public void minutes(String minutes) {
        this.minutes = minutes;
    }

    public int duration() {
        return durationInMinutes;
    }

    public void duration(int effort) {
        this.durationInMinutes = effort;
    }

    @Override
    public LocalDate date() {
        return date;
    }

    @Override
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

    public void contractId(String contractId) {
        this.contractId = contractId;
    }

    public boolean check() {
        return (date() != null) && (duration() >= 0) && (contractId != null) && (assignment() != null);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Effort other) && (date().equals(other.date())) && (duration() == other.duration()) && (Objects.nonNull(assignment()) && assignment().equals(other.assignment()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(date(), duration(), assignment());
    }

    @Override
    public String toString() {
        return """
            Effort[date=%s, durationInMinutes=%s, contractId=%s, text=%s, assignment=%s]
            """.formatted(date(), duration(), contractId(), text(), assignment());
    }
}
