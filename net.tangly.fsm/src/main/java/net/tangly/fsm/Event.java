/*
 * Copyright 2006-2018 Marcel Baumann
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

package net.tangly.fsm;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * The event is an immutable class containing the event with payload sent to the finite state machine. The event contains all the context information
 * necessary to process the system change with the machine and the context of the machine.
 *
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
public class Event<E extends Enum<E>> {

    /**
     * The identifier of the type of the event.
     */
    private final E type;

    /**
     * The parameters of this instance of event.
     */
    private final List<Object> parameters;

    /**
     * Constructor of the class.
     *
     * @param type       identifier of the type of the event
     * @param parameters parameters of the event
     */
    public Event(E type, final List<Object> parameters) {
        this.type = type;
        this.parameters = (parameters != null) ? List.copyOf(parameters) : Collections.emptyList();
    }

    /**
     * Constructor of the class.
     *
     * @param type       identifier of the type of the event
     * @param parameters parameters of the event
     */
    public Event(E type, Object... parameters) {
        this(type, List.of(parameters));
    }

    /**
     * Constructor of the class for an instance without parameters.
     *
     * @param type identifier of the type of the event
     */
    public Event(E type) {
        this.type = type;
        this.parameters = Collections.emptyList();
    }

    /**
     * Returns the identifier of the event.
     *
     * @return the identifier of the event
     */
    public E type() {
        return type;
    }

    /**
     * Returns the parameters of the event.
     *
     * @return parameters of the event
     */
    public List<Object> parameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]").add("type=" + type).add("parameters=" + parameters).toString();
    }
}