/*
 * Copyright 2006-2024 Marcel Baumann
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

package net.tangly.commons.lang;

import net.tangly.commons.lang.exceptions.ThrowingConsumer;
import net.tangly.commons.lang.exceptions.ThrowingFunction;
import net.tangly.commons.lang.exceptions.ThrowingIOExceptionConsumer;
import net.tangly.commons.lang.exceptions.ThrowingPredicate;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ThrowingTest {
    void consumer(Integer value) throws Exception {
        if (value == 100) {
            throw new Exception();
        }
    }

    void consumerWithIOException(Integer value) throws IOException {
        if (value == 100) {
            throw new IOException();
        }
    }

    Double function(Integer value) throws TimeoutException {
        if (value != 50) {
            return Double.valueOf(value);
        } else {
            throw new TimeoutException();
        }
    }

    boolean predicate(Integer value) throws URISyntaxException {
        if (value != 100) {
            return true;
        } else {
            throw new URISyntaxException("", "");
        }
    }


    @Test
    void testWrappers() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> IntStream.range(1, 101).boxed().forEach(ThrowingIOExceptionConsumer.of(this::consumerWithIOException)));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> IntStream.range(1, 101).boxed().forEach(ThrowingConsumer.of(this::consumer)));
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> IntStream.range(1, 101).boxed().map(ThrowingFunction.of(this::function)).forEach(o -> {
            }));
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> IntStream.range(1, 101).boxed().filter(ThrowingPredicate.of(this::predicate)).forEach(ThrowingConsumer.of(this::consumer)));
    }
}
