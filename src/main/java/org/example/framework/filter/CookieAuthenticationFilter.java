package org.example.framework.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.app.domain.User;
import org.example.framework.attribute.ContextAttributes;
import org.example.framework.attribute.RequestAttributes;
import org.example.framework.security.AuthenticationException;
import org.example.framework.security.AuthenticationProvider;
import org.example.framework.security.TokenAuthentication;

import java.io.IOException;

public class CookieAuthenticationFilter extends HttpFilter {
    private AuthenticationProvider provider;

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        provider = ((AuthenticationProvider) getServletContext().getAttribute(ContextAttributes.AUTH_PROVIDER_ATTR));
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (!provider.authenticationIsRequired(req)) {
            super.doFilter(req, res, chain);
            return;
        }

        final var token = req.getHeader("Cookie");
        if (token == null) {
            super.doFilter(req, res, chain);
            return;
        }

        try {
            final var authentication = provider.authenticate(new TokenAuthentication(token, null));
            req.setAttribute(RequestAttributes.AUTH_ATTR, authentication);
            final var newToken = provider.updateToken(token, ((User) authentication.getPrincipal()).getId());
            res.setHeader("Set-Cookie", newToken);
        } catch (AuthenticationException e) {
            res.sendError(401);
            return;
        }

        super.doFilter(req, res, chain);
    }
}
