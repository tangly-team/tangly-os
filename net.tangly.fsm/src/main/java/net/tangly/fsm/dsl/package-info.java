/*
 * Copyright 2006-2023 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

/**
 * <p>The domain specific language builds finite state machine definitions including Lambda expressions and optional human-readable names and
 * descriptions. The DSL assumes that you are working with a state. Once you have retrieved the state you want to modify you can</p>
 * <dl>
 *     <dt>addToRoot</dt><dd>adds a new state to the root state and configure the state properties</dd>
 *     <dt>add</dt><dd>adds a new state to a composite state and configure the state properties</dd>
 *     <dt>onLocal</dt><dd>adds a local transition and configure its event, guard and action. You can addToRoot local transitions and self transitions.</dd>
 *     <dt>on</dt><dd>adds a transition to another state and configure the transition event, guard and action</dd>
 * </dl>
 * <p>The first aspect you configure when adding is always the event triggering the firing of the transaction. The order of all other
 * properties is open.</p>
 * <h2>Features</h2>
 * <ul>
 *     <li>use enums for states and events - resulting in single class state machine definitions statically checked</li>
 *     <li>transition, entry and exit actions. The actions must have either a unique parameter of type Event or no parameters</li>
 *     <li>transition guards. The guards must have either a unique parameter of type Event or no parameters.</li>
 *     <li>hierarchical states with history behavior to initialize state always to same state or last active state</li>
 *     <li>fluent definition syntax</li>
 *     <li>extensible thorough event handlers simplify logging, auditing and debugging</li> <li>state machine ports as text, csv or yEd
 *     diagram</li>
 * </ul>
 *
 * <h2>Create a FSM description with Builder</h2>
 * <p>Below an snippet showing how to construct a finites state machine decscription using the FSM builder.</p>
 * {@snippet file = "net/tangly/fsm/imp/FsmWasherTest.java" region = "declare-fsm-washer-with-builder"}
 */

package net.tangly.fsm.dsl;
