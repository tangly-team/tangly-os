/*
 * Copyright 2006-2022 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erpr.crm.ports;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import javax.inject.Inject;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.GroupRegistry;
import net.fortuna.ical4j.vcard.ParameterFactoryRegistry;
import net.fortuna.ical4j.vcard.PropertyFactoryRegistry;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.tangly.core.EmailAddress;
import net.tangly.core.Tag;
import net.tangly.core.crm.CrmTags;
import net.tangly.core.crm.VcardType;
import net.tangly.core.crm.NaturalEntity;
import net.tangly.core.crm.Photo;
import net.tangly.erp.crm.services.CrmRealm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Handler to import and export VCard representations of natural entities.
 */
public class CrmVcardHdl {
    private static final String VCARD_EXT = ".vcf";
    private static final String PHOTO_EXT = ".jpeg";
    private static final String PICTURES_FOLDER = "pictures";

    private static final Logger logger = LogManager.getLogger();
    private final CrmRealm realm;

    @Inject
    public CrmVcardHdl(@NotNull CrmRealm realm) {
        this.realm = realm;
    }

    /**
     * Import all vcard files located in the folder <i>directory</i>. The imported VCard are added as natural entities. If an organization is specified, an
     * employee connection is added under the condition the legal entity is found in the CRM.
     *
     * @param directory directory to the folder containing the VCard files to import
     */
    public void importVCards(@NotNull Path directory) {
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(path -> !Files.isDirectory(path) && path.getFileName().toString().endsWith(VCARD_EXT)).forEach(this::importVCard);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void importVCard(@NotNull Path path) {
        Path picturesFolder = path.getParent().getParent().resolve(PICTURES_FOLDER);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            var groupRegistry = new GroupRegistry();
            var propReg = new PropertyFactoryRegistry();
            var parReg = new ParameterFactoryRegistry();
            var builder = new VCardBuilder(reader, groupRegistry, propReg, parReg);
            var card = new VCard2(builder.build());
            Optional<EmailAddress> homeEmail = card.homeEmail();
            Optional<NaturalEntity> person = Optional.empty();
            if (homeEmail.isPresent()) {
                person = realm.naturalEntities().findBy(o -> o.email(VcardType.home).orElse(null), homeEmail.get());
            }
            person.ifPresent(o -> o.photo(this.photo(o, card, picturesFolder).orElse(null)));

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ParserException e) {
            logger.atError().withThrowable(e).log("Error when parsing vcf file {}", path);
        }
    }

    /**
     * The quality of the linkedin picture is higher rated than the one of the vcard. The stored picture will supersede the vcard picture if found.
     *
     * @param entity      natural entity which picture should be updated
     * @param card        VCard of the natural entity
     * @param picturesDir the folder containing linkedin pictures. The name of the picture is the linkedIn profile name
     * @return the best found picture if found otherwise an empty optional
     */
    private Optional<Photo> photo(@NotNull NaturalEntity entity, @NotNull VCard2 card, @NotNull Path picturesDir) {
        Optional<Photo> photo = card.photo();
        Optional<Tag> tag = entity.findBy(CrmTags.CRM_IM_LINKEDIN);
        if (tag.isPresent()) {
            Path path = picturesDir.resolve(tag.get().value() + PHOTO_EXT);
            if (Files.exists(path)) {
                try (var stream = Files.newInputStream(path)) {
                    photo = Optional.of(Photo.of(stream.readAllBytes()));
                } catch (IOException e) {
                    logger.atError().log("Could not read picture from {}", path);
                }
            }
        }
        return photo;
    }
}
