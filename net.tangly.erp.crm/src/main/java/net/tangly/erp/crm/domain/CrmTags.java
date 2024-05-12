/*
 * Copyright 2006-2024 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.erp.crm.domain;


import net.tangly.core.*;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * A taxonomy of tags for a customer relationship management system. The namespace is naturally <i>crm</i>. Additional namespaces were added to introduce standard tags such a
 * location tags.
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
    private static final String EMAIL = ":email-";
    private static final String PHONE = ":phone-";
    private static final String ADDRESS = ":address-";

    public static final String CRM_EMAIL_HOME = CRM + EMAIL + VcardType.home.name();
    public static final String CRM_EMAIL_WORK = CRM + EMAIL + VcardType.work.name();

    public static final String CRM_PHONE_MOBILE = CRM + PHONE + VcardType.mobile.name();
    public static final String CRM_PHONE_HOME = CRM + PHONE + VcardType.home.name();
    public static final String CRM_PHONE_WORK = CRM + PHONE + VcardType.work.name();

    public static final String CRM_ADDRESS_HOME = CRM + ADDRESS + VcardType.home.name();
    public static final String CRM_ADDRESS_WORK = CRM + ADDRESS + VcardType.work.name();
    public static final String CRM_ADDRESS_BILLING = CRM + ADDRESS + BILLING;
    public static final String CRM_ADDRESS_DELIVERY = CRM + ADDRESS + DELIVERY;
    public static final String CRM_CUSTOMER_SEGMENT = CRM + SEGMENT;

    public static final String CRM_SITE_HOME = STR."\{CRM}:site-\{VcardType.home.name()}";
    public static final String CRM_SITE_WORK = STR."\{CRM}:site-\{VcardType.work.name()}";

    public static final String LINKEDIN = "linkedIn";
    public static final String SKYPE = "skype";
    public static final String GOOGLE = "google";

    public static final String CRM_IM_LINKEDIN = STR."\{CRM}:im-\{LINKEDIN}";

    public static final String CRM_VAT_NUMBER = STR."\{CRM}:vat-number";
    public static final String CRM_BANK_CONNECTION = STR."\{CRM}:bank-connection";
    public static final String CRM_RESPONSIBLE = STR."\{CRM}:responsible";

    public static final String CRM_EMPLOYEE_TITLE = STR."\{CRM}:title";
    public static final String CRM_SCHOOL = STR."\{CRM}:school";

    public static final String GEO_LATITUDE = STR."\{GEO}:\{LATITUDE}";
    public static final String GEO_LONGITUDE = STR."\{GEO}:\{LONGITUDE}";
    public static final String GEO_PLUSCODE = STR."\{GEO}:\{PLUSCODE}";

    /**
     * Private constructor of a utility class.
     */
    private CrmTags() {
    }

    public static URL of(String url) {
        try {
            return new URI(url).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerTags(@NotNull TypeRegistry registry) {
        registry.register(TagType.ofMandatory(CRM, STR."email-\{VcardType.home.name()}", EmailAddress.class, EmailAddress::of));
        registry.register(TagType.ofMandatory(CRM, STR."email-\{VcardType.work.name()}", EmailAddress.class, EmailAddress::of));
        registry.register(TagType.ofMandatory(CRM, STR."phone-\{VcardType.mobile.name()}", PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, STR."phone-\{VcardType.home.name()}", PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, STR."phone-\{VcardType.work.name()}", PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, STR."address-\{VcardType.home.name()}", Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, STR."address-\{VcardType.work.name()}", Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, STR."address-\{BILLING}", Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, STR."address-\{DELIVERY}", Address.class, Address::of));

        registry.register(TagType.ofMandatory(CRM, STR."site-\{VcardType.home.name()}", URL.class, CrmTags::of));
        registry.register(TagType.ofMandatory(CRM, STR."site-\{VcardType.work.name()}", URL.class, CrmTags::of));
        registry.register(TagType.ofMandatoryString(CRM, "title"));
        registry.register(TagType.ofMandatoryString(CRM, STR."im-\{LINKEDIN}"));
        registry.register(TagType.ofMandatoryString(CRM, STR."im-\{SKYPE}"));
        registry.register(TagType.ofMandatoryString(CRM, STR."im-\{GOOGLE}"));
        registry.register(TagType.ofMandatoryString(CRM, "vat-number"));
        registry.register(TagType.ofMandatory(CRM, "bank-connection", BankConnection.class, BankConnection::of));
        registry.register(TagType.ofMandatoryString(CRM, "responsible"));

        registry.register(TagType.ofMandatory(GEO, LATITUDE, Double.TYPE, Double::valueOf));
        registry.register(TagType.ofMandatory(GEO, LONGITUDE, Double.TYPE, Double::valueOf));
        registry.register(TagType.ofMandatory(GEO, ALTITUDE, Double.TYPE, Double::valueOf));
        registry.register(TagType.ofMandatoryString(GEO, PLUSCODE));
    }

    public static String phoneTag(@NotNull String kind) {
        return STR."\{CRM}:phone-\{Objects.requireNonNull(kind)}";
    }

    public static String emailTag(@NotNull String kind) {
        return STR."\{CRM}:email-\{Objects.requireNonNull(kind)}";
    }

    public static String addressTag(@NotNull String kind) {
        return STR."\{CRM}:address-\{Objects.requireNonNull(kind)}";
    }

    public static String siteTag(@NotNull String kind) {
        return STR."\{CRM}:site-\{Objects.requireNonNull(kind)}";
    }

    public static String imTag(@NotNull String im) {
        return STR."\{CRM}:im-\{Objects.requireNonNull(im)}";
    }

    public static String linkedInTag(@NotNull HasTags entity) {
        return entity.findBy(CrmTags.CRM_IM_LINKEDIN).map(Tag::value).orElse(null);
    }
}
