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

package net.tangly.crm.ports;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.GroupRegistry;
import net.fortuna.ical4j.vcard.ParameterFactoryRegistry;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.PropertyFactoryRegistry;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.property.Fn;
import net.fortuna.ical4j.vcard.property.N;
import net.fortuna.ical4j.vcard.property.SortString;
import net.fortuna.ical4j.vcard.property.Version;
import net.tangly.bus.core.EmailAddress;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.NaturalEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to import and export VCard representations of natural entities.
 */
public class CrmVcardHdl {
    private static final String VCARD_EXT = ".vcf";

    private static final Logger logger = LoggerFactory.getLogger(CrmVcardHdl.class);
    private final Crm crm;

    public CrmVcardHdl(@NotNull Crm crm) {
        this.crm = crm;
    }

    /**
     * Import all vcard files located in the folder <i>directory</i>. The imported VCard are added as naturl entities. If an organization is specified, an
     * employee connection is added under the condition the legal entity is found in the CRM.
     *
     * @param directory directory to the folder containing the VCard files to import
     */
    public void importVCards(@NotNull Path directory) {
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(file -> !Files.isDirectory(file) && file.getFileName().toString().endsWith(VCARD_EXT)).forEach(o -> importVCard(o));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void importVCard(@NotNull Path path) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            GroupRegistry groupRegistry = new GroupRegistry();
            PropertyFactoryRegistry propReg = new PropertyFactoryRegistry();
            ParameterFactoryRegistry parReg = new ParameterFactoryRegistry();
            VCardBuilder builder = new VCardBuilder(reader, groupRegistry, propReg, parReg);
            VCard card = builder.build();
            VCard2 card2 = new VCard2(card);
            Optional<EmailAddress> homeEmail = card2.homeEmail();
            Optional<NaturalEntity> person = Optional.empty();
            if (homeEmail.isPresent()) {
                person = crm.naturalEntities().findBy(o -> o.email(CrmTags.Type.home).orElse(null), homeEmail.get());
            }
            person.ifPresent(o -> o.photo(card2.photo()));

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ParserException e) {
            logger.atError().setCause(e).log("Error when parsing vcf file {}", path);
        }
    }

    public void enhance(@NotNull NaturalEntity entity, @NotNull VCard2 vcard) {
        // photography
        // website
        // home phone number
        // birthday
    }

    public VCard of(NaturalEntity entity) {
        List<Property> props = new ArrayList<Property>();
        props.add(Version.VERSION_4_0);
        props.add(new N(entity.lastname(), entity.firstname(), null, null, null));
        props.add(new Fn(entity.name()));
        props.add(new SortString(entity.lastname()));
        return null;
    }
}


