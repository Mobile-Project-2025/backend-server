package com.mobile.server.domain.auth.jwt;

public interface TokenProvider {
    String createAccessToken(Long userId, String role);
    String createRefreshToken(Long userId, String rotateId);
    Long parseUserId(String token);
    String parseRole(String token);
}