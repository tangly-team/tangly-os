/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.fsm;

import java.util.Collections;
import java.util.List;

/**
 * The event is an immutable class containing the event with payload sent to the finite state machine. The event contains all the context information necessary
 * to process the system change with the machine and the context of the machine.
 *
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
public record Event<E extends Enum<E>>(E type, List<Object> parameters) {

    public static <E extends Enum<E>> Event<E> of(E type, Object... parameters) {
        return new Event<>(type, List.of(parameters));
    }

    public static <E extends Enum<E>> Event<E> of(E type) {
        return new Event<>(type, Collections.emptyList());
    }
}
