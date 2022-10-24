package com.example.authservice.security.enums;

public enum UserRole {
    USER_ADMIN("ROLE_ADMIN"), USER_ROLE("ROLE_USER");
    private String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
