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

package net.tangly.fsm.utilities;

import net.tangly.fsm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * The state machine logger documents all activities performed during the firing of events for a
 * specific finite state machine instance. All logs have the following structure:
 * machine_name[machine_oid] operation operation parameters
 */
public class StateMachineLogger<O, S extends Enum<S>, E extends Enum<E>> implements StateMachineEventHandler<O, S, E> {
    /**
     * /** The logger of the instance.
     */
    private static final Logger logger = LoggerFactory.getLogger(StateMachineLogger.class);

    /**
     * The machine being logged with this logger handler instance.
     */
    private final StateMachine<O, S, E> machine;


    /**
     * Constructor of the class
     *
     * @param machine finite state machine to check
     */
    public StateMachineLogger(StateMachine<O, S, E> machine) {
        this.machine = machine;
    }

    @Override
    public void processEvent(Event<E> event) {
        logger.debug("machine [{}] Process Event : event->{}", machine, event);
    }

    @Override
    public void wasReset() {
        logger.debug("machine [{}] reset machine : <none>", machine);
    }

    @Override
    public void fireLocalTransition(Transition<O, S, E> transition, Event<E> event) {
        logger.debug("machine [{}]: event->{}, fire local transition {} ", machine, event, transition);
    }

    @Override
    public void fireTransition(Transition<O, S, E> transition, Event<E> event) {
        logger.debug("machine [{}]: event->{}, fire transition->{}", machine, event, transition);
    }

    @Override
    public void executeEntryAction(State<O, S, E> state, Event<E> event) {
        logger.debug("machine [{}] : event->{}, execute entry action for state->{}", machine, event, state);
    }

    @Override
    public void executeExitAction(State<O, S, E> state, Event<E> event) {
        logger.debug("machine [{}] : event->{}, execute exit action for state->{}", machine, event, state);
    }

    @Override
    public void enterState(State<O, S, E> state) {
        logger.debug("machine [{}] : enter state {}", machine, state);
    }

    @Override
    public void exitState(State<O, S, E> state) {
        logger.debug("machine [{}] : exit state {}", machine, state);
    }


    @Override
    public void throwException(Transition<O, S, E> transition, Event<E> event, Exception exception) {
        logger.error("machine [{}] {} : event->{}, transition->{}, exception->{}", machine, event, transition, exception);
    }

    @Override
    public void throwException(State<O, S, E> state, BiConsumer<O, Event<E>> action, Event<E> event, Exception exception) {
        if (Objects.equals(action, state.entryAction())) {
            logger.error("machine [{}] {} : event->{}, state->{}, entry action exception->{}", machine, event, state, exception);
        } else if (Objects.equals(action, state.exitAction())) {
            logger.error("machine [{}] {} : event->{}, state->{}, exit action exception->{}", machine, event, state, exception);
        }
    }
}
