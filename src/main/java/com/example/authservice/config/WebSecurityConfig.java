package com.example.authservice.config;

import com.example.authservice.security.CustomUserDetailsService;
import com.example.authservice.security.JwtTokenFilter;
import com.example.authservice.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Value("${app.jwt.token.secret-key}")
    private String secretKey;

    @Value("${app.jwt.token.expire-seconds}")
    private long validityInSeconds;

    private final CustomUserDetailsService customUserDetailsService;

    public WebSecurityConfig(final CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .headers().frameOptions().sameOrigin().and()    // for H2 console
                .csrf().disable()
                .httpBasic().and()
                .authorizeRequests(ar -> ar
                        // only allow unauthenticated access to login
                        .antMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        // still allow H2 console without auth
                        .antMatchers("/h2-console/**").permitAll()
                        // everything else requires a valid JWT
                        .anyRequest().authenticated()
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, rsp, ex) ->
                                rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage()))
                )
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(
                        new JwtTokenFilter(jwtTokenProvider()),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }


    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(secretKey, validityInSeconds);
        return jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}