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

package net.tangly.core.crm;


import net.tangly.core.*;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * A taxonomy of tags for a customer relationship management system. The namespace is naturally <i>crm</i>. Additional namespaces were added to introduce
 * standard tags such a location tags.
 */
public final class CrmTags {


    public static final String CRM = "crm";
    public static final String GEO = "geo";

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ALTITUDE = "altitude";
    public static final String PLUSCODE = "pluscode";

    private static final String BILLING = "billing";
    private static final String DELIVERY = "delivery";
    private static final String SEGMENT = "segment";

    public static final String CRM_EMAIL_HOME = CRM + ":email-" + VcardType.home.name();
    public static final String CRM_EMAIL_WORK = CRM + ":email-" + VcardType.work.name();

    public static final String CRM_PHONE_MOBILE = CRM + ":phone-" + VcardType.mobile.name();
    public static final String CRM_PHONE_HOME = CRM + ":phone-" + VcardType.home.name();
    public static final String CRM_PHONE_WORK = CRM + ":phone-" + VcardType.work.name();

    public static final String CRM_ADDRESS_HOME = CRM + ":address-" + VcardType.home.name();
    public static final String CRM_ADDRESS_WORK = CRM + ":address-" + VcardType.work.name();
    public static final String CRM_ADDRESS_BILLING = CRM + ":address-" + BILLING;
    public static final String CRM_ADDRESS_DELIVERY = CRM + ":address-" + DELIVERY;
    public static final String CRM_CUSTOMER_SEGMENT = CRM + SEGMENT;

    public static final String CRM_SITE_HOME = CRM + ":site-" + VcardType.home.name();
    public static final String CRM_SITE_WORK = CRM + ":site-" + VcardType.work.name();

    public static final String LINKEDIN = "linkedIn";
    public static final String SKYPE = "skype";
    public static final String GOOGLE = "google";

    public static final String CRM_IM_LINKEDIN = CRM + ":im-" + LINKEDIN;

    public static final String CRM_VAT_NUMBER = CRM + ":vat-number";
    public static final String CRM_BANK_CONNECTION = CRM + ":bank-connection";
    public static final String CRM_RESPONSIBLE = CRM + ":responsible";

    public static final String CRM_EMPLOYEE_TITLE = CRM + ":title";
    public static final String CRM_SCHOOL = CRM + ":school";

    public static final String GEO_LATITUDE = GEO + ":" + LATITUDE;
    public static final String GEO_LONGITUDE = GEO + ":" + LONGITUDE;
    public static final String GEO_PLUSCODE = GEO + ":" + PLUSCODE;

    /**
     * Private constructor of a utility class.
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

    public static void registerTags(@NotNull TypeRegistry registry) {
        registry.register(TagType.ofMandatory(CRM, "email-" + VcardType.home.name(), EmailAddress.class, EmailAddress::of));
        registry.register(TagType.ofMandatory(CRM, "email-" + VcardType.work.name(), EmailAddress.class, EmailAddress::of));
        registry.register(TagType.ofMandatory(CRM, "phone-" + VcardType.mobile.name(), PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "phone-" + VcardType.home.name(), PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "phone-" + VcardType.work.name(), PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "address-" + VcardType.home.name(), Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, "address-" + VcardType.work.name(), Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, "address-" + BILLING, Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, "address-" + DELIVERY, Address.class, Address::of));

        registry.register(TagType.ofMandatory(CRM, "site-" + VcardType.home.name(), URL.class, CrmTags::of));
        registry.register(TagType.ofMandatory(CRM, "site-" + VcardType.work.name(), URL.class, CrmTags::of));
        registry.register(TagType.ofMandatoryString(CRM, "title"));
        registry.register(TagType.ofMandatoryString(CRM, "im-" + LINKEDIN));
        registry.register(TagType.ofMandatoryString(CRM, "im-" + SKYPE));
        registry.register(TagType.ofMandatoryString(CRM, "im-" + GOOGLE));
        registry.register(TagType.ofMandatoryString(CRM, "vat-number"));
        registry.register(TagType.ofMandatory(CRM, "bank-connection", BankConnection.class, BankConnection::of));
        registry.register(TagType.ofMandatoryString(CRM, "responsible"));

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
     * Create the LinkedIn profile link for a natural entity
     *
     * @param entity person which LinkedIn profile should be displayed
     * @return link to the LinkedIn profile
     */
    public static String individualLinkedInUrl(@NotNull HasTags entity) {
        return entity.findBy(CrmTags.CRM_IM_LINKEDIN).map(Tag::value).map(o -> "https://www.linkedin.com/in/" + o).orElse(null);
    }

    /**
     * Create the LinkedIn company profile lik for a legal entity. LinkedIn handles regular organizations and schools in different ways.
     *
     * @param entity organization which LinkedIn profile should be displayed
     * @return link to the linkedIn profile
     */
    public static String organizationLinkedInUrl(@NotNull HasTags entity) {
        var school = entity.findBy(CrmTags.CRM_SCHOOL);
        return entity.findBy(CrmTags.CRM_IM_LINKEDIN).map(Tag::value).map(o -> "https://www.linkedin.com/" + (school.isPresent() ? "school/" : "company/") + o)
            .orElse(null);
    }
}
