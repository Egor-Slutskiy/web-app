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
import org.example.framework.security.BasicAuthenticationProvider;
import org.example.framework.security.TokenAuthentication;
import org.postgresql.util.Base64;

import java.io.IOException;

public class BasicAuthenticationFilter extends HttpFilter {
    private AuthenticationProvider provider;
    private BasicAuthenticationProvider basicProvider;

    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        basicProvider = ((BasicAuthenticationProvider) getServletContext().getAttribute(ContextAttributes.AUTH_PROVIDER_ATTR));
        provider = ((AuthenticationProvider) getServletContext().getAttribute(ContextAttributes.AUTH_PROVIDER_ATTR));
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (!provider.authenticationIsRequired(req)) {
            super.doFilter(req, res, chain);
            return;
        }

        final var basicAuth = req.getHeader("Authorization");

        if(basicAuth == null || !basicAuth.contains("Basic")){
            super.doFilter(req, res, chain);
            return;
        }

        try {
            String userPassword = new String(Base64.decode(basicAuth.split(" ")[1]));
            final String username = userPassword.split(":")[0];
            final String password = userPassword.split(":")[1];
            final var authentication = basicProvider.basicAuth(username, password);
            req.setAttribute(RequestAttributes.AUTH_ATTR, authentication);
        } catch (AuthenticationException e) {
            res.sendError(401);
            return;
        }

        super.doFilter(req, res, chain);
    }
}
