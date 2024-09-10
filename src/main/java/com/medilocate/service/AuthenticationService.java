package com.medilocate.service;

import com.github.f4b6a3.ulid.Ulid;
import com.medilocate.entity.User;
import com.medilocate.entity.UserSession;
import com.medilocate.entity.enums.Role;
import com.medilocate.repository.UserRepository;
import com.medilocate.repository.UserSessionDetailRepository;
import com.medilocate.security.UserDetailsImpl;
import com.medilocate.security.dto.JwtAuthenticationResponse;
import com.medilocate.security.dto.SignUpRequest;
import com.medilocate.security.enums.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionDetailRepository userSessionDetailRepository;
    private final JwtService jwtService;

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
}
