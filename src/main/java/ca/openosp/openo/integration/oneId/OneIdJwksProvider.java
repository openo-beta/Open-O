/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.integration.oneId;

import ca.openosp.openo.commn.model.SystemPreferences.ONEID_KEYS;
import ca.openosp.openo.managers.EhrConnectivityManager;
import ca.openosp.openo.utility.MiscUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Locator;
import io.jsonwebtoken.ProtectedHeader;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import java.security.Key;
import java.security.PublicKey;

/**
 * Verifies an OpenID Connect id_token from Ontario Health: RS256 signature against the published
 * JWKS (key selected by the token's key id), then issuer, audience, expiry / not-before, and the
 * nonce echoed from login. Signing keys are fetched from the configured JWKS endpoint and cached
 * with a bounded time-to-live; a key-id miss triggers a single refresh to pick up key rotation.
 * Only keys from the JWKS endpoint are trusted - a key declared inside the token is never used.
 *
 * @since 2026-07-02
 */
@Service
public class OneIdJwksProvider {

    private static final Logger logger = MiscUtils.getLogger();

    private static final String RS256 = "RS256";
    private static final long CACHE_TTL_MILLIS = 60L * 60L * 1000L;
    private static final long FETCH_TIMEOUT_MILLIS = 10000L;
    private static final long CLOCK_SKEW_SECONDS = 60L;

    @Autowired
    private EhrConnectivityManager ehrConnectivityManager;

    private JwkSet cachedKeys;
    private long cachedAtMillis;

    /**
     * Verifies an id_token and returns its subject.
     *
     * @param idToken String the compact JWT id_token from the token response
     * @param expectedNonce String the nonce generated and stored at login
     * @return String the validated subject ("sub") claim
     * @throws IdTokenValidationException when any check fails (signature, issuer, audience, expiry,
     *         not-before, nonce, or missing configuration/subject)
     */
    public String verifyIdToken(String idToken, String expectedNonce) throws IdTokenValidationException {
        String jwksUri = requireConfig(ONEID_KEYS.endpoint_jwks, "JWKS endpoint");
        String issuer = requireConfig(ONEID_KEYS.oneid_issuer, "expected issuer");
        String audience = requireConfig(ONEID_KEYS.oag_client_id, "client id");

        Claims claims;
        try {
            Jws<Claims> jws = Jwts.parser()
                    .keyLocator(verificationKeyLocator(jwksUri))
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                    .build()
                    .parseSignedClaims(idToken);
            claims = jws.getPayload();
        } catch (RuntimeException e) {
            // Fail closed: any signature, claim, key, or fetch problem rejects the login.
            logger.warn("id_token verification failed (" + e.getClass().getSimpleName() + ")");
            throw new IdTokenValidationException("id_token verification failed", e);
        }

        String nonce = claims.get("nonce", String.class);
        if (expectedNonce == null || !expectedNonce.equals(nonce)) {
            throw new IdTokenValidationException("id_token nonce mismatch");
        }

        String subject = claims.getSubject();
        if (subject == null || subject.trim().isEmpty()) {
            throw new IdTokenValidationException("id_token has no subject");
        }
        return subject;
    }

    private Locator<Key> verificationKeyLocator(String jwksUri) {
        return header -> {
            if (!RS256.equals(header.getAlgorithm())) {
                return null;
            }
            String kid = (header instanceof ProtectedHeader) ? ((ProtectedHeader) header).getKeyId() : null;
            return publicKeyForKid(jwksUri, kid);
        };
    }

    private PublicKey publicKeyForKid(String jwksUri, String kid) {
        PublicKey key = selectKey(currentKeys(jwksUri, false), kid);
        if (key == null) {
            // A missing key id usually means the keys rotated; refresh once before giving up.
            key = selectKey(currentKeys(jwksUri, true), kid);
        }
        return key;
    }

    private static PublicKey selectKey(JwkSet keys, String kid) {
        if (keys == null) {
            return null;
        }
        if (kid == null || kid.trim().isEmpty()) {
            return soleKey(keys);
        }
        for (Jwk<?> jwk : keys.getKeys()) {
            if (kid.equals(jwk.getId())) {
                Key key = jwk.toKey();
                if (key instanceof PublicKey) {
                    return (PublicKey) key;
                }
            }
        }
        return null;
    }

    /**
     * The only public key in the set, or null when the set holds more than one.
     *
     * <p>A token may leave the key id out where the issuer publishes a single key, since there is
     * nothing to choose between. Where it publishes several, which is what a key rotation looks
     * like, choosing one would make verification depend on the order they happened to be listed in.
     *
     * @param keys JwkSet the published keys
     * @return PublicKey the single public key, or null when there is not exactly one
     */
    private static PublicKey soleKey(JwkSet keys) {
        PublicKey only = null;
        for (Jwk<?> jwk : keys.getKeys()) {
            Key key = jwk.toKey();
            if (key instanceof PublicKey) {
                if (only != null) {
                    logger.warn("id_token carries no key id and the JWKS publishes more than one");
                    return null;
                }
                only = (PublicKey) key;
            }
        }
        return only;
    }

    private synchronized JwkSet currentKeys(String jwksUri, boolean forceRefresh) {
        long now = System.currentTimeMillis();
        if (forceRefresh || cachedKeys == null || (now - cachedAtMillis) > CACHE_TTL_MILLIS) {
            cachedKeys = Jwks.setParser().build().parse(fetchJwks(jwksUri));
            cachedAtMillis = now;
        }
        return cachedKeys;
    }

    private String fetchJwks(String jwksUri) {
        WebClient wc = WebClient.create(jwksUri).accept(MediaType.APPLICATION_JSON);
        WebClient.getConfig(wc).getHttpConduit().getClient().setConnectionTimeout(FETCH_TIMEOUT_MILLIS);
        WebClient.getConfig(wc).getHttpConduit().getClient().setReceiveTimeout(FETCH_TIMEOUT_MILLIS);
        return wc.get(String.class);
    }

    private String requireConfig(ONEID_KEYS key, String label) throws IdTokenValidationException {
        String value = ehrConnectivityManager.getConfigValue(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IdTokenValidationException("missing " + label + " configuration");
        }
        return value.trim();
    }
}
