package com.example.authservice.service.auth;

import com.example.authservice.entities.Phone;
import com.example.authservice.entities.User;
import com.example.authservice.repo.UserRepository;
import com.example.authservice.security.JwtTokenProvider;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordValidator passwordValidator;

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
            throw new BadCredentialsException(AppExceptionConstants.BAD_LOGIN_CREDENTIALS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
