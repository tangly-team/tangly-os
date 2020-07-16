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
 * Mixin states that the class has an external unique object identifier and a human readable name. The interface supports only querying the external identifier.
 * It is the responsibility of the application to decide if the external identifier can be modified or not.
 */
public interface HasEditableQualifiers extends HasQualifiers {

    /**
     * Sets the unique external identifier of the instance.
     *
     * @param id external identifier
     */
    void id(String id);

    /**
     * Sets the name of the entity.
     *
     * @param name human readable name of the entity
     */
    void name(String name);

    /**
     * Sets the text description of the entity.
     *
     * @param text human readable text description
     */
    void text(String text);
}
