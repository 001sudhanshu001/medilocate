package com.medilocate.security.helper;

import com.medilocate.constants.JwtTokenProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionCreationHelper {
    public boolean canCreateNewSession(int numberOfSessionInDb) {
        Integer allowedSessionCount = JwtTokenProperty.ALLOWED_SESSION_COUNT;
        boolean autoLogoutFromOtherDeviceOnOverflowSessionCount =
                JwtTokenProperty.AutoLogoutFromOtherDeviceOnOverflowSessionCount;

        boolean thereIsSlotForAnotherSession = numberOfSessionInDb < allowedSessionCount;
        return thereIsSlotForAnotherSession || autoLogoutFromOtherDeviceOnOverflowSessionCount;
    }

    public boolean doWeNeedToRemoveOldSession(int numberOfSessionInDB) {
        Integer allowedSessionCount = JwtTokenProperty.ALLOWED_SESSION_COUNT;
        boolean autoLogoutFromOtherDeviceOnOverflowSessionCount =
                JwtTokenProperty.AutoLogoutFromOtherDeviceOnOverflowSessionCount;
        return numberOfSessionInDB >= allowedSessionCount &&
                autoLogoutFromOtherDeviceOnOverflowSessionCount;
    }

}
