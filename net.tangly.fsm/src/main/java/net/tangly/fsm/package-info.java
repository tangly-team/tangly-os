/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

/**
 * <p>The package defines the interfaces to access and work with hierarchical state machines. The interfaces are designed to provide an immutable
 * representation of finite state machine definition. The finite state machine aims to provide following features:</p>
 * <ul>
 * <li>Easy to use flat one level state machine for simple use cases.</li>
 * <li>Hierarchical state machine structure to ease complex state configuration.</li>
 * <li>Usage of triggers, transitions, guards, transition actions, and state entry or exit actions.</li>
 * <li>Type safe configuration based on Java generics.</li>
 * <li>Builder pattern for easy instantiation and legible declaration.</li>
 * <li>State machine event listeners for advanced requirements.</li>
 * </ul>
 * <h2>Definitions</h2> <p>The actual state of a state machine is given by its active state configuration and by the contents of its event queue.
 * The active state configuration is the tree of active states; in particular, for every concurrent composite state each of its orthogonal regions is active.
 * The event queue holds the events that have not yet been handled by the machine.</p>
 * <p>The event dispatcher selects the first event from the queue; the event is then processed in a run-to-completion (RTC) step. First, a
 * maximally consistent set of enabled transitions is chosen: a transition is enabled if all of its source states are contained in the active state
 * configuration, if its trigger is matched by the current event, and if its guard is true; two enabled transitions are consistent if they do not share a source
 * state. The set must have a maximum size of one.</p>
 * <p>The least common ancestor (LCA) of the enabled transition is determined, i.e. the lowest composite state that contains all the transition
 * source and target states. The transition main source state, that is the direct substate of the LCA containing the source states, is deactivated, the
 * transition actions are executed, and its target states are activated.</p>
 * <h2>Decisions</h2>
 * <ul> <li>We do not support concurrent composite states. By design you should map concurrency to multiple instances and use the concurrency
 * semantic provided through the programming language. </li>
 * <li>We do not support transitions without an explicit trigger, are called completion transitions and are triggered by completion events which are
 * emitted when a state completes all its internal activities</li> </ul>
 */
package net.tangly.fsm;
