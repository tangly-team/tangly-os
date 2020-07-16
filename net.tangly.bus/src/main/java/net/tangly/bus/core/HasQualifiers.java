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

package net.tangly.bus.core;

/**
 * Mixin states that the class has an external unique object identifier, a human readable name, and a text description. The interface supports only querying the
 * external identifier. It is the responsibility of the application to decide if the external identifier can be modified or not. Reasonable defaults are
 * provided for name and text and are derived from the external identifier.
 */
public interface HasQualifiers extends HasId {
    /**
     * Returns the human readable name of the instance.
     *
     * @return human readable name of the instance
     */
    default String name() {
        return id();
    }

    /**
     * Returns the human readable text description of the instance.
     *
     * @return human readable text description of the instance
     */
    default String text() {
        return name();
    }
}
