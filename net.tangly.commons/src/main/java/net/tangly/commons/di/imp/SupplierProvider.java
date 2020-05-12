/*
 * Copyright 2006-2019 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.commons.di.imp;

import javax.inject.Provider;
import java.util.function.Supplier;

class SupplierProvider<T> implements Supplier<T> {
    private final Class<T> clazz;
    private final Provider<T> creator;

    public SupplierProvider(Class<T> clazz, Provider<T> creator) {
        this.clazz = clazz;
        this.creator = creator;
    }

    @Override
    public T get() {
        return creator.get();
    }

    /**
     * Returns the class of the instantiated objects.
     *
     * @return class of the instantiated objects
     */
    public Class<T> clazz() {
        return clazz;
    }
}
