/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.commons.utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.jetbrains.annotations.NotNull;

public class AsciiDoctorHelper {
    public static final String ASCII_DOC_EXT = ".adoc";
    public static final String PDF_EXT = ".pdf";

    public static void createPdfWithLocalFile(@NotNull Path asciidocInvoice, @NotNull Path directory, @NotNull String filenameWithoutExtension) {
        try {
            Path asciiDocInvoiceCopy = directory.resolve(filenameWithoutExtension + ASCII_DOC_EXT);
            Files.copy(asciidocInvoice, asciiDocInvoiceCopy);
            System.setProperty("jruby.compat.version", "RUBY1_9");
            System.setProperty("jruby.compile.mode", "OFF");
            try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
                Map<String, Object> options = OptionsBuilder.options().inPlace(true).backend("pdf").safe(SafeMode.UNSAFE).asMap();
                asciidoctor.convertFile(asciiDocInvoiceCopy.toFile(), options);
            }
            Files.delete(asciiDocInvoiceCopy);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void createPdf(@NotNull Path directory, @NotNull String filenameWithoutExtension) {
        createPdf(directory.resolve(filenameWithoutExtension + ASCII_DOC_EXT), directory, filenameWithoutExtension);
    }

    public static void createPdf(@NotNull Path asciidocPath, @NotNull Path pdfDirectory, @NotNull String filenameWithoutExtension) {
        System.setProperty("jruby.compat.version", "RUBY1_9");
        System.setProperty("jruby.compile.mode", "OFF");
        try (Asciidoctor asciidoctor = Asciidoctor.Factory.create();
             OutputStream out = Files.newOutputStream(pdfDirectory.resolve(filenameWithoutExtension + PDF_EXT))) {
            String asciidoc = Files.readString(asciidocPath);
            Attributes attributes = AttributesBuilder.attributes().get();
            Options options = OptionsBuilder.options().inPlace(true).attributes(attributes).backend("pdf").toStream(out).get();
            asciidoctor.convert(asciidoc, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
