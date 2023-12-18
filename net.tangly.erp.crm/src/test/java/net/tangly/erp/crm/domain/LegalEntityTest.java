/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.crm.domain;

import net.tangly.core.Address;
import net.tangly.core.Entity;
import net.tangly.core.HasLocation;
import net.tangly.core.Tag;
import net.tangly.core.crm.CrmTags;
import net.tangly.core.crm.LegalEntity;
import net.tangly.core.crm.VcardType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LegalEntityTest {
    @Test
    void testPosition() {
        final var pluscode = "8FVC5FM5+Q6G";
        final var longitude = 47.18459018924399;
        final var latitude = 8.458151140334703;
        final var address = Address.builder().country("CH").locality("Zug").postcode("6300").street("Bahnhofstrasse 1").build();
        var entity = new LegalEntity(Entity.UNDEFINED_OID);
        entity.address(VcardType.work, address);
        entity.add(Tag.of(CrmTags.GEO_PLUSCODE, pluscode));
        entity.add(Tag.of(CrmTags.GEO_LONGITUDE, Double.toString(longitude)));
        entity.add(Tag.of(CrmTags.GEO_LATITUDE, Double.toString(latitude)));
        assertThat(entity.plusCode()).hasValue(HasLocation.PlusCode.of(pluscode));
        assertThat(entity.position()).hasValue(HasLocation.GeoPosition.of(longitude, latitude));
        assertThat(entity.address()).hasValue(address);
    }
}
