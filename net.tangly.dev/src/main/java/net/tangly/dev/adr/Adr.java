/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.dev.adr;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * Defines an architectural design record used to codify software architecture decisions.
 */
public class Adr {
    public static final Set<String> STATUSES = Set.of("deprecated", "implemented", "proposed", "rejected", "superseded");
    private final String id;
    private String title;
    private LocalDate date;
    private String status;
    private String context;
    private String decision;
    private String consequences;

    private final List<Link> links;
    private String module;
    private Path adrPath;

    public Adr(@NotNull String id) {
        this.id = id;
        links = new ArrayList<>();
    }

    public static boolean isStatusValid(@NotNull String status) {
        return STATUSES.contains(status);
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public void title(String title) {
        this.title = title;
    }

    public LocalDate date() {
        return date;
    }

    public void date(LocalDate date) {
        this.date = date;
    }

    public String status() {
        return status;
    }

    public void status(String status) {
        this.status = status;
    }

    public String context() {
        return context;
    }

    public void context(String context) {
        this.context = context;
    }

    public String decision() {
        return decision;
    }

    public void decision(String decision) {
        this.decision = decision;
    }

    public String consequences() {
        return consequences;
    }

    public void consequences(String consequences) {
        this.consequences = consequences;
    }

    public List<Link> links() {
        return Collections.unmodifiableList(links);
    }

    public void addLink(Link link) {
        links.add(link);
    }

    public String module() {
        return module;
    }

    public void module(String module) {
        this.module = module;
    }

    public Path adrPath() {
        return adrPath;
    }

    public void adrPath(Path adrPath) {
        this.adrPath = adrPath;
    }
}
