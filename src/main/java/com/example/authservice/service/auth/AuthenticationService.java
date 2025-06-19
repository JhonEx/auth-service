package com.example.authservice.service.auth;

import com.example.authservice.service.auth.dtos.AuthResponseDTO;
import com.example.authservice.service.auth.dtos.LoginRequestDTO;
import com.example.authservice.service.auth.dtos.RegisterUserRequestDTO;
import com.example.authservice.service.auth.dtos.UserDTO;

import java.sql.SQLIntegrityConstraintViolationException;

public interface AuthenticationService {

    AuthResponseDTO loginUser(LoginRequestDTO loginRequest);

    /**
     * Additional login method that simply returns the JWT token.
     * This can be used by clients that only need a token to access
     * secured resources.
     */
    AuthResponseDTO loginForToken(LoginRequestDTO loginRequest);

    /**
     * Returns a fresh JWT token for the currently authenticated user.
     */
    AuthResponseDTO refreshToken();

    /**
     * Creates a new user in the system.
     */
    UserDTO createUser(RegisterUserRequestDTO registerUserRequestDTO) throws SQLIntegrityConstraintViolationException;

    /**
     * Returns all users in the system.
     */
    java.util.List<UserDTO> getAllUsers();

}
