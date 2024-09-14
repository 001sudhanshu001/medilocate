package com.medilocate.service;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import com.medilocate.constants.JwtTokenProperty;
import com.medilocate.dto.request.RefreshTokenRequest;
import com.medilocate.entity.User;
import com.medilocate.entity.UserSession;
import com.medilocate.entity.enums.Role;
import com.medilocate.exception.custom.EmailDuplicateException;
import com.medilocate.repository.UserRepository;
import com.medilocate.repository.UserSessionDetailRepository;
import com.medilocate.security.UserDetailsImpl;
import com.medilocate.security.dto.JwtAuthenticationResponse;
import com.medilocate.security.dto.LogOutRequest;
import com.medilocate.security.dto.SignUpRequest;
import com.medilocate.security.dto.SigninRequest;
import com.medilocate.security.enums.TokenType;
import com.medilocate.security.exception.JwtSecurityException;
import com.medilocate.security.helper.SessionCreationHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.medilocate.security.exception.JwtSecurityException.JWTErrorCode.REFRESH_TOKEN_ONLY_ALLOWED_WITH_EXPIRED_TOKEN;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionDetailRepository userSessionDetailRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SessionCreationHelper sessionCreationHelper;

    @Transactional
    public JwtAuthenticationResponse signup(SignUpRequest signUpRequest, boolean createAdmin) {
        Optional<User> byEmail = userRepository.findByEmail(signUpRequest.getEmail());
        if(byEmail.isPresent()) {
            throw new EmailDuplicateException("This Email is Already taken");
        }

        User appUser = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .role(createAdmin ? Role.ADMIN : Role.PATIENT)
                .isEnabled(true) // TODO : Will Implement the email Verification
                .isVerified(true)
                .build();

        userRepository.save(appUser);

        Map<TokenType, String> tokenMappedByType = jwtService.generateBothToken(new UserDetailsImpl(appUser));

        saveLoginSession(tokenMappedByType, appUser);

        return JwtAuthenticationResponse.builder()
                .accessToken(tokenMappedByType.get(TokenType.ACCESS_TOKEN))
                .refreshToken(tokenMappedByType.get(TokenType.REFRESH_TOKEN))
                .build();
    }

    private void saveLoginSession(Map<TokenType, String> tokenMappedByType, User user) {
        String accessToken = tokenMappedByType.get(TokenType.ACCESS_TOKEN);
        String refreshToken = tokenMappedByType.get(TokenType.REFRESH_TOKEN);

        Date refreshTokenWillExpireAt = new Date(Ulid.from(refreshToken).getTime());

        UserSession sessionDetail = new UserSession();

        sessionDetail.setActiveRefreshToken(refreshToken);
        sessionDetail.setAppUser(user);
        sessionDetail.setRefreshTokenExpiryDate(refreshTokenWillExpireAt);
        sessionDetail.setActiveAccessToken(accessToken);

        Date date = new Date();
        sessionDetail.setCreatedDate(date);
        sessionDetail.setLastModifiedDate(date);

        userSessionDetailRepository.save(sessionDetail);
    }

    @Transactional
    public JwtAuthenticationResponse signin(SigninRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getUserName())
                .orElseThrow(() -> new JwtSecurityException(
                        JwtSecurityException.JWTErrorCode.USER_NOT_FOUND,
                        "Invalid email or password")
                );

        List<UserSession> logoutSessions = validateAndReturnLogoutSession(user);

        Map<TokenType, String> tokenMappedByType = jwtService.generateBothToken(new UserDetailsImpl(user));
        saveLoginSession(tokenMappedByType, user);

        return JwtAuthenticationResponse.builder()
                .accessToken(tokenMappedByType.get(TokenType.ACCESS_TOKEN))
                .refreshToken(tokenMappedByType.get(TokenType.REFRESH_TOKEN))
                .loggedOutSessions(logoutSessions)
                .userName(request.getUserName())
                .build();
    }

    private List<UserSession> validateAndReturnLogoutSession(User user) {
        List<UserSession> sessions = user.getUserSessions();
        if (sessions.isEmpty()) return List.of();

        int numberOfSessionsInDB = sessions.size();

        removeInActiveSessionFromDB(sessions);
        throwExceptionIfNewSessionNotAllowed(numberOfSessionsInDB);
        return removeSessionIfAllowed(numberOfSessionsInDB, sessions);
    }

    private void removeInActiveSessionFromDB(List<UserSession> sessions) {
        List<UserSession> inActiveUserSessions = sessions.stream()
                .filter(UserSession::hasRefreshDateCrossed)
                .filter(this::isAccessTokenExpired)
                .toList();

        if (inActiveUserSessions.isEmpty()) {
            return;
        }

        userSessionDetailRepository.deleteAll(inActiveUserSessions);

        sessions.removeAll(inActiveUserSessions);
    }

    private boolean isAccessTokenExpired(UserSession session) {
        String accessToken = session.getActiveAccessToken();
        Date tokenExpiryDate = jwtService.getTokenExpiryFromExpiredJWT(accessToken);
        return tokenExpiryDate.before(new Date());
    }

    private void throwExceptionIfNewSessionNotAllowed(int numberOfSessionsInDB) {
        if (!sessionCreationHelper.canCreateNewSession(numberOfSessionsInDB)) {
            throw new JwtSecurityException(
                    JwtSecurityException.JWTErrorCode.MAX_SESSION_REACHED,
                    "Session Not Allowed, You Have To Logout From Other Device First"
            );
        }
    }

    private List<UserSession> removeSessionIfAllowed(
            int numberOfSessionsInDB, List<UserSession> sessions) {
        Integer allowedSessionCount = JwtTokenProperty.ALLOWED_SESSION_COUNT;
        if (sessionCreationHelper.doWeNeedToRemoveOldSession(numberOfSessionsInDB)) {
            sessions.sort(Comparator.comparing(UserSession::getCreatedDate));

            int sessionListSizeShouldBeForCreatingNewOne = allowedSessionCount - 1;

            List<UserSession> deletableSessions = new ArrayList<>();
            while (!sessions.isEmpty() && sessions.size() != sessionListSizeShouldBeForCreatingNewOne) {
                deletableSessions.add(sessions.remove(0));
            }

            userSessionDetailRepository.deleteAllInBatch(deletableSessions);
            return deletableSessions;
        }

        return List.of();
    }

    @Transactional
    public String logout(LogOutRequest logOutRequest) {
        String accessToken = logOutRequest.getAccessToken();
        String refreshToken = logOutRequest.getRefreshToken();

        String userName = jwtService.getUserNameFromJWT(accessToken);

        User user = userRepository.findByEmail(userName).orElseThrow(() ->
                new JwtSecurityException(
                        JwtSecurityException.JWTErrorCode.USER_NOT_FOUND,
                        "User Not Found With UserName:: "
                )
        );

        List<UserSession> sessions = user.getUserSessions();

        Optional<UserSession> optionalUserSessionDetail = findInOldSessions(sessions, accessToken, refreshToken);

        if (optionalUserSessionDetail.isEmpty()) {
            throw new JwtSecurityException(
                    JwtSecurityException.JWTErrorCode.SESSION_NOT_FOUND,
                    "User Session Not Found"
            );
        }

        UserSession session = optionalUserSessionDetail.get();

        userSessionDetailRepository.delete(session);

        return userName;
    }

    private Optional<UserSession> findInOldSessions(List<UserSession> oldSessions,
                                                    String accessToken, String refreshToken) {

        return oldSessions.stream()
                .filter(userSessionDetail -> {
                    String activeAccessToken = userSessionDetail.getActiveAccessToken();
                    String activeRefreshToken = userSessionDetail.getActiveRefreshToken();

                    return StringUtils.equals(accessToken, activeAccessToken) &&
                            StringUtils.equals(refreshToken, activeRefreshToken);
                }).findFirst();
    }

    public String getAuthenticatedUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (authentication != null && authentication.isAuthenticated()) {
            userName = authentication.getName();
        }

        return userName;
    }

    @Transactional
    public JwtAuthenticationResponse refresh(RefreshTokenRequest refreshTokenRequest) {
        String accessToken = refreshTokenRequest.getAccessToken();
        throwExceptionIfAccessTokenIsNotExpired(accessToken);

        String refreshToken = refreshTokenRequest.getRefreshToken();
        validateRefreshTokenAsULID(refreshToken);

        String userName = jwtService.getUserNameFromJWT(accessToken);
        User user = userRepository.findByEmail(userName).orElseThrow(
                () -> new JwtSecurityException(
                        JwtSecurityException.JWTErrorCode.USER_NOT_FOUND,
                        "User Not Found"
                )
        );

        List<UserSession> userSessions = user.getUserSessions();

        Optional<UserSession> oldSession = findInOldSessions(
                userSessions, accessToken, refreshToken
        );

        if (oldSession.isEmpty()) {
            throw new JwtSecurityException(
                    JwtSecurityException.JWTErrorCode.SESSION_NOT_FOUND,
                    "User Session For Refresh Not Found With Given Tokens"
            );
        }

        UserSession sessionToUpdate = oldSession.get();
        Date refreshTokenExpiryDate = sessionToUpdate.getRefreshTokenExpiryDate();

        if(refreshTokenExpiryDate.before(new Date())) {
            throw new JwtSecurityException(
                    JwtSecurityException.JWTErrorCode.REFRESH_TOKEN_EXPIRED,
                    "Refresh Token Is Expired, Create New Login Request"
            );
        }

        String newAccessToken = jwtService.generateAccessToken(new UserDetailsImpl(user));
        Ulid newRefreshToken = UlidCreator.getUlid(refreshTokenExpiryDate.getTime());

        sessionToUpdate.setActiveAccessToken(newAccessToken);
        sessionToUpdate.setActiveRefreshToken(newRefreshToken.toString());
        sessionToUpdate.setLastModifiedDate(new Date());
        sessionToUpdate.increaseTokenRefreshCount();

        userSessionDetailRepository.save(sessionToUpdate);

        return JwtAuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.toString())
                .build();
    }

    private static void validateRefreshTokenAsULID(String refreshToken) {
        if (!Ulid.isValid(refreshToken)) {
            throw new JwtSecurityException(
                    JwtSecurityException.JWTErrorCode.INVALID_REFRESH_TOKEN,
                    "Invalid Refresh Token Provided"
            );
        }

        Date refreshTokenValidity = new Date(Ulid.from(refreshToken).getTime());
        if (refreshTokenValidity.before(new Date())) {
            throw new JwtSecurityException(
                    JwtSecurityException.JWTErrorCode.REFRESH_TOKEN_EXPIRED,
                    "Refresh Token Is Expired, Create New Login Request"
            );
        }
    }

    private void throwExceptionIfAccessTokenIsNotExpired(String accessToken) {
        Date accessTokenExpiredAt = jwtService.getTokenExpiryFromExpiredJWT(accessToken);
        if (accessTokenExpiredAt.after(new Date())) {
            throw new JwtSecurityException(
                    REFRESH_TOKEN_ONLY_ALLOWED_WITH_EXPIRED_TOKEN,
                    "Refreshing The Token Is Only Allowed When Access Token Is Expired"
            );
        }
    }

}
