package com.example.authservice.security;

import com.example.authservice.entities.User;
import com.example.authservice.repo.UserRepository;
import com.example.authservice.utils.exceptions.AppExceptionConstants;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException(AppExceptionConstants.BAD_LOGIN_CREDENTIALS));

        if(!user.getActive()){
            throw new BadCredentialsException(AppExceptionConstants.USER_EMAIL_NOT_AVAILABLE);
        }

        return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRoles(), user.getActive());
    }
}