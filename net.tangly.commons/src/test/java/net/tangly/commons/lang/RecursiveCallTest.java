/*
 * Copyright 2024 Marcel Baumann
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

import net.tangly.commons.lang.functional.RecursiveCall;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RecursiveCallTest {
    static RecursiveCall<Long> factorialLong(Long total, Long summand) {
        if (summand == 1) {
            return RecursiveCall.done(total);
        }
        return () -> factorialLong(total * summand, summand - 1L);
    }

    static RecursiveCall<Long> fibonacciLong(Long n, Long n_2, Long n_1) {
        if (n == 0) {
            return RecursiveCall.done(n_2);
        } else if (n == 1) {
            return RecursiveCall.done(n_1);
        } else {
            return () -> fibonacciLong(n - 1, n_1, n_2 + n_1);
        }
    }

    long factorial(long n) {
        return factorialLong(1L, n).run();
    }

    long fibonacci(long n) {
        return fibonacciLong(n, 0L, 1L).run();
    }

    @Test
    void testFactorial() {
        assertThat(factorial(1L)).isEqualTo(1L);
        assertThat(factorial(10L)).isEqualTo(3628800L);
        assertThat(factorial(20L)).isEqualTo(2432902008176640000L);
    }

    @Test
    void testFibonacci() {
        assertThat(fibonacci(0L)).isEqualTo(0L);
        assertThat(fibonacci(1L)).isEqualTo(1L);
        assertThat(fibonacci(2L)).isEqualTo(1L);
        assertThat(fibonacci(3L)).isEqualTo(2L);
        assertThat(fibonacci(4L)).isEqualTo(3L);
        assertThat(fibonacci(5L)).isEqualTo(5L);
        assertThat(fibonacci(6L)).isEqualTo(8L);
        assertThat(fibonacci(7L)).isEqualTo(13L);
        assertThat(fibonacci(8L)).isEqualTo(21L);
        assertThat(fibonacci(9L)).isEqualTo(34L);
        assertThat(fibonacci(19L)).isEqualTo(4181);
        assertThat(fibonacci(32L)).isEqualTo(2178309L);
        assertThat(fibonacci(35L)).isEqualTo(9227465L);
    }
}
