package org.example.framework.security;

import java.util.concurrent.TimeUnit;

public class TokenLifetime {
    private TokenLifetime(){}

    public static final long time = TimeUnit.MINUTES.toMillis(5);
}
