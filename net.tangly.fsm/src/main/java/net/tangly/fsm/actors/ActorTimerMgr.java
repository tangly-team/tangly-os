/*
 * Copyright 2023 Marcel Baumann
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

package net.tangly.fsm.actors;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ActorTimerMgr<T> extends ActorImp<T> implements Actor<T> {
    enum TimerCommands {
        CREATE, CANCEL, ABORT
    }

    public record Timer<T>(Actor<T> client, String name, long alarmTimeInNanoSeconds, boolean recurring, long delayInNanoSeconds) implements Comparable<Timer<T>> {
        static <T> Timer<T> ofOnceAbsolute(Actor<T> client, String name, long absolute, TimeUnit unit) {
            return new Timer<>(client, name, TimeUnit.NANOSECONDS.convert(absolute, unit), false, 0);
        }

        static <T> Timer<T> ofOnce(Actor<T> client, String name, long duration, TimeUnit unit) {
            return new Timer<>(client, name, 0, false, TimeUnit.NANOSECONDS.convert(duration, unit));
        }

        static <T> Timer<T> ofRecurringAbsolute(Actor<T> client, String name, long absolute, TimeUnit absoluteUnit, long duration, TimeUnit unit) {
            return new Timer<>(client, name, TimeUnit.NANOSECONDS.convert(absolute, absoluteUnit), true, TimeUnit.NANOSECONDS.convert(duration, unit));
        }

        static <T> Timer<T> ofRecurring(Actor<T> client, String name, long duration, TimeUnit unit) {
            return new Timer<>(client, name, 0, true, TimeUnit.NANOSECONDS.convert(duration, unit));
        }

        @Override
        public int compareTo(@NotNull ActorTimerMgr.Timer<T> timer) {
            return Long.compare(alarmTimeInNanoSeconds(), timer.alarmTimeInNanoSeconds());
        }
    }

    public record TimerCmd<T>(TimerCommands command, Timer<T> timer) {
    }

    public ActorTimerMgr(@NotNull String name, @NotNull Function<T, TimerCmd<T>> extractor, @NotNull Function<Timer<T>, T> builder) {
        super(name);
        this.timers = new ArrayList<>();
        this.extractor = extractor;
        this.builder = builder;
    }

    public void createTimer(@NotNull Timer<T> timer, @NotNull Function<TimerCmd<T>, T> builder) {
        receive(builder.apply(new TimerCmd<>(TimerCommands.CREATE, timer)));
    }

    public void cancelTimer(@NotNull Actor<T> client, @NotNull String name, @NotNull Function<TimerCmd<T>, T> builder) {
        receive(builder.apply(new TimerCmd<>(TimerCommands.CANCEL, Timer.ofOnce(client, name, 0, TimeUnit.NANOSECONDS))));
    }

    @Override
    protected boolean process(@NotNull T msg) {
        boolean continues = true;
        if (extractor.apply(msg) instanceof TimerCmd<T> cmd) {
            switch (cmd.command) {
                case CANCEL -> removeTimer(cmd.timer());
                case CREATE -> scheduleNextOccurrence(cmd.timer());
                case ABORT -> continues = false;
            }
        }
        return continues;
    }

    @Override
    public void run() {
        boolean continues = true;
        while (continues) {
            T msg = message(waitFor(), TimeUnit.NANOSECONDS);
            if (extractor.apply(msg) instanceof TimerCmd<T>) {
                continues = process(msg);
            } else {
                processTimeout();
            }
        }
    }

    private void processTimeout() {
        long currentTime = System.nanoTime();
        while (!timers.isEmpty() && (timers.getFirst().alarmTimeInNanoSeconds() <= currentTime)) {
            Timer timer = timers.getFirst();
            Actor.send(timer.client(), builder.apply(timer));
            timers.remove(timer);
            if (timer.recurring()) {
                scheduleNextOccurrence(timer);
            }
        }
    }

    private void scheduleNextOccurrence(@NotNull Timer<T> timer) {
        long alarmTime = timer.alarmTimeInNanoSeconds() == 0 ? System.nanoTime() + timer.delayInNanoSeconds() : timer.alarmTimeInNanoSeconds() + timer.delayInNanoSeconds();
        timers.add(new Timer<>(timer.client(), timer.name(), alarmTime, timer.recurring(), timer.delayInNanoSeconds()));
        Collections.sort(timers);
    }

    private void removeTimer(@NotNull Timer<T> timer) {
        timers.stream().filter(o -> (o.client().equals(timer.client()) && (o.name().equals(timer.name())))).findAny().ifPresent(timers::remove);
    }

    private long waitFor() {
        return timers.isEmpty() ? 0 : Math.max(timers.getFirst().alarmTimeInNanoSeconds - System.nanoTime(), 1);
    }

    private final List<Timer<T>> timers;
    private final Function<T, TimerCmd<T>> extractor;
    private final Function<Timer<T>, T> builder;
}
