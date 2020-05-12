/*
 * Copyright 2006-2020 Marcel Baumann
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

package net.tangly.commons.lang;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Interface of the handler used for the functional tests.
 */
interface Handler {
    void action();

    void action(Exception e);

    void undefined(Exception e);
}

/**
 * Tests the functions of the ExceptionMapper class.
 */
class ExceptionMapperTest {

    @Test
    void testRegistration() {
        // Given
        var mapper = create();
        // Then
        assertThat(mapper.isRegistered(URISyntaxException.class)).isTrue();
        assertThat(mapper.isRegistered(Exception.class)).isTrue();
        // When
        mapper.unregister(URISyntaxException.class);
        mapper.unregister(Exception.class);
        // Then
        assertThat(mapper.isRegistered(URISyntaxException.class)).isFalse();
        assertThat(mapper.isRegistered(Exception.class)).isTrue();
    }

    @Test
    @DisplayName("Test direct mapping of exception class to a transformation lambda expression")
    void testRegularMapping() {
        // Given
        var mapper = create();
        var handler = mock(Handler.class);
        var exception = new IOException();
        // When
        try {
            throw exception;
        } catch (IOException e) {
            mapper.process(handler, e);
        }
        // Then
        verify(handler).action();
        verify(handler).action(exception);
    }

    @Test
    @DisplayName("Test direct mapping of exception class to a transformation lambda")
    void testRegularMappingWithGeneralExceptionHandling() {
        // Given
        var mapper = create();
        var handler = mock(Handler.class);
        var exception = new IOException();
        // When
        try {
            throw exception;
        } catch (Exception e) {
            mapper.process(handler, e);
        }
        // Then
        verify(handler).action();
        verify(handler).action(exception);
    }

    @Test
    @DisplayName("Test undefined mapping of exception class to default lambda of Exception class")
    void testUndefinedMapping() {
        // Given
        var mapper = create();
        var handler = mock(Handler.class);
        // When
        var exception = new NumberFormatException();
        try {
            throw exception;
        } catch (NumberFormatException e) {
            mapper.process(handler, e);
        }
        // Then
        verify(handler).undefined(exception);
    }


    @Test
    @DisplayName("Test mapping of exception class to a transformation lambda of superclass")
    void testInheritedMapping() {
        // Given
        var mapper = create();
        var handler = mock(Handler.class);
        var exception = new FileNotFoundException();
        // When
        try {
            throw exception;
        } catch (IOException e) {
            mapper.process(handler, e);
        }
        // Then
        verify(handler).action();
        verify(handler).action(exception);
    }

    /**
     * Creates a mapping between the set of test exception classes and lambda transformation expressions.
     *
     * @return the exception mapper used in the unit tests
     */
    private ExceptionMapper<Handler> create() {
        ExceptionMapper<Handler> mapper = new ExceptionMapper<>();
        mapper.register(IOException.class, (h, e) -> {
            h.action();
            h.action(e);
        });
        // defined to infer the non call of this lambda
        mapper.register(URISyntaxException.class, Handler::action);
        // overwrite of default exception lambda
        mapper.register(Exception.class, Handler::undefined);
        return mapper;
    }
}
