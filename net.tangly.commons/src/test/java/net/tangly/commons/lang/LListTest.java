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

package net.tangly.commons.lang;

import net.tangly.commons.lang.functional.LList;
import org.junit.jupiter.api.Test;

import static net.tangly.commons.lang.functional.LList.cons;
import static net.tangly.commons.lang.functional.LList.list;
import static net.tangly.commons.lang.functional.LList.nil;
import static org.assertj.core.api.Assertions.assertThat;

class LListTest {
    @Test
    void testCreateCons() {
        LList<Integer> list = cons(1, cons(2, cons(3, cons(4, cons(5, nil())))));
        assertThat(list.first()).isEqualTo(1);
        assertThat(list.rest().first()).isEqualTo(2);
        assertThat(list.rest().rest().first()).isEqualTo(3);
        assertThat(list.rest().rest().rest().first()).isEqualTo(4);
        assertThat(list.rest().rest().rest().rest().first()).isEqualTo(5);
        assertThat(list.rest().rest().rest().rest().rest()).isEqualTo(nil());
    }

    @Test
    void testCreateList() {
        LList<Integer> list = list(1, 2, 3, 4, 5);
        assertThat(list.first()).isEqualTo(1);
        assertThat(list.rest().first()).isEqualTo(2);
        assertThat(list.rest().rest().first()).isEqualTo(3);
        assertThat(list.rest().rest().rest().first()).isEqualTo(4);
        assertThat(list.rest().rest().rest().rest().first()).isEqualTo(5);
        assertThat(list.rest().rest().rest().rest().rest()).isEqualTo(nil());
    }

    @Test
    void testLength() {
        LList<Integer> list = cons(1, cons(2, cons(3, cons(4, cons(5, nil())))));
        assertThat(list.length()).isEqualTo(5);
        assertThat(LList.nil().length()).isZero();
    }

    @Test
    void testCopy() {
        LList<Integer> list = list(1, 2, 3, 4, 5).copy();
        assertThat(list.first()).isEqualTo(1);
        assertThat(list.rest().first()).isEqualTo(2);
        assertThat(list.rest().rest().first()).isEqualTo(3);
        assertThat(list.rest().rest().rest().first()).isEqualTo(4);
        assertThat(list.rest().rest().rest().rest().first()).isEqualTo(5);
        assertThat(list.rest().rest().rest().rest().rest()).isEqualTo(nil());
    }

    @Test
    void testAppend() {
        LList<Integer> list = list(1, 2, 3, 4, 5).append(list(6, 7, 8, 9, 10));
        assertThat(list.length()).isEqualTo(10);
        assertThat(list.first()).isEqualTo(1);
        assertThat(list.rest().first()).isEqualTo(2);
        assertThat(list.rest().rest().first()).isEqualTo(3);
        assertThat(list.rest().rest().rest().first()).isEqualTo(4);
        assertThat(list.rest().rest().rest().rest().first()).isEqualTo(5);
        assertThat(list.rest().rest().rest().rest().rest().first()).isEqualTo(6);
        assertThat(list.rest().rest().rest().rest().rest().rest().first()).isEqualTo(7);
        assertThat(list.rest().rest().rest().rest().rest().rest().rest().first()).isEqualTo(8);
        assertThat(list.rest().rest().rest().rest().rest().rest().rest().rest().first()).isEqualTo(9);
        assertThat(list.rest().rest().rest().rest().rest().rest().rest().rest().rest().first()).isEqualTo(10);
        assertThat(list.rest().rest().rest().rest().rest().rest().rest().rest().rest().rest()).isEqualTo(nil());
    }

    @Test
    void testListSum() {
        LList<Integer> list = cons(1, cons(2, cons(3, cons(4, cons(5, nil())))));
        assertThat(sum(list)).isEqualTo(15);

    }

    @Test
    void testListSquaredAveraged() {
        LList<Integer> list = cons(1, cons(2, cons(3, cons(4, cons(5, nil())))));
        assertThat(sum(squared(list)) / list.length()).isEqualTo(11);
        assertThat(sum(list.map(o -> o * o)) / list.length()).isEqualTo(11);
        assertThat(list.map(o -> o * o).stream().mapToInt(Integer::intValue).sum() / list.length()).isEqualTo(11);
    }

    private int sum(LList<Integer> list) {
        return list.isEmpty() ? 0 : list.first() + sum(list.rest());
    }

    private LList<Integer> squared(LList<Integer> list) {
        return list.isEmpty() ? list : cons(list.first() * list.first(), squared(list.rest()));
    }
}
