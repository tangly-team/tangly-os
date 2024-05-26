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

package net.tangly.commons.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Plugin(name = AppenderInMemory.IN_MEMORY_APPENDER, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class AppenderInMemory extends AbstractAppender {

    private static final long serialVersionUID = 8047713135100613186L;

    public static final String IN_MEMORY_APPENDER = "InMemoryAppender";

    private List<LogStatement> messages = new ArrayList<>();

    protected AppenderInMemory(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, false, Property.EMPTY_ARRAY);
    }

    public static AppenderInMemory getAppender() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final AppenderInMemory inMemoryAppender = (AppenderInMemory) config.getAppenders().get(IN_MEMORY_APPENDER);
        return inMemoryAppender;
    }

    @Override
    public void append(final LogEvent event) {
        final String level = event.getLevel().toString();
        final String message = event.getMessage().getFormattedMessage();
        final long timeMillis = event.getTimeMillis();
        final LogStatement logStatement = new LogStatement(level, message, timeMillis);
        getMessages().add(logStatement);
    }

    @PluginFactory
    public static AppenderInMemory createAppender(@PluginAttribute("name") String name, @PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filter") final Filter filter, @PluginAttribute("otherAttribute") String otherAttribute) {
        if (name == null) {
            name = IN_MEMORY_APPENDER;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new AppenderInMemory(name, filter, layout);
    }

    public List<LogStatement> getMessages() {
        return this.messages;
    }

    public void setMessages(List<LogStatement> messages) {
        this.messages = messages;
    }
}
