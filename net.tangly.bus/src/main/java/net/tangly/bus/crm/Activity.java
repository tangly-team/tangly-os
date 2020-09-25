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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import net.tangly.bus.core.EntityImp;
import net.tangly.bus.core.Strings;
import org.jetbrains.annotations.NotNull;

/**
 * An activity is an action between a seller and a potential sellee. Activities are performed in the context of interaction.
 */
public class Activity extends EntityImp {
    private ActivityCode code;
    private int durationInMinutes;
    private String details;

    public static List<String> emailIds(String details) {
        return Strings.isNullOrBlank(details) ? null : Arrays.asList(details.split(" "));
    }

    public ActivityCode code() {
        return code;
    }

    public void code(@NotNull ActivityCode code) {
        this.code = code;
    }

    public int durationInMinutes() {
        return durationInMinutes;
    }

    public void durationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
    }

    public String details() {
        return details;
    }

    public void details(String details) {
        this.details = details;
    }

    public boolean isValid() {
        return Objects.nonNull(code) && (durationInMinutes > 0);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Activity[oid=%s, id=%s, name=%s, fromDate=%s, toDate=%s, text=%s, code=%s, durationInMinutes=%s]", oid(), id(), name(),
                fromDate(), toDate(), text(), code(), durationInMinutes());
    }
}
