/*
 * Copyright 2024 Marcel Baumann
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

package net.tangly.app.domain;

import net.tangly.core.gravatar.Gravatar;
import net.tangly.core.gravatar.GravatarImage;
import net.tangly.core.gravatar.GravatarRating;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.List;

/**
 * An application user can log in the application and access to domain data based on domain access rights.
 *
 * @param username
 * @param passwordHash
 * @param active
 * @param naturalPersonId
 * @param accessRights
 */
public record User(@NotNull String username, @NotNull String passwordHash, @NotNull String passwordSalt, boolean active, String naturalPersonId,
                   @NotNull List<UserAccessRights> accessRights, String gravatarEmail) {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

    public static String encryptPassword(@NotNull String password, @NotNull String salt) {
        int derivedKeyLength = 160;
        int iterations = 20_000;
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, iterations, derivedKeyLength);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] encBytes = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(encBytes);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String newSalt() {
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("mssing random algorithm SHA1PRNG", e);
        }
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static byte[] avatar(@NotNull String gravatarEmail) {
        var gravatar = new Gravatar();
        return gravatar.avatar(gravatarEmail, 200, GravatarRating.GENERAL_AUDIENCES, GravatarImage.GRAVATAR_ICON);
    }

    public boolean authenticate(@NotNull String password) {
        return encryptPassword(password, passwordSalt()).equals(passwordHash());
    }
}
