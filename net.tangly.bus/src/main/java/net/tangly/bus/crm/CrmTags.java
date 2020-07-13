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


import java.net.URL;
import java.util.Objects;

import net.tangly.bus.core.Address;
import net.tangly.bus.core.EmailAddress;
import net.tangly.bus.core.PhoneNr;
import net.tangly.bus.core.TagType;
import net.tangly.bus.core.TagTypeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * A taxonomy of tags for a customer relationship management system. The namespace is naturally <i>crm</i>.
 */
public final class CrmTags {
    public static final String CRM = "crm";
    public static final String GEO = "geo";

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ALTITUDE = "altitude";
    public static final String PLUSCODE = "pluscode";

    public static final String HOME = "home";
    public static final String WORK = "work";
    public static final String MOBILE = "mobile";

    public static final String CRM_EMAIL_HOME = "crm:email-home";
    public static final String CRM_EMAIL_WORK = "crm:email-work";

    public static final String CRM_PHONE_MOBILE = "crm:phone-mobile";
    public static final String CRM_PHONE_HOME = "crm:phone-home";
    public static final String CRM_PHONE_WORK = "crm:phone-work";

    public static final String CRM_ADDRESS_WORK = "crm:address-work";
    public static final String CRM_ADDRESS_HOME = "crm:address-home";

    public static final String CRM_SITE_HOME = "crm:site-home";
    public static final String CRM_SITE_WORK = "crm:site-work";

    public static final String CRM_IM_LINKEDIN = "crm:im-linkedin";
    public static final String CRM_IM_SKYPE = "crm:im-skype";
    public static final String CRM_IM_GOOGLE = "crm:im-google";

    public static final String CRM_COMPANY_ID = "crm:company-id";
    public static final String CRM_VAT_NUMBER = "crm:vat-number";
    public static final String CRM_BANK_CONNECTION = "crm:bank-connection";

    public static final String CRM_ACTIVITY = "crm:activity";

    /**
     * Private constructor of an utility class.
     */
    private CrmTags() {
    }

    public static void registerTags(@NotNull TagTypeRegistry registry) {
        registry.register(TagType.ofMandatory(CRM, "email-home", EmailAddress.class, EmailAddress::of));
        registry.register(TagType.ofMandatory(CRM, "email-work", EmailAddress.class, EmailAddress::of));
        registry.register(TagType.ofMandatory(CRM, "phone-mobile", PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "phone-home", PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "phone-work", PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "address-home", Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, "address-work", Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, "site-home", URL.class));
        registry.register(TagType.ofMandatory(CRM, "site-work", URL.class));
        registry.register(TagType.ofMandatory(CRM, "title", String.class));
        registry.register(TagType.ofMandatory(CRM, "im-linkedin", String.class));
        registry.register(TagType.ofMandatory(CRM, "im-skype", String.class));
        registry.register(TagType.ofMandatory(CRM, "im-google", String.class));
        registry.register(TagType.ofMandatory(CRM, "company-id", String.class));
        registry.register(TagType.ofMandatory(CRM, "vat-number", String.class));
        registry.register(TagType.ofMandatory(CRM, "bank-connection", BankConnection.class));
        registry.register(TagType.ofMandatory(CRM, "activity", String.class));

        registry.register(TagType.ofMandatory(GEO, LATITUDE, Double.TYPE));
        registry.register(TagType.ofMandatory(GEO, LONGITUDE, Double.TYPE));
        registry.register(TagType.ofMandatory(GEO, ALTITUDE, Double.TYPE));
        registry.register(TagType.ofMandatory(GEO, PLUSCODE, String.class));
    }

    public static String phoneTag(@NotNull String kind) {
        return "crm:phone-" + Objects.requireNonNull(kind);
    }

    public static String emailTag(@NotNull String kind) {
        return "crm:email-" + Objects.requireNonNull(kind);
    }

    public static String addressTag(@NotNull String kind) {
        return "crm:address-" + Objects.requireNonNull(kind);
    }

    public static String siteTag(@NotNull String kind) {
        return "crm:site-" + Objects.requireNonNull(kind);
    }

    public static String imTag(@NotNull String im) {
        return "crm:im-" + Objects.requireNonNull(im);
    }
}
