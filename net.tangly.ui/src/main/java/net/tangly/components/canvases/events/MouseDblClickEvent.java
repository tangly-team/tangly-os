package net.tangly.components.canvases.events;

import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import net.tangly.components.canvases.Canvas;

@DomEvent("dblclick")
public class MouseDblClickEvent extends MouseEvent {
    public MouseDblClickEvent(Canvas source, boolean fromClient, @EventData("event.offsetX") int offsetX, @EventData("event.offsetX") int offsetY,
                              @EventData("event.button") int button) {
        super(source, fromClient, offsetX, offsetY, button);
    }
}
