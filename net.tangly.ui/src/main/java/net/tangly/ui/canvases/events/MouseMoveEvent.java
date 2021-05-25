/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.ui.canvases.events;

import com.vaadin.flow.component.DebounceSettings;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.dom.DebouncePhase;
import net.tangly.ui.canvases.Canvas;

@DomEvent(value = "mousemove", debounce = @DebounceSettings(timeout = 250, phases = DebouncePhase.INTERMEDIATE))
public class MouseMoveEvent extends MouseEvent {
    public MouseMoveEvent(Canvas source, boolean fromClient, @EventData("event.offsetX") int offsetX, @EventData("event.offsetY") int offsetY,
                          @EventData("event.button") int button) {
        super(source, fromClient, offsetX, offsetY, button);
    }
}
