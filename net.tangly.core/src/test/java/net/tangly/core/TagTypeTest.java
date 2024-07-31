/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class TagTypeTest {
    private static final String NAMESPACE = "Test-Namespace";
    private static final String TAG_STRING_NONE = "Tag-Test-None";
    private static final String TAG_STRING_OPTIONAL = "Tag-Test-Optional";
    private static final String TAG_STRING_MANDATORY = "Tag-Test-Mandatory";

    @Test
    void testTagTypeJson() throws IOException {
        TypeRegistry registry = new TypeRegistry();
        TagHelper.build(Paths.get(getClass().getClassLoader().getResource("json/TestTagTypes.json").getPath())).forEach(registry::register);
        assertThat(registry.tagTypes().size()).isEqualTo(3);
        assertThat(registry.tagNamesForNamespace(NAMESPACE)).contains(TAG_STRING_NONE, TAG_STRING_OPTIONAL, TAG_STRING_MANDATORY);
        assertThat(registry.find(NAMESPACE, TAG_STRING_NONE)).map(TagType::canHaveValue).hasValue(false);
        assertThat(registry.find(NAMESPACE, TAG_STRING_OPTIONAL)).map(TagType::canHaveValue).hasValue(true);
        assertThat(registry.find(NAMESPACE, TAG_STRING_MANDATORY)).map(TagType::mustHaveValue).hasValue(true);
    }
}
