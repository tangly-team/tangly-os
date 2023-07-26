/*
 * Copyright 2006-2023 Marcel Baumann
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

package net.tangly.app.domain.model;

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
import java.util.Objects;

/**
 * The abstraction defines a registered subject entitled to use the application and associated services. The id of the subject is the human-readable unique identifier.
 */
public class Subject {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private final long naturalEntityId;
    private final String username;

    private String gravatarEmail;
    private String passwordSalt;
    private String passwordHash;
    private transient byte[] avatar;

    public Subject(String username, long naturalEntityId) {
        this.username = username;
        this.naturalEntityId = naturalEntityId;
    }

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

    public long naturalEntityId() {
        return naturalEntityId;
    }

    public String username() {
        return username;
    }

    public String gravatarEmail() {
        return gravatarEmail;
    }

    public void gravatarEmail(String gravatarEmail) {
        this.gravatarEmail = gravatarEmail;
    }

    public String passwordSalt() {
        return passwordSalt;
    }

    public void passwordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public void passwordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void newPassword(String password) {
        passwordSalt(Subject.newSalt());
        passwordHash(Subject.encryptPassword(password, passwordSalt()));

    }

    public boolean authenticate(@NotNull String password) {
        return encryptPassword(password, passwordSalt()).equals(passwordHash());
    }

    public byte[] avatar() {
        if (avatar == null) {
            var gravatar = new Gravatar();
            avatar = gravatar.avatar(gravatarEmail, 200, GravatarRating.GENERAL_AUDIENCES, GravatarImage.GRAVATAR_ICON);
        }
        return avatar;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Subject subject) && (Objects.equals(naturalEntityId(), subject.naturalEntityId()) && Objects.equals(username(), subject.username()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(naturalEntityId, username);
    }
}
