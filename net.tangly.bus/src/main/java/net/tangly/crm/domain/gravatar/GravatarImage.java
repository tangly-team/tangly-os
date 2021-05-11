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

package net.tangly.crm.domain.gravatar;

/**
 * Defines the official image kinds of the gravatar site.
 */

public enum GravatarImage {
    GRAVATAR_ICON(""), IDENTICON("identicon"), MONSTERID("monsterid"), WAVATAR("wavatar"), HTTP_404("404");

    private final String code;

    GravatarImage(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}