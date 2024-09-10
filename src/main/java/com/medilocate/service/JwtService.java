package com.medilocate.service;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.medilocate.constants.JwtTokenProperty;
import com.medilocate.security.enums.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    private Key signatureKey;

    @PostConstruct
    private void init() {
        byte[] keyBytes = Decoders.BASE64.decode(JwtTokenProperty.SIGN_WITH_KEY);
        signatureKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Map<TokenType, String> generateBothToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        String authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        extraClaims.put(JwtTokenProperty.AUTHORITY_KEY, authorities);

        return generateBothToken(extraClaims, userDetails);
    }

    private Map<TokenType, String> generateBothToken(Map<String, Object> extraClaims,
                                                     UserDetails userDetails) {

        long currentTimeMillis = System.currentTimeMillis();
        String accessToken = getAccessToken(extraClaims, userDetails, currentTimeMillis);

        long refreshTokenExpirationInMilliSeconds = JwtTokenProperty.refreshTokenExpirationInMilliSeconds();
        Date refreshTokenWillExpireOn = new Date(currentTimeMillis + refreshTokenExpirationInMilliSeconds);
        Ulid refreshToken = UlidCreator.getUlid(refreshTokenWillExpireOn.getTime());

        return Map.of(
                TokenType.ACCESS_TOKEN, accessToken,
                TokenType.REFRESH_TOKEN, refreshToken.toString()
        );
    }

    private String getAccessToken(Map<String, Object> extraClaims, UserDetails userDetails,
                                  long currentTimeMillis) {
        long bearerTokenExpirationInMilliSeconds =
                JwtTokenProperty.bearerTokenExpirationInMilliSeconds();

        Date issuedAt = new Date(currentTimeMillis);
        Date willExpireOn = new Date(currentTimeMillis + bearerTokenExpirationInMilliSeconds);

        return Jwts.builder().setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuer(JwtTokenProperty.ISSUED_BY)
                .setIssuedAt(issuedAt)
                .setExpiration(willExpireOn)
                .signWith(signatureKey, JwtTokenProperty.SIGNATURE_ALGORITHM)
                .compact();
    }

    public Date getTokenExpiryFromExpiredJWT(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signatureKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration();
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            return claims.getExpiration();
        }
    }
}
