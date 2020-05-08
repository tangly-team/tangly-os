/*
 * Copyright 2006-2020 Marcel Baumann
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

package net.tangly.bus.core;

/**
 * The interface marks an entity owned by another object.
 */
public interface HasOwner extends HasOid {
    /**
     * Returns the unique object identifier of the owning object.
     *
     * @return owning object identifier if defined otherwise {@link HasId#UNDEFINED_OID}
     */
    long foid();

    /**
     * Sets the unique object identifier of the owning object.
     *
     * @param foid object identifier of the owing object if defined otherwise {@link HasId#UNDEFINED_OID}
     */
    void foid(long foid);
}
