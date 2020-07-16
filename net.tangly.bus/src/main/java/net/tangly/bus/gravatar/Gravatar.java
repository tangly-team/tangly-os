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

package net.tangly.bus.gravatar;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

/**
 * A gravatar is a dynamic image resource that is requested from the gravatar.com server. The class calculates the gravatar url and fetches gravatar images.
 * See
 * <a href="https://en.gravatar.com/site/implement/images/">Gravatar</a>.
 */
public final class Gravatar {
    private static final String GRAVATAR_URL = "https://www.gravatar.com/avatar/";

    public byte[] avatar(String email) {
        return avatar(email, 0, GravatarRating.GENERAL_AUDIENCES, GravatarImage.GRAVATAR_ICON);
    }

    public byte[] avatar(String email, int sizeInPixels, GravatarRating rating, GravatarImage image) {
        try {
            return IOUtils.toByteArray(new URL(getUrl(email, sizeInPixels, rating, image)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getUrl(String email, int sizeInPixels, GravatarRating rating, GravatarImage image) {
        return GRAVATAR_URL + DigestUtils.md5Hex(email.toLowerCase().trim()) + ".jpg" + formatUrlParameters(sizeInPixels, rating, image);
    }

    private String formatUrlParameters(int sizeInPixels, GravatarRating rating, GravatarImage image) {
        List<String> params = new ArrayList<>();
        if (sizeInPixels != 0) {
            params.add("s=" + sizeInPixels);
        }
        if (rating != GravatarRating.GENERAL_AUDIENCES) {
            params.add("r=" + rating.code());
        }
        if (image != GravatarImage.GRAVATAR_ICON) {
            params.add("d=" + image.code());
        }
        return params.isEmpty() ? "" : ("?" + String.join("&", params));
    }
}
