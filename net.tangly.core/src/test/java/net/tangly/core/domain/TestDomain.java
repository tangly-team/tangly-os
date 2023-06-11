/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestDomain {
    static class TestRealm implements Realm {
        @Override
        public void close() throws Exception {
        }
    }

    static class TestHandler implements Handler<TestRealm> {
        private final TestRealm realm;

        TestHandler(TestRealm realm) {
            this.realm = realm;
        }

        @Override
        public void importEntities() {

        }

        @Override
        public void exportEntities() {

        }

        @Override
        public TestRealm realm() {
            return null;
        }
    }

    static class TestBoundedDomain extends BoundedDomain<TestRealm, Void, TestHandler, Void> {
        TestBoundedDomain(TestRealm realm, TestHandler handler) {
            super("TestBoundedDomain", realm, null, handler, null, null);
        }
    }

    @Test
    void createDomain() {
        var realm = new TestRealm();
        var handler = new TestHandler(realm);
        var domain = new TestBoundedDomain(realm, handler);

        assertThat(domain.realm()).isEqualTo(realm);
        assertThat(domain.handler()).isEqualTo(handler);
    }
}
