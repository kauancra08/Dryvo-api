package br.com.kauangoncalves.drywall_api.jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {

        this.secretKey = new SecretKeySpec(
                secret.getBytes(),
                "HmacSHA256"
        );

        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(Long userId, String email) {

        Instant now = Instant.now();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(userId.toString())
                .claim("email", email)
                .issueTime(java.util.Date.from(now))
                .expirationTime(java.util.Date.from(expiration))
                .build();

        try {
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claims
            );

            signedJWT.sign(new MACSigner(secretKey.getEncoded()));

            return signedJWT.serialize();

        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar token JWT", e);
        }
    }
}