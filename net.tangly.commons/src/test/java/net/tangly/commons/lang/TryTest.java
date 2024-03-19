/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.lang;

import net.tangly.commons.lang.functional.Try;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class TryTest {
    @Test
    void testConstructors() {
        final String success = "Success";
        Try<String> tries = Try.success(success);
        assertThat(tries.isSuccess()).isTrue();
        assertThat(tries.isFailure()).isFalse();
        assertThat(tries.get()).isEqualTo(success);

        tries = Try.of(() -> success);
        assertThat(tries.isSuccess()).isTrue();
        assertThat(tries.isFailure()).isFalse();
        assertThat(tries.get()).isEqualTo(success);

        tries = Try.failure(new RuntimeException());
        assertThat(tries.isSuccess()).isFalse();
        assertThat(tries.isFailure()).isTrue();

        tries = Try.of(() -> {
            throw new RuntimeException();
        });
        assertThat(tries.isSuccess()).isFalse();
        assertThat(tries.isFailure()).isTrue();

    }

    @Test
    void testSuccessTry() {
        final double value = 1.1;
        var tries = Try.of(() -> value);
        assertThat(tries.isFailure()).isFalse();
        assertThat(tries.isSuccess()).isTrue();
        assertThat(tries.get()).isEqualTo(value);
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(tries::getThrown);
        assertThat(tries.map(Double::intValue).get()).isEqualTo(Double.valueOf(value).intValue());
        assertThat(tries.flatMap(o -> Try.success(String.valueOf(o))).isSuccess()).isTrue();
        assertThat(tries.flatMap(o -> Try.success(String.valueOf(o))).get()).isEqualTo(String.valueOf(value));
    }

    @Test
    void testFailureTry() {
        var tries = Try.of(() -> {
            throw new RuntimeException();
        });
        assertThat(tries.isFailure()).isTrue();
        assertThat(tries.isSuccess()).isFalse();
        assertThat(tries.getThrown()).isInstanceOf(RuntimeException.class);
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(tries::get);
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> tries.map(Object::toString).get());
        assertThat(tries.flatMap(o -> Try.failure(new IllegalStateException())).isFailure()).isTrue();
        assertThat(tries.flatMap(o -> Try.failure(new IllegalStateException())).getThrown()).isInstanceOf(RuntimeException.class);
    }

}
