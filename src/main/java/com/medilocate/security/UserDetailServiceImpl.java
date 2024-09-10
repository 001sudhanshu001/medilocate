package com.medilocate.security;

import com.medilocate.entity.User;
import com.medilocate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User appUser = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User Not Found With UserName :: " + username)
        );
        return new UserDetailsImpl(appUser);
    }
}
