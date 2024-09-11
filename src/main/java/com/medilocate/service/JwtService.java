package com.medilocate.service;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.medilocate.constants.JwtTokenProperty;
import com.medilocate.security.enums.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
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

    public String getUserNameFromJWT(String token) {
        try{
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signatureKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch(ExpiredJwtException e) {
            Claims claims = e.getClaims();
            return claims.getSubject();
        }
    }

    public UsernamePasswordAuthenticationToken createAuthentication(String token) {
        Claims claims = extractAllClaims(token);

        String scopesString = claims.get(JwtTokenProperty.AUTHORITY_KEY).toString();
        String[] authStrings = scopesString.split(",");

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(authStrings)
                .map(SimpleGrantedAuthority::new)
                .toList();

        String subject = claims.getSubject();
        User principal = new User(subject, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    private Claims extractAllClaims(String token) throws ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, SecurityException, IllegalArgumentException {
        return Jwts.parserBuilder()
                .setSigningKey(signatureKey).build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isValidToken(String token) {
        boolean validationResult = false;
        try {
            Jwts.parserBuilder().setSigningKey(signatureKey).build().parse(token);
            validationResult = true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}. Token was: {}", e.getMessage(), token);
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT: {}. Token was: {}", e.getMessage(), token);
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}. Token is: {}", e.getMessage(), token);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT exception: {}. Token was: {}", e.getMessage(), token);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty:: {}", e.getMessage());
        }
        return validationResult;
    }
}
