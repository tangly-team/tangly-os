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

import net.tangly.core.HasMutableDate;
import net.tangly.core.HasMutableText;
import net.tangly.core.Strings;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An activity is an action between a seller and a potential sellee. Activities are performed in the context of an interaction. Activities are only available
 * through their context, meaning no access to all activities exists.
 * <p>The author of the activity identifies the collaborator interacting with the potential customer. Consider using documentation templates in the textual
 * description of the activity to allow structured search over activities. For example you could write @{contact name} in your text to later search all
 * activities where a specific person was participating.</p>
 */
public class Activity implements HasMutableDate, HasMutableText {
    private LocalDate date;
    private ActivityCode code;
    private int durationInMinutes;
    private String author;
    private String text;

    public Activity() {
    }

    public static List<String> emailIds(String details) {
        return Strings.isNullOrBlank(details) ? null : Arrays.asList(details.split(" "));
    }

    @Override
    public LocalDate date() {
        return date;
    }

    @Override
    public void date(LocalDate date) {
        this.date = date;
    }

    public ActivityCode code() {
        return code;
    }

    public void code(@NotNull ActivityCode code) {
        this.code = code;
    }

    public int duration() {
        return durationInMinutes;
    }

    public void duration(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public String author() {
        return author;
    }

    public void author(String author) {
        this.author = author;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public void text(String text) {
        this.text = text;
    }

    public boolean check() {
        return Objects.nonNull(code) && (durationInMinutes > 0);
    }

    @Override
    public String toString() {
        return """
            Activity[date=%s, code=%s, durationInMinutes=%s, author=%s, text=%s]
            """.formatted(date(), code(), duration(), author(), text());
    }
}
