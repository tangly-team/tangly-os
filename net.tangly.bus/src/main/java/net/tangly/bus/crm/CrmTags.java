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

package net.tangly.bus.crm;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

import net.tangly.bus.core.Address;
import net.tangly.bus.core.BankConnection;
import net.tangly.bus.core.EmailAddress;
import net.tangly.bus.core.HasId;
import net.tangly.bus.core.HasTags;
import net.tangly.bus.core.QualifiedEntity;
import net.tangly.bus.core.PhoneNr;
import net.tangly.bus.core.Tag;
import net.tangly.bus.core.TagType;
import net.tangly.bus.core.TagTypeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * A taxonomy of tags for a customer relationship management system. The namespace is naturally <i>crm</i>. Additional namespaces were added to introduce
 * standard tags such a location tags.
 */
public final class CrmTags {
    public enum Type {
        home, work, mobile
    }

    public static final String CRM = "crm";
    public static final String GEO = "geo";

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ALTITUDE = "altitude";
    public static final String PLUSCODE = "pluscode";

    private static final String BILLING = "billing";
    private static final String DELIVERY = "delivery";
    private static final String SEGMENT = "segment";

    public static final String CRM_EMAIL_HOME = CRM + ":email-" + Type.home.name();
    public static final String CRM_EMAIL_WORK = CRM + ":email-" + Type.work.name();

    public static final String CRM_PHONE_MOBILE = CRM + ":phone-" + Type.mobile.name();
    public static final String CRM_PHONE_HOME = CRM + ":phone-" + Type.home.name();
    public static final String CRM_PHONE_WORK = CRM + ":phone-" + Type.work.name();

    public static final String CRM_ADDRESS_HOME = CRM + ":address-" + Type.home.name();
    public static final String CRM_ADDRESS_WORK = CRM + ":address-" + Type.work.name();
    public static final String CRM_ADDRESS_BILLING = CRM + ":address-" + BILLING;
    public static final String CRM_ADDRESS_DELIVERY = CRM + ":address-" + DELIVERY;
    public static final String CRM_CUSTOMER_SEGMENT = CRM + SEGMENT;

    public static final String CRM_SITE_HOME = CRM + ":site-" + Type.home.name();
    public static final String CRM_SITE_WORK = CRM + ":site-" + Type.work.name();

    public static final String LINKEDIN = "linkedIn";
    public static final String SKYPE = "skype";
    public static final String GOOGLE = "google";

    public static final String CRM_IM_LINKEDIN = CRM + ":im-" + LINKEDIN;

    public static final String CRM_VAT_NUMBER = CRM + ":vat-number";
    public static final String CRM_BANK_CONNECTION = CRM + ":bank-connection";

    public static final String CRM_EMPLOYEE_TITLE = CRM + ":title";
    public static final String CRM_SCHOOL = CRM + ":school";

    /**
     * Private constructor of an utility class.
     */
    private CrmTags() {
    }

    public static URL of(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerTags(@NotNull TagTypeRegistry registry) {
        registry.register(TagType.ofMandatory(CRM, "email-" + Type.home.name(), EmailAddress.class, EmailAddress::of));
        registry.register(TagType.ofMandatory(CRM, "email-" + Type.work.name(), EmailAddress.class, EmailAddress::of));
        registry.register(TagType.ofMandatory(CRM, "phone-" + Type.mobile.name(), PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "phone-" + Type.home.name(), PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "phone-" + Type.work.name(), PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "address-" + Type.home.name(), Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, "address-" + Type.work.name(), Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, "address-" + BILLING, Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, "address-" + DELIVERY, Address.class, Address::of));

        registry.register(TagType.ofMandatory(CRM, "site-" + Type.home.name(), URL.class, CrmTags::of));
        registry.register(TagType.ofMandatory(CRM, "site-" + Type.work.name(), URL.class, CrmTags::of));
        registry.register(TagType.ofMandatoryString(CRM, "title"));
        registry.register(TagType.ofMandatoryString(CRM, "im-" + LINKEDIN));
        registry.register(TagType.ofMandatoryString(CRM, "im-" + SKYPE));
        registry.register(TagType.ofMandatoryString(CRM, "im-" + GOOGLE));
        registry.register(TagType.ofMandatoryString(CRM, "company-id"));
        registry.register(TagType.ofMandatoryString(CRM, "vat-number"));
        registry.register(TagType.ofMandatory(CRM, "bank-connection", BankConnection.class, BankConnection::of));

        registry.register(TagType.ofMandatory(GEO, LATITUDE, Double.TYPE, Double::valueOf));
        registry.register(TagType.ofMandatory(GEO, LONGITUDE, Double.TYPE, Double::valueOf));
        registry.register(TagType.ofMandatory(GEO, ALTITUDE, Double.TYPE, Double::valueOf));
        registry.register(TagType.ofMandatoryString(GEO, PLUSCODE));
    }

    public static String phoneTag(@NotNull String kind) {
        return CRM + ":phone-" + Objects.requireNonNull(kind);
    }

    public static String emailTag(@NotNull String kind) {
        return CRM + ":email-" + Objects.requireNonNull(kind);
    }

    public static String addressTag(@NotNull String kind) {
        return CRM + ":address-" + Objects.requireNonNull(kind);
    }

    public static String siteTag(@NotNull String kind) {
        return CRM + ":site-" + Objects.requireNonNull(kind);
    }

    public static String imTag(@NotNull String im) {
        return CRM + ":im-" + Objects.requireNonNull(im);
    }

    /**
     * Create the linkedIn profile link for a natural entity
     *
     * @param entity person which linkedIn profile should be displayed
     * @return link to the linkedIn profile
     */
    public static String individualLinkedInUrl(@NotNull HasTags entity) {
        return entity.findBy(CrmTags.CRM_IM_LINKEDIN).map(Tag::value).map(o -> "https://www.linkedin.com/in/" + o).orElse(null);
    }

    /**
     * Create the linkedIn company profile lik for a legal entity. LinkedIn handles regular organizations and schools in different ways.
     *
     * @param entity organization which linkedIn profile should be displayed
     * @return link to the linkedIn profile
     */
    public static String organizationLinkedInUrl(@NotNull HasTags entity) {
        Optional<Tag> school = entity.findBy(CrmTags.CRM_SCHOOL);
        return entity.findBy(CrmTags.CRM_IM_LINKEDIN).map(Tag::value).map(o -> "https://www.linkedin.com/" + (school.isPresent() ? "school/" : "company/") + o)
                .orElse(null);
    }

    /**
     * Create a link to the Zefix page for the oorganization - makes only sense for Swiss companies -.
     *
     * @param entity organization which Zefix information should be displayed
     * @return link to Zefix information
     */
    public static String organizationZefixUrl(@NotNull LegalEntity entity) {
        return "https://www.zefix.ch/en/search/entity/list?name=" + entity.id() + "&searchType=exact";
    }

}
