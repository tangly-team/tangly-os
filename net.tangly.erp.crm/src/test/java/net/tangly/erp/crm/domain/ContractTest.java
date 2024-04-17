/*
 * Copyright 2023-2024 Marcel Baumann
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

import net.tangly.core.HasOid;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class ContractTest {
    @Test
    void testEntity() {
        Contract entity = new Contract(HasOid.UNDEFINED_OID);
        entity.name("Contract");
        entity.id("C-001");
        entity.from(LocalDate.of(2000, Month.JANUARY, 1));
        entity.to(LocalDate.of(2000, Month.DECEMBER, 31));
        assertThat(entity.toString().length()).isNotZero();
    }
}
