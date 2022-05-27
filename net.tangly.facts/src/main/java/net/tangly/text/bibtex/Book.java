/*
 * Copyright 2022-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.text.bibtex;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

@Builder
public record Book(@NotNull String reference,
                   @NotNull String title,
                   String subtitle,
                   @NotNull String authors,
                   Integer year,
                   String version,
                   String isbn,
                   Publisher publisher,
                   String series,
                   Integer pagetotal,
                   String url,
                   Set<String> keywords,
                   String note) {
    public Book {
        assert !Objects.requireNonNull(reference).isBlank();
        assert !Objects.requireNonNull(title).isBlank();
    }
}
