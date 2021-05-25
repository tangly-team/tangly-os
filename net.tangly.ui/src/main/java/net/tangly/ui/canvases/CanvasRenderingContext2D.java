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

import java.io.Serializable;

/**
 * The context for rendering shapes and images on a canvas.
 * <p>
 * This is a Java wrapper for the <a href=
 * "https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D">same
 * client-side API</a>.
 */
public class CanvasRenderingContext2D {

    private Canvas canvas;

    protected CanvasRenderingContext2D(Canvas canvas) {
        this.canvas = canvas;
    }

    public void setFillStyle(String fillStyle) {
        setProperty("fillStyle", fillStyle);
    }

    /**
     * Set a pattern to use as a fill style. Must reference an image source previously loaded
     * with Canvas.loadImage().
     *
     * @param src the path to the image resource
     * @param type the pattern repeat type (see the Canvas API)
     */
    public void setPatternFillStyle(String src, String type)
    {
        runScript(String.format(
            "if ($0.images) {"
                + "var img = $0.images['%s'];"
                + "if (img) {"
                +   "var ctx = $0.getContext('2d');"
                +   "var pat = ctx.createPattern(img, '%s');"
                +   "ctx.fillStyle = pat;"
                + "}"
                + "}", src, type));
    }

    public void setStrokeStyle(String strokeStyle) {
        setProperty("strokeStyle", strokeStyle);
    }

    /**
     * Set a pattern to use as a strok -style. Must reference an image source previously loaded
     * with Canvas.loadImage().
     *
     * @param src the path to the image resource
     * @param type the pattern repeat type (see the Canvas API)
     */
    public void setPatternStrokeStyle(String src, String type)
    {
        runScript(String.format(
            "if ($0.images) {"
                + "var img = $0.images['%s'];"
                + "if (img) {"
                +   "var ctx = $0.getContext('2d');"
                +   "var pat = ctx.createPattern(img, '%s');"
                +   "ctx.strokeStyle = pat;"
                + "}"
                + "}", src, type));
    }

    public void setLineWidth(double lineWidth) {
        setProperty("lineWidth", lineWidth);
    }

    public void setFont(String font) {
        setProperty("font", font);
    }

    public void arc(double x, double y, double radius, double startAngle,
                    double endAngle, boolean antiClockwise) {
        callJsMethod("arc", x, y, radius, startAngle, endAngle, antiClockwise);
    }

    public void beginPath() {
        callJsMethod("beginPath");
    }

    public void clearRect(double x, double y, double width, double height) {
        callJsMethod("clearRect", x, y, width, height);
    }

    public void closePath() {
        callJsMethod("closePath");
    }

    /**
     * Fetches the image from the given location and draws it on the canvas.
     * <p>
     * <b>NOTE:</b> Unless you have previously loaded the image with Canvas.loadImage, the
     * drawing will happen asynchronously after the browser has retrieved the image.
     *
     * @param src
     *            the url of the image to draw
     * @param x
     *            the x-coordinate of the top-left corner of the image
     * @param y
     *            the y-coordinate of the top-left corner of the image
     */
    public void drawImage(String src, double x, double y) {
        runScript(String.format(
            //@formatter:off
            "var img = null;"
                + "if ($0.images) img = $0.images['%s'];"
                + "if (img != null)"
                + "  $0.getContext('2d').drawImage(img, %s, %s);"
                + "else {"
                + "  img = new Image();"
                + "  img.onload = function () {"
                + "    $0.getContext('2d').drawImage(img, %s, %s);"
                + "  };"
                + "  img.src='%s';"
                + "}",
            src, x, y, x, y, src));
        //@formatter:on
    }

    /**
     * Fetches the image from the given location and draws it on the canvas.
     * <p>
     * <b>NOTE:</b> Unless you have previously loaded the image with Canvas.loadImage, the
     * drawing will happen asynchronously after the browser has retrieved the image.
     *
     * @param src
     *            the url of the image to draw
     * @param x
     *            the x-coordinate of the top-left corner of the image
     * @param y
     *            the y-coordinate of the top-left corner of the image
     * @param width
     *            the width for the image
     * @param height
     *            the height for the image
     */
    public void drawImage(String src, double x, double y, double width,
                          double height) {
        runScript(String.format(
            //@formatter:off
            "var img = null;"
                + "if ($0.images) img = $0.images['%s'];"
                + "if (img != null)"
                + "  $0.getContext('2d').drawImage(img, %s, %s, %s, %s);"
                + "else {"
                + "  img = new Image();"
                + "  img.onload = function () {"
                + "    $0.getContext('2d').drawImage(img, %s, %s, %s, %s);"
                + "  };"
                + "  img.src='%s';"
                + "}",
            src, x, y, width, height, x, y, width, height, src));
        //@formatter:on
    }

    public void fill() {
        callJsMethod("fill");
    }

    public void fillRect(double x, double y, double width, double height) {
        callJsMethod("fillRect", x, y, width, height);
    }

    public void fillText(String text, double x, double y) {
        callJsMethod("fillText", text, x, y);
    }

    public void lineTo(double x, double y) {
        callJsMethod("lineTo", x, y);
    }

    public void moveTo(double x, double y) {
        callJsMethod("moveTo", x, y);
    }

    public void rect(double x, double y, double width, double height) {
        callJsMethod("rect", x, y, width, height);
    }

    public void restore() {
        callJsMethod("restore");
    }

    public void rotate(double angle) {
        callJsMethod("rotate", angle);
    }

    public void save() {
        callJsMethod("save");
    }

    public void scale(double x, double y) {
        callJsMethod("scale", x, y);
    }

    public void stroke() {
        callJsMethod("stroke");
    }

    public void strokeRect(double x, double y, double width, double height) {
        callJsMethod("strokeRect", x, y, width, height);
    }

    public void strokeText(String text, double x, double y) {
        callJsMethod("strokeText", text, x, y);
    }

    public void translate(double x, double y) {
        callJsMethod("translate", x, y);
    }

    protected void setProperty(String propertyName, Serializable value) {
        runScript(String.format("$0.getContext('2d').%s='%s'", propertyName,
            value));
    }

    /**
     * Runs the given js so that the execution order works with callJsMethod().
     * Any $0 in the script will refer to the canvas element.
     */
    private void runScript(String script) {
        canvas.getElement().getNode().runWhenAttached(
            // This structure is needed to make the execution order work
            // with Element.callFunction() which is used in callJsMethod()
            ui -> ui.getInternals().getStateTree().beforeClientResponse(
                canvas.getElement().getNode(),
                context -> ui.getPage().executeJavaScript(script,
                    canvas.getElement())));
    }

    protected void callJsMethod(String methodName, Serializable... parameters) {
        canvas.getElement().callFunction("getContext('2d')." + methodName,
            parameters);
    }

}
