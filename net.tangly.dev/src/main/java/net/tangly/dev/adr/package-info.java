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
 * Provides support to process architectural design records in an application. The project can define application wide design decisions. Each module can provide
 * own specific architecture guidance.
 * <p>The architecture design records are documented using asciidoc syntax. Used abbreviations are:
 * <dl>
 * <dt>AD</dt><dd>architecture decision is relevant for the architecture and associated costs</dd>
 * <dt>ADL</dt><dd>architecture decision log defines a history of all architecture decisions</dd>
 * <dt>ADR</dt><dd>architecture decision record document a single architecture decision</dd>
 * <dt>AKM</dt><dd>architecture knowledge management to support team members during evolution of the product</dd>
 * <dt>ASR</dt><dd>architecturally-significant requirement constraining the chosen architectureal solution</dd>
 * </dl>
 * <p>If you're considering using decision records with your team, then here's some advice that we've learned by working with many teams.</p>
 * <p>You have an opportunity to lead your teammates, by talking together about the "why", rather than mandating the "what". For example, decision records are a
 * way for teams to think smarter and communicate better; decision records are not valuable if they're just an after-the-fact forced paperwork requirement. <p>
 * <p>Some teams much prefer the name "decisions" over the abbreviation "ADRs". When some teams use the directory name "decisions", then it's as if a light
 * bulb turns on, and the team starts putting more information into the directory, such as vendor decisions, planning decisions, scheduling decisions, etc.
 * All of these kinds of information can use the same template. We hypothesize that people learn faster with words ("decisions") over abbreviations ("ADRs"),
 * and people are more motivated to write work-in-progress docs when the word "record" is removed, and also some developers and some managers dislike the word
 * "architecture".</p>
 * <p> In theory, immutability is ideal. In practice, mutability has worked better for our teams. We insert the new info the existing ADR, with a date stamp,
 * and a note that the info arrived after the decision. This kind of approach leads to a "living document" that we all can update. Typical updates are when
 * we get information thanks to new teammates, or new offerings, or real-world results of our usages, or after-the-fact third-party changes such as vendor
 * capabilties, pricing plans, license agreements, etc.</p>
 */
package net.tangly.dev.adr;
