package org.example.framework.security;

public interface BasicAuthenticationProvider {
    Authentication basicAuth(String username, String password);
}
