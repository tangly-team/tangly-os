/*
 *
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.fsm.actors;

import net.tangly.fsm.Event;

/**
 * The actor abstraction provides an active actor object to process events through a finite state
 * machine.
 */
public interface Actor<E extends Enum<E>> {

    /**
     * The unique name of the actor
     */
    String name();

    /**
     * Returns true if the actor is still alive and ready to process received events.
     *
     * @return true if the actor is alive otherwise false
     */
    boolean isAlive();

    /**
     * Receives an event for further processing.
     *
     * @param event event to process asynchronously later
     */
    void receive(Event<E> event);
}
