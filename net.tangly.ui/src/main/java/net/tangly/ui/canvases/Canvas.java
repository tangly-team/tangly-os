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

package net.tangly.ui.canvases;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.shared.Registration;
import net.tangly.ui.canvases.events.ImageLoadEvent;
import net.tangly.ui.canvases.events.MouseClickEvent;
import net.tangly.ui.canvases.events.MouseDblClickEvent;
import net.tangly.ui.canvases.events.MouseDownEvent;
import net.tangly.ui.canvases.events.MouseMoveEvent;
import net.tangly.ui.canvases.events.MouseUpEvent;

/**
 * Canvas component that you can draw shapes and images on. It's a Java wrapper for the
 * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Canvas_API">HTML5
 * canvas</a>.
 * <p>The component is derived from the work under <a href="https://github.com/pekam/vaadin-flow-canvas">Canvas</a>.</p>
 * Use {@link #getContext()} to get API for rendering shapes and images on the canvas.
 */
@Tag("canvas")
@SuppressWarnings("serial")
public class Canvas extends Component implements HasStyle, HasSize, KeyNotifier {
    private transient CanvasRenderingContext2D context;

    /**
     * Creates a new canvas component with the given size. Use the API provided by {@link #getContext()} to render graphics on the canvas. The width and height
     * parameters will be used for the canvas' coordinate system. They will determine the size of the component in pixels, unless you explicitly set the
     * component's size with {@link #setWidth(String)} or {@link #setHeight(String)}.
     *
     * @param width  the width of the canvas
     * @param height the height of the canvas
     */
    public Canvas(int width, int height) {
        context = new CanvasRenderingContext2D(this);

        getElement().setAttribute("width", String.valueOf(width));
        getElement().setAttribute("height", String.valueOf(height));
    }

    /**
     * Gets the context for rendering shapes and images in the canvas. It is a Java wrapper for the <a href= "https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D">same
     * client-side API</a>.
     *
     * @return the 2D rendering context of this canvas
     */
    public CanvasRenderingContext2D getContext() {
        return context;
    }

    /**
     * {@inheritDoc}
     * <p><b>NOTE:</b> Canvas has an internal coordinate system that it uses for drawing, and it uses the width and height provided in the constructor. This
     * coordinate system is independent of the component's size. Changing the component's size with this method may scale/stretch the rendered graphics.</p>
     */
    @Override
    public void setWidth(String width) {
        HasSize.super.setWidth(width);
    }

    /**
     * {@inheritDoc}
     * <p> <b>NOTE:</b> Canvas has an internal coordinate system that it uses for drawing, and it uses the width and height provided in the constructor. This
     * coordinate system is independent of the component's size. Changing the component's size with this method may scale/stretch the rendered graphics.</p>
     */
    @Override
    public void setHeight(String height) {
        HasSize.super.setHeight(height);
    }

    /**
     * {@inheritDoc}
     * <p><b>NOTE:</b> Canvas has an internal coordinate system that it uses for drawing, and it uses the width and height provided in the constructor. This
     * coordinate system is independent of the component's size. Changing the component's size with this method may scale/stretch the rendered graphics.</p>
     */
    @Override
    public void setSizeFull() {
        HasSize.super.setSizeFull();
    }

    /**
     * Load an image resource an prepare it for use as a fill or stroke style. Since images are loaded asyncronously, you need to wait until you receive the
     * associated ImageLoadEvent. Register an event listener using addImageLoadListener().
     *
     * @param src the path to the image resource
     */
    public void loadImage(String src) {
        getElement().executeJavaScript(
            "var img = new Image();" + "var self = this;" + "img.onload = function () {" + "if (!self.images) self.images = {};" + "self.images[$0] = img;" +
                "self.$server.imageLoaded($0);" + "};" + "img.src=$0;", src);
    }

    @ClientCallable
    public void imageLoaded(String src) {
        fireEvent(new ImageLoadEvent(this, true, src));
    }

    public Registration addMouseDownListener(ComponentEventListener<MouseDownEvent> listener) {
        return addListener(MouseDownEvent.class, listener);
    }

    public Registration addMouseUpListener(ComponentEventListener<MouseUpEvent> listener) {
        return addListener(MouseUpEvent.class, listener);
    }

    public Registration addMouseMoveListener(ComponentEventListener<MouseMoveEvent> listener) {
        return addListener(MouseMoveEvent.class, listener);
    }

    public Registration addMouseClickListener(ComponentEventListener<MouseClickEvent> listener) {
        return addListener(MouseClickEvent.class, listener);
    }

    public Registration addMouseDblClickListener(ComponentEventListener<MouseDblClickEvent> listener) {
        return addListener(MouseDblClickEvent.class, listener);
    }

    public Registration addImageLoadListener(ComponentEventListener<ImageLoadEvent> listener) {
        return addListener(ImageLoadEvent.class, listener);
    }
}
