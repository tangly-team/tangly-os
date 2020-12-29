package net.tangly.components.canvases.events;

import com.vaadin.flow.component.DebounceSettings;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.dom.DebouncePhase;
import net.tangly.components.canvases.Canvas;

@DomEvent(value = "mousemove", debounce = @DebounceSettings(timeout = 250, phases = DebouncePhase.INTERMEDIATE))
public class MouseMoveEvent extends MouseEvent {
    public MouseMoveEvent(Canvas source, boolean fromClient, @EventData("event.offsetX") int offsetX, @EventData("event.offsetY") int offsetY,
                          @EventData("event.button") int button) {
        super(source, fromClient, offsetX, offsetY, button);
    }
}
