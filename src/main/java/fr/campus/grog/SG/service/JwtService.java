package fr.campus.grog.SG.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        // =========================================================================
        // Load and parse the RSA Public Key (Used for VERIFYING tokens)
        // =========================================================================
        // Read the raw public key PEM file content from the classpath resource
        // Standard JVM factory for RSA cryptographic operations
        String publicKeyContent = new String(this.publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        // Strip the public key PEM banners and all formatting whitespace
        String cleanPublicKey = publicKeyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        // Decode the Base64 text into raw binary bytes
        byte[] publicKeyBytes = Base64.getDecoder().decode(cleanPublicKey);
        // X.509 is the standard international format for public keys.
        // Convert the specification into a concrete java.security.PublicKey instance.
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }

    /**
     * Extracts the payload from a signed JWT after having verified the token
     *
     * @param token Compact serialized JWT string
     * @return The payload embedded in the token's claim
     */
    public Claims extractAllClaims(String token){
        // Parse and verify the signed JWT using our RSA public key
        Claims claims = Jwts.parser()
                // Verify the cryptographic signature using our public key
                .verifyWith(this.publicKey)
                .build()
                // Parses the compact token and validates its signature
                .parseSignedClaims(token)
                // Retrieve the payload containing all claims
                .getPayload();
        return claims;
    }

    /**
     * Extracts the username (subject claim) from a signed JWT.
     *
     * @param token Compact serialized JWT string
     * @return The username embedded in the payload's subject claim
     */
    public String extractUsername(String token) {
        Claims claims = this.extractAllClaims(token);
        // Extract the RFC 7519 'sub' (subject) claim representing the username
        return claims.getSubject();
    }

    /**
     * Extracts the roles from a signed JWT.
     *
     * @param token Compact serialized JWT string
     * @return The roles embedded in the roles' subject claim
     */
    public List<String> extractRoles(String token) {
        Claims claims = this.extractAllClaims(token);
        // Extract the roles, as this is not a predefined methode we have to do like that
        return claims.get("roles", List.class);
    }

    /**
     * Extracts the userId from a signed JWT.
     *
     * @param token Compact serialized JWT string
     * @return The userId embedded in the roles' subject claim
     */
    public UUID extractUserId(String token) {
        Claims claims = this.extractAllClaims(token);
        // Extract the roles, as this is not a predefined methode we have to do like that
        String userIdStr = claims.get("userId", String.class);
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    /**
     * Validates whether a token is authentic, untampered, and currently active.
     *
     * @param token Compact serialized JWT string
     * @return true if signature is valid and token is not expired, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            // JJWT automatically validates the signature AND checks if expiration < now.
            // If the token is expired, forged, or malformed, it throws a JwtException.
            Jwts.parser()
                    .verifyWith(this.publicKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid, expired, corrupted, or null
            return false;
        }
    }

}
