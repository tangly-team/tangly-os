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

package net.tangly.crm.ports;

import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.crm.CrmTags;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CrmTest {
    @Test
    void testCrmTags() {
        TagTypeRegistry registry = new TagTypeRegistry();
        CrmTags.registerTags(registry);
        assertThat(registry.namespaces().size()).isEqualTo(2);
        assertThat(registry.namespaces()).contains(CrmTags.CRM, CrmTags.GEO);
        assertThat(registry.tagNamesForNamespace(CrmTags.GEO)).contains(CrmTags.LATITUDE, CrmTags.LONGITUDE, CrmTags.ALTITUDE, CrmTags.PLUSCODE);
        assertThat(registry.tagNamesForNamespace(CrmTags.CRM).isEmpty()).isFalse();
    }
}
