package com.example.authservice.bootstrap;

import com.example.authservice.entities.Phone;
import com.example.authservice.entities.User;
import com.example.authservice.repo.UserRepository;
import com.example.authservice.security.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class Bootstrap implements ApplicationListener<ContextRefreshedEvent> {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Bootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.error("Loading Bootstrap...");
        if(userRepository.count() == 0){
            loadData();
        }
    }

    private void loadData(){

        List<Phone> phones = new ArrayList<>();
        Phone phone1 = new Phone();
        phone1.setCountryCode("+57");
        phone1.setCityCode("1");
        phone1.setNumber("55699825");
        phones.add(phone1);

        User userTest = new User();
        userTest.setUsername("test@test.com");
        userTest.setName("Test User");
        userTest.setPassword(passwordEncoder.encode("password"));
        userTest.setRoles(Set.of(UserRole.USER_ROLE));
        userTest.setPhone(phones);
        phone1.setUser(userTest);

        userRepository.save(userTest);
    }
}
