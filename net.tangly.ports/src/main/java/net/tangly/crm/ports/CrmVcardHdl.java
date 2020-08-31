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

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.Group;
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
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to import and export VCard representations of natural entities.
 */
public class CrmVcardHdl {
    private static final String EXTENSION = ".vcf";

    private static final Logger logger = LoggerFactory.getLogger(CrmVcardHdl.class);
    private final Crm crm;

    public CrmVcardHdl(@NotNull Crm crm) {
        this.crm = crm;
    }

    /**
     * Import all vcard files located in the folder path. The imported VCard are added as naturl entities. If an organization is specified, an employee
     * connection is added under the condition the legal entity is found in the CRM.
     *
     * @param path path to the folder containing the VCard files to import
     */
    public void importVCards(@NotNull Path path) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            GroupRegistry groupRegistry = new GroupRegistry();
            PropertyFactoryRegistry propReg = new PropertyFactoryRegistry();
            ParameterFactoryRegistry parReg = new ParameterFactoryRegistry();
            VCardBuilder builder = new VCardBuilder(reader, groupRegistry, propReg, parReg);
            VCard card = builder.build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ParserException e) {
            logger.atError().setCause(e).log("Error when parsing vcf file {}", path);
        }
    }

    /**
     * A natural entity is identified through a home or a work email address. This email address is the only universal unique identifier.
     *
     * @param card
     * @return
     */
    public NaturalEntity findOrCreatePerson(VCard card) {
        // either missing or "individual"
        String kind = card.getProperty(Property.Id.KIND).getValue();
        String fullname = card.getProperty(Property.Id.FN).getValue();
        String lastname = card.getProperty(Property.Id.N).getValue();
        String firstname = card.getProperty(Property.Id.FN).getValue();
        String birthday = card.getProperty(Property.Id.BDAY).getValue();
        String deathday = card.getProperty(Property.Id.DDAY).getValue();
        // either M, F, null
        String gender = card.getProperty(Property.Id.GENDER).getValue();
        // Potential same natural entity has same firstname and lastname

        List<Property> properties = card.getProperties(Property.Id.EMAIL);
        properties.get(0).getGroup().equals(Group.HOME);
        properties.get(0).getGroup().equals(Group.WORK);
        return null;
    }

    public LegalEntity findOrCreateOrganization(VCard card) {
        // "organization"
        String kind = card.getProperty(Property.Id.KIND).getValue();
        return null;
    }

    public Optional<LegalEntity> find(VCard vcard) {
        return Optional.empty();
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


