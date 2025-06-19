package com.example.authservice.service.auth;

import com.example.authservice.entities.Phone;
import com.example.authservice.entities.User;
import com.example.authservice.repo.UserRepository;
import com.example.authservice.security.JwtTokenProvider;
import com.example.authservice.security.CustomUserDetails;
import com.example.authservice.security.enums.UserRole;
import com.example.authservice.service.auth.dtos.AuthResponseDTO;
import com.example.authservice.service.auth.dtos.LoginRequestDTO;
import com.example.authservice.service.auth.dtos.RegisterUserRequestDTO;
import com.example.authservice.service.auth.dtos.UserDTO;
import com.example.authservice.utils.PasswordValidator;
import com.example.authservice.utils.exceptions.AppExceptionConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDTO loginUser(LoginRequestDTO loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            String token = jwtTokenProvider.createToken(authentication);
            AuthResponseDTO authResponseDTO = new AuthResponseDTO();
            authResponseDTO.setToken(token);

            userRecord(loginRequest.getUsername(), token);

            return authResponseDTO;
        } catch (AuthenticationException e) {
            throw new BadCredentialsException(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthResponseDTO loginForToken(LoginRequestDTO loginRequest) {
        // same implementation as loginUser but without modifying the user record
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            String token = jwtTokenProvider.createToken(authentication);
            AuthResponseDTO authResponseDTO = new AuthResponseDTO();
            authResponseDTO.setToken(token);
            return authResponseDTO;
        } catch (AuthenticationException e) {
            throw new BadCredentialsException(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthResponseDTO refreshToken() {
        // fetch authenticated user from security context
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException(AppExceptionConstants.UNAUTHORIZED_ACCESS);
        }
        try {
            String token = jwtTokenProvider.createToken(authentication);
            AuthResponseDTO responseDTO = new AuthResponseDTO();
            responseDTO.setToken(token);

            // update record with new token if possible
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
                userRecord(details.getUsername(), token);
            }
            return responseDTO;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public java.util.List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : users) {
            userDTOList.add(mapUserToUserDTO(user));
        }
        return userDTOList;
    }

    public void userRecord(String email, String token){
        Optional<User> userFound = userRepository.findByUsername(email);
        if(userFound.isPresent()){
            userFound.get().setToken(token);
            userFound.get().setLastLogin(new Date());

            userRepository.save(userFound.get());
        }

    }

    @Override
    public UserDTO createUser(RegisterUserRequestDTO registerUserRequestDTO) {
        if(!PasswordValidator.isValid(registerUserRequestDTO.getPassword())){
            throw new BadCredentialsException(AppExceptionConstants.PASSWORD_POLICY);
        }
        List<Phone> phoneList = new ArrayList<>();

        User user = new User();
        user.setUsername(registerUserRequestDTO.getEmail());
        user.setName(registerUserRequestDTO.getFullName());

        user.setPassword(passwordEncoder.encode(registerUserRequestDTO.getPassword()));
        user.setRoles(Set.of(UserRole.USER_ROLE));

        if(!registerUserRequestDTO.getPhones().isEmpty()){
            registerUserRequestDTO.getPhones().forEach(phone ->{
                phone.setUser(user);
                phoneList.add(phone);
            });
        }
        user.setPhone(phoneList);
        User returnedUser = userRepository.save(user);

        return mapUserToUserDTO(returnedUser);

    }

    private UserDTO mapUserToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getUsername());
        userDTO.setPassword(user.getPassword());
        userDTO.setPhones(user.getPhone());

        return userDTO;
    }
}
