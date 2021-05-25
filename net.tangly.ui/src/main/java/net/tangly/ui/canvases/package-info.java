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

/**
 * The API is similar to its client-side counterpart:
 *
 * <pre>@{code
 * Canvas canvas = new Canvas(800, 500);
 * CanvasRenderingContext2D ctx = canvas.getContext();
 *
 * // Draw a red line from point (10,10) to (100,100):
 * ctx.setStrokeStyle("red");
 * ctx.beginPath();
 * ctx.moveTo(10, 10);
 * ctx.lineTo(100, 100);
 * ctx.closePath();
 * ctx.stroke();
 * }</pre>
 */
package net.tangly.ui.canvases;
