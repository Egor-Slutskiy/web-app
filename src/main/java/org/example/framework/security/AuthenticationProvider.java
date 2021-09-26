package org.example.framework.security;

import jakarta.servlet.http.HttpServletRequest;
import org.example.framework.attribute.RequestAttributes;

public interface AuthenticationProvider {
  Authentication authenticate(Authentication authentication) throws AuthenticationException;
  String updateToken(String token, long userId);

  default boolean authenticationIsRequired(HttpServletRequest req) {
    final var existingAuth = (Authentication) req.getAttribute(RequestAttributes.AUTH_ATTR);

    if (existingAuth == null || !existingAuth.isAuthenticated() || existingAuth.getCredentials().equals(Roles.ROLE_ANONYMOUS)) {
      return true;
    }

    return AnonymousAuthentication.class.isAssignableFrom(existingAuth.getClass());
  }
}
