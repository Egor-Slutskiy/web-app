package org.example.demo;

import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TokenDemo {
  public static void main(String[] args) {
    final var keyGenerator = new Base64StringKeyGenerator(64);
    System.out.println(keyGenerator.generateKey());

    final var str = Base64.getEncoder().encode("test:msg".getBytes());
    System.out.println(new String(str, StandardCharsets.UTF_8));
    final var decoded = Base64.getDecoder().decode(str);
    System.out.println(new String(decoded, StandardCharsets.UTF_8));
  }
}
