package com.mobile.domain.auth.jwt;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.jsonwebtoken.Jwts;


@Component
public class JWTUtil {

    private final SecretKey secretKey;

    //AccessToken 유효기간(15분)
    private static final long ACCESS_TTL_MS = 15 * 60 * 1000;

    public JWTUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    //AccessToken 생성
    public String createAccessToken(Long userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .claim("userId",userId)
                .claim("role",role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ACCESS_TTL_MS))
                .signWith(secretKey)
                .compact();
    }

    //userId 파싱
    public Long parseUserId(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().get("userId", Number.class).longValue();
    }

    //role 파싱
    public String parseRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public boolean isExpired(String token) {
        try {
            Date exp = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token).getPayload().getExpiration();
            return exp.before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
}
