/*
 * Copyright 2023 Marcel Baumann
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

package net.tangly.app.domain.model;

import net.tangly.core.HasDate;
import net.tangly.core.HasId;
import net.tangly.core.HasName;
import net.tangly.core.HasText;
import net.tangly.core.TypeRegistry;
import net.tangly.core.domain.BoundedDomain;
import net.tangly.core.domain.DomainEntity;
import net.tangly.core.domain.Handler;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.stream.LongStream;

public class BoundedDomainItems extends BoundedDomain<BoundedDomainItems.AppRealm, BoundedDomainItems.AppBusinessLogic, BoundedDomainItems.AppHandler, BoundedDomainItems.AppPort> {
    public static final String DOMAIN = "Items";

    private BoundedDomainItems(BoundedDomainItems.AppRealm realm, BoundedDomainItems.AppBusinessLogic logic, BoundedDomainItems.AppHandler handler,
                               BoundedDomainItems.AppPort port) {
        super(DOMAIN, realm, logic, handler, port, new TypeRegistry());
    }

    public static BoundedDomainItems create() {
        BoundedDomainItems.AppRealm realm = new BoundedDomainItems.AppRealm();
        return new BoundedDomainItems(realm, new BoundedDomainItems.AppBusinessLogic(), new BoundedDomainItems.AppHandler(realm), new BoundedDomainItems.AppPort());
    }

    @Override
    public List<DomainEntity<?>> entities() {
        return List.of(new DomainEntity<>(DOMAIN, BoundedDomainItems.Invoice.class, realm().invoices()));
    }

    record Invoice(String id, String name, String text, BigDecimal amount) implements HasId, HasName, HasText {
    }

    record Contract(String id, String name, String text) implements HasId, HasName, HasText {
    }

    record Assignment(LocalDate date, long durationInMinutes, String text) implements HasDate, HasText {
    }

    public static class AppRealm implements Realm {
        private final ProviderInMemory<BoundedDomainItems.Invoice> invoices;
        private final ProviderInMemory<BoundedDomainItems.Contract> contracts;
        private final ProviderInMemory<BoundedDomainItems.Assignment> assignments;

        public AppRealm() {
            invoices = new ProviderInMemory<>();
            contracts = new ProviderInMemory<>();
            assignments = new ProviderInMemory<>();
            load();
        }

        public Provider<BoundedDomainItems.Invoice> invoices() {
            return invoices;
        }

        public Provider<Contract> contracts() {
            return contracts;
        }

        public Provider<Assignment> assignments() {
            return assignments;
        }

        private void load() {
            LocalDate today = LocalDate.of(2000, Month.JANUARY, 1);
            LongStream.rangeClosed(1, 100)
                .forEach(o -> invoices().update(new BoundedDomainItems.Invoice(Long.toString(o), "invoice-" + o, "invoice text-" + o, BigDecimal.valueOf(o))));
            LongStream.rangeClosed(1, 100).forEach((o -> contracts().update(new Contract(Long.toString(o), "contract-" + o, "_contract_ text-" + o))));
            LongStream.rangeClosed(1, 100).forEach((o -> assignments().update(new Assignment(today.plusDays(o), o, "_duration_ text-" + o))));
        }
    }

    public static class AppBusinessLogic {
    }

    public static class AppHandler implements Handler<BoundedDomainItems.AppRealm> {
        private final BoundedDomainItems.AppRealm realm;

        public AppHandler(@NotNull BoundedDomainItems.AppRealm realm) {
            this.realm = realm;
        }

        public BoundedDomainItems.AppRealm realm() {
            return realm;
        }
    }

    public static class AppPort {
    }
}
