/*
 * Copyright 2023-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.fsm.actors;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static net.tangly.fsm.actors.ActorTimerMgr.Timer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ActorTimerMgrTest {
    sealed interface Message permits MessageTimerCmd, MessageTimer {
    }

    record MessageTimerCmd(ActorTimerMgr.TimerCmd<Message> cmd) implements Message {
    }

    record MessageTimer(Timer<Message> timer) implements Message {
    }

    static class ClientActor<Message> extends ActorImp<Message> {
        ClientActor(ExecutorService executor) {
            super("Client", executor);
            counter = new AtomicInteger();
        }

        int numberOfReceivedMsgs() {
            return counter.get();
        }

        @Override
        protected boolean process(@NotNull Message msg) {
            System.out.println(msg);
            counter.addAndGet(1);
            return true;
        }

        private final AtomicInteger counter;
    }

    @Test
    void createCmdTest() {
        Actors<Message> actors = new Actors<>();
        ActorTimerMgr<Message> timerMgr = new ActorTimerMgr<>("test-timer-manager", actors.executor(), o -> (o instanceof MessageTimerCmd cmd) ? cmd.cmd() :
            null,
            MessageTimer::new);
        actors.register(timerMgr);
        ClientActor<Message> actor = new ClientActor<>(actors.executor());
        actors.register(actor);
        Timer<Message> timer = Timer.ofOnce(actor, "Once", 100, TimeUnit.MILLISECONDS);

        assertThat(actor.numberOfReceivedMsgs()).isZero();

        timerMgr.createTimer(timer, MessageTimerCmd::new);
        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> assertThat(actor.numberOfReceivedMsgs()).isNotZero());

        timer = Timer.ofRecurring(actor, "Recurring", 100, TimeUnit.MILLISECONDS);
        timerMgr.createTimer(timer, MessageTimerCmd::new);
        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> assertThat(actor.numberOfReceivedMsgs()).isBetween(5, 6));

        timerMgr.cancelTimer(actor, "Cancel", MessageTimerCmd::new);
        System.out.println(actor.numberOfReceivedMsgs());
        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> assertThat(actor.numberOfReceivedMsgs()).isBetween(5, 10));
    }

    @Test
    void testRecurringTimer() {
        Actors<Message> actors = new Actors<>();
        ActorTimerMgr<Message> timerMgr =
            new ActorTimerMgr<>("test-timer-manager", actors.executor(), o -> (o instanceof MessageTimerCmd cmd) ? cmd.cmd() : null, MessageTimer::new);
        actors.register(timerMgr);
    }
}
