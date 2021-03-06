package org.example.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.example.app.domain.User;
import org.example.app.domain.UserWithPassword;
import org.example.app.dto.*;
import org.example.app.exception.PasswordNotMatchesException;
import org.example.app.exception.RegistrationException;
import org.example.app.exception.UserNotFoundException;
import org.example.app.jpa.JpaTransactionTemplate;
import org.example.app.repository.UserRepository;
import org.example.framework.security.*;
import org.example.framework.util.KeyValue;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Log
@RequiredArgsConstructor
public class UserService implements AuthenticationProvider, AnonymousProvider, BasicAuthenticationProvider {
  private final UserRepository repository;
  private final JpaTransactionTemplate transactionTemplate;
  private final PasswordEncoder passwordEncoder;
  private final StringKeyGenerator keyGenerator;

  @Override
  public Authentication authenticate(Authentication authentication) {
    final var token = (String) authentication.getPrincipal();

    final Optional<User> user = repository.findByToken(token);

    if(user.isPresent()){
      final Optional<Timestamp> optionalTimestamp = repository.tokenCreatedTime(token);
      if(optionalTimestamp.isPresent()){
        final Instant created = optionalTimestamp.get().toInstant();
        if(created.plus(TokenLifetime.time, ChronoUnit.MINUTES).toEpochMilli() < Instant.now().toEpochMilli()){
          repository.deleteToken(token);
          throw new AuthenticationException("Token expired");
        }
      }
    }

    final var role = repository.findRoleById(user.map(User::getId).orElse(-1L));

    return repository.findByToken(token)
            .map(o -> new TokenAuthentication(o, role, List.of(), true))
            .orElseThrow(AuthenticationException::new);
  }

  @Override
  public AnonymousAuthentication provide() {
    return new AnonymousAuthentication(new User(
        -1,
        "anonymous"
    ));
  }

  public RegistrationResponseDto register(RegistrationRequestDto requestDto) {
    // TODO login:
    //  case-sensitivity: coursar Coursar
    //  cleaning: "  Coursar   "
    //  allowed symbols: [A-Za-z0-9]{2,60}
    //  mis...: Admin, Support, root, ...
    //  ??????: ...
    // FIXME: check for nullability
    final var username = requestDto.getUsername().trim().toLowerCase();
    // TODO password:
    //  min-length: 8
    //  max-length: 64
    //  non-dictionary
    final var password = requestDto.getPassword().trim();
    final var hash = passwordEncoder.encode(password);
    final var token = keyGenerator.generateKey();
    final var saved = repository.save(0, username, hash).orElseThrow(RegistrationException::new);

    repository.saveToken(saved.getId(), token);
    repository.saveRole(saved.getId(), Roles.ROLE_USER);
    return new RegistrationResponseDto(saved.getId(), saved.getUsername(), token);
  }

  public LoginResponseDto login(LoginRequestDto requestDto) {
    final var username = requestDto.getUsername().trim().toLowerCase();
    final var password = requestDto.getPassword().trim();

    final var result = transactionTemplate.executeInTransaction((entityManager, transaction) -> {
      final var saved = repository.getByUsernameWithPassword(
          entityManager,
          transaction,
          username
      ).orElseThrow(UserNotFoundException::new);

      // TODO: be careful - slow
      if (!passwordEncoder.matches(password, saved.getPassword())) {
        // FIXME: Security issue
        throw new PasswordNotMatchesException();
      }

      final var token = keyGenerator.generateKey();
      repository.saveToken(saved.getId(), token);
      return new KeyValue<>(token, saved);
    });

    // FIXME: Security issue

    final var token = result.getKey();
    final var saved = result.getValue();
    return new LoginResponseDto(saved.getId(), saved.getUsername(), token);
  }

  public ResetPassResponseDto createResetCode(long userId){
    final long code = (long) ((Math.random() * (999999 - 100000)) + 100000);
    log.log(Level.INFO, "Confirm code: " + code);
    final String status = "UNCONFIRMED";
    repository.saveResetCode(code, userId, status);
    return new ResetPassResponseDto("CODE_CREATED", "Send your secure code and new password");
  }

  public boolean confirmResetCode(long code, long userId){
    final var dbCode = repository.getResetCode(userId);
    return dbCode.isPresent() && dbCode.get().getCode() == code & dbCode.get().getStatus().equals("UNCONFIRMED");
  }

  public ResetPassResponseDto resetPassword(String password, long userId){
    final var hash = passwordEncoder.encode(password);
    repository.saveNewPassword(hash, userId);
    return new ResetPassResponseDto("OK", "Your password have been updated");
  }

  @Override
  public String updateToken(String token, long userid){
    final var newToken = keyGenerator.generateKey();
    repository.deleteToken(token);
    repository.saveToken(userid, newToken);
    return newToken;
  }

  // FIXME:
  //  ?????????? ?????????? ?????????????????? ???? ???? ???????????????????????? ??????????????
  @Override
  public Authentication basicAuth(String username, String password) {
    final var loginResponseDto = login(new LoginRequestDto(username, password));
    return authenticate(new TokenAuthentication(loginResponseDto.getToken(), null));
  }
}
