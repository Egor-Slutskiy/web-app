package org.example.app.util;

import jakarta.servlet.http.HttpServletRequest;
import org.example.app.domain.User;
import org.example.framework.attribute.RequestAttributes;
import org.example.framework.security.Authentication;

import java.util.List;

public class UserHelper {
  private UserHelper() {
  }

  // TODO: beautify
  public static User getUser(HttpServletRequest req) {
    return ((User) ((Authentication) req.getAttribute(RequestAttributes.AUTH_ATTR)).getPrincipal());
  }

  public static boolean isAdmin(HttpServletRequest req){
    return ((List<?>)((Authentication)req.getAttribute(RequestAttributes.AUTH_ATTR)).getCredentials()).contains("ROLE_ADMIN");
  }
}
