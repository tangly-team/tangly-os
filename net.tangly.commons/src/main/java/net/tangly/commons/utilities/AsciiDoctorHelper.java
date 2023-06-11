/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.utilities;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * defines helper functions to transform a asciidoc document into a PDF document.
 */
public class AsciiDoctorHelper {
    public static final String ASCIIDOC_EXT = ".adoc";
    public static final String PDF_EXT = ".pdf";

    private AsciiDoctorHelper() {
    }

    public static void createPdf(@NotNull String asciidoc, @NotNull OutputStream out) {
        System.setProperty("jruby.compat.version", "RUBY1_9");
        System.setProperty("jruby.compile.mode", "OFF");
        try (var asciidoctor = Asciidoctor.Factory.create()) {
            var attributes = Attributes.builder().build();
            var options = Options.builder().inPlace(true).attributes(attributes).backend("pdf").safe(SafeMode.UNSAFE).toStream(out).build();
            asciidoctor.convert(asciidoc, options);
        }
    }

    public static void createPdf(@NotNull Path asciidocFilePath, @NotNull Path pdfFilePath) {
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(pdfFilePath))) {
            createPdf(Files.readString(asciidocFilePath), out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
