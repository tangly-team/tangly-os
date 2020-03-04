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

package net.tangly.commons.lang;

/**
 * A reference to an object, emulating the concept of typed pointer to typed variable.
 *
 * @param <T> type of the reference
 */
public class Reference<T> {
    private T reference;

    /**
     * Constructor of the class.
     *
     * @param reference reference object
     */
    public Reference(T reference) {
        this.reference = reference;
    }

    /**
     * Constructor of the class to build an empty reference.
     */
    public Reference() {
    }

    /**
     * Sets the referencec object.
     *
     * @param reference referenced object
     */
    public void reference(T reference) {
        this.reference = reference;
    }

    /**
     * Returns the reference object.
     *
     * @return referenced object
     */

    public T reference() {
        return reference;
    }
}
