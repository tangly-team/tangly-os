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

package net.tangly.commons.utilities;

import org.asciidoctor.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AsciiDoctorHelper {
    public static final String ASCIIDOC_EXT = ".adoc";
    public static final String PDF_EXT = ".pdf";

    private AsciiDoctorHelper() {
    }

    public static void createPdf(@NotNull Path asciidocFilePath, @NotNull Path pdfFilePath) {
        System.setProperty("jruby.compat.version", "RUBY1_9");
        System.setProperty("jruby.compile.mode", "OFF");
        try (Asciidoctor asciidoctor = Asciidoctor.Factory.create(); OutputStream out = Files.newOutputStream(pdfFilePath)) {
            String asciidoc = Files.readString(asciidocFilePath);
            Attributes attributes = AttributesBuilder.attributes().get();
            Options options = OptionsBuilder.options().inPlace(true).attributes(attributes).backend("pdf").safe(SafeMode.UNSAFE).toStream(out).get();
            asciidoctor.convert(asciidoc, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
