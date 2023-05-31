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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

/**
 * Provide a subscription handler for multiple suppliers and consumers. The type of handled messages is a generic parameter and provides type security. The implementation uses the
 * flow interface of the standard Java API.
 *
 * @param <T>
 */
public class Channel<T> implements AutoCloseable {
    private String name;
    private SubmissionPublisher<T> publisher;

    static class ActorSubscriber<T> implements Flow.Subscriber<T> {
        private static final Logger logger = LogManager.getLogger();
        private final Actor<T> actor;
        private final Channel<T> channel;
        private Flow.Subscription subscription;

        public ActorSubscriber(@NotNull Actor<T> actor, @NotNull Channel<T> channel) {
            this.actor = actor;
            this.channel = channel;
        }

        @Override
        public void onSubscribe(@NotNull Flow.Subscription subscription) {
            logger.atInfo().log("actor {} is subscribed to channel {}", actor.name(), channel.name());
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(@NotNull T message) {
            logger.atInfo().log("actor {} received message {} from channel {}", actor.name(), message, channel.name());
            actor.receive(message);
            subscription.request(1);
        }

        @Override
        public void onError(Throwable e) {
            logger.atError().withThrowable(e).log("actor {} has error on channel {}", actor.name(), channel.name());
        }

        @Override
        public void onComplete() {
            logger.atInfo().log("actor {} has completed on channel {}", actor.name(), channel.name());
        }
    }

    public Channel(@NotNull String name) {
        this.name = name;

        publisher = new SubmissionPublisher<>();
    }

    public String name() {
        return name;
    }

    public SubmissionPublisher<T> publisher() {
        return publisher;
    }

    public void subscribe(@NotNull Actor<T> actor) {
        publisher.subscribe(new ActorSubscriber<>(actor, this));
    }

    public void dispatch(@NotNull T message) {
        publisher.submit(message);
    }

    @Override
    public void close() throws Exception {
        publisher.close();
    }
}
