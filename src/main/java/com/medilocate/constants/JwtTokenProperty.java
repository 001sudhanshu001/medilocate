package com.medilocate.constants;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Validated
public class JwtTokenProperty {
    public static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

    public static final String SIGN_WITH_KEY = "LouTBHuneWvUFHrHRlwEbIJrrLOZH6SgynUSyUSv85Jynrpxu1qyn2DJh6upqLoubeUOtbrqRfT0dhurLkiy";

    public static final String ISSUED_BY = "sudhanshu.digital";

    public static final String AUTHORITY_KEY = "role";

    public static final Integer ALLOWED_SESSION_COUNT = 4;

    public static boolean AutoLogoutFromOtherDeviceOnOverflowSessionCount = true;

    private static Duration bearerTokenExpiration = Duration.of(30, ChronoUnit.MINUTES);

    private static Duration refreshTokenExpiration = Duration.of(30, ChronoUnit.MINUTES);

    public static long bearerTokenExpirationInMilliSeconds() {
        return bearerTokenExpiration.toMillis();
    }

    public static long refreshTokenExpirationInMilliSeconds() {
        return refreshTokenExpiration.toMillis();
    }

}
