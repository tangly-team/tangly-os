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

package net.tangly.core.domain;

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
import java.util.Optional;

/**
 * An application user can log in the application and access to domain data based on domain access rights.
 * A user is defined in the context of a tenant.
 *
 * @param username        username is the identifier of the user in the context of a tenant
 * @param passwordHash    password hash is the encrypted password of the user
 * @param active          true if the user is active and can log in the application
 * @param naturalPersonId optional identifier of the natural person associated with the user
 * @param accessRights    list of access rights for the user on the different domains of the application
 * @param gravatarEmail   email address used to retrieve the avatar of the user through the gravatar service
 */
public record User(@NotNull String username, @NotNull String passwordHash, @NotNull String passwordSalt, boolean active, String naturalPersonId,
                   @NotNull List<AccessRights> accessRights, String gravatarEmail) {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

    /**
     * Encrypts the password of a user using a salt.
     *
     * @param password password to encrypt
     * @param salt     salt used to encrypt the password
     * @return encrypted password
     */
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

    /**
     * Generates a new salt value with a JDK provided random generator.
     *
     * @return new salt value
     */
    public static String newSalt() {
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("missing random algorithm SHA1PRNG", e);
        }
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Returns the avatar of a user based on the email address of the user.
     *
     * @param gravatarEmail email address of the user registered in the gravatar service
     * @return avatar picture of the user
     */
    public static byte[] avatar(@NotNull String gravatarEmail) {
        var gravatar = new Gravatar();
        return gravatar.avatar(gravatarEmail, 200, GravatarRating.GENERAL_AUDIENCES, GravatarImage.GRAVATAR_ICON);
    }

    /**
     * Returns the access rights of a user for a specific domain.
     *
     * @param domain domain for which the access rights are requested
     * @return access rights of the user for the domain
     */
    public Optional<AccessRights> accessRightsFor(@NotNull String domain) {
        return accessRights.stream().filter(rights -> rights.domain().equals(domain)).findAny();
    }

    public boolean hasAdminRightsFor(@NotNull String domain) {
        return accessRightsFor(domain).map(o -> (o.right() == AccessRightsCode.domainAdmin) || (o.right() == AccessRightsCode.tenantAdmin)).orElse(false);
    }

    /**
     * Authenticates the user based on the password provided.
     *
     * @param password password to authenticate the user
     * @return true if the password is correct
     */
    public boolean authenticate(@NotNull String password) {
        return encryptPassword(password, passwordSalt()).equals(passwordHash());
    }
}
