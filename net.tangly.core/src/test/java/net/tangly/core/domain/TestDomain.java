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

package net.tangly.core.domain;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestDomain {
    static class TestRealm implements Realm {
    }

    record TestPort(TestRealm realm) implements Port<TestRealm> {

        @Override
        public void importEntities(@NotNull DomainAudit audit) {
        }

        @Override
        public void exportEntities(@NotNull DomainAudit audit) {
        }

        @Override
        public void clearEntities(@NotNull DomainAudit audit) {
        }

        @Override
        public TestRealm realm() {
            return null;
        }
    }

    static class TestBoundedDomain extends BoundedDomain<TestRealm, Void, TestPort> {
        TestBoundedDomain(TestRealm realm, TestPort port) {
            super("TestBoundedDomain", realm, null, port, null, null);
        }
    }

    @Test
    void createDomain() {
        var realm = new TestRealm();
        var handler = new TestPort(realm);
        var domain = new TestBoundedDomain(realm, handler);

        assertThat(domain.realm()).isEqualTo(realm);
        assertThat(domain.port()).isEqualTo(handler);
    }
}
