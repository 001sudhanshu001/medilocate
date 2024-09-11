package com.medilocate.service;

import com.github.f4b6a3.ulid.Ulid;
import com.medilocate.constants.JwtTokenProperty;
import com.medilocate.entity.User;
import com.medilocate.entity.UserSession;
import com.medilocate.entity.enums.Role;
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
        User appUser = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .role(createAdmin ? Role.ADMIN : Role.PATIENT)
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
    public JwtAuthenticationResponse signin(SigninRequest request, Role role) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword())
        );

        User user = userRepository.findByEmailAndRole(request.getUserName(), role)
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
}
