package org.example.app.repository;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.ResetCodeWithStatus;
import org.example.app.domain.User;
import org.example.app.domain.UserWithPassword;
import org.example.app.entity.UserEntity;
import org.example.framework.security.Roles;
import org.example.jdbc.JdbcTemplate;
import org.example.jdbc.RowMapper;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserRepository {
  private final JdbcTemplate jdbcTemplate;

  private final RowMapper<User> rowMapper = resultSet -> new User(
      resultSet.getLong("id"),
      resultSet.getString("username")
  );
  private final RowMapper<UserWithPassword> rowMapperWithPassword = resultSet -> new UserWithPassword(
      resultSet.getLong("id"),
      resultSet.getString("username"),
      resultSet.getString("password")
  );
  private final RowMapper<ResetCodeWithStatus> codeRowMapper = resultSet -> new ResetCodeWithStatus(
          resultSet.getLong("code"),
          resultSet.getString("status")
  );

  private final RowMapper<String> roleRowMapper = resultSet -> resultSet.getString("role");
  private final RowMapper<Timestamp> timestampRowMapper = resultSet -> resultSet.getTimestamp("created");

  public Optional<User> getByUsername(String username) {
    // language=PostgreSQL
    return jdbcTemplate.queryOne("SELECT id, username FROM users WHERE username = ?", rowMapper, username);
  }

  public Optional<UserWithPassword> getByUsernameWithPassword(EntityManager entityManager, EntityTransaction transaction, String username) {
    // em, emt - closeable
    return entityManager.createNamedQuery(UserEntity.FIND_BY_USERNAME, UserEntity.class)
        .setParameter("username", username)
        .setMaxResults(1)
        .getResultStream()
        .map(o -> new UserWithPassword(o.getId(), o.getUsername(), o.getPassword()))
        .findFirst();
    // language=PostgreSQL
    // return jdbcTemplate.queryOne("SELECT id, username, password FROM users WHERE username = ?", rowMapperWithPassword, username);
  }

  /**
   * saves user to db
   *
   * @param id       - user id, if 0 - insert, if not 0 - update
   * @param username
   * @param hash
   */
  // TODO: DuplicateKeyException <-
  public Optional<User> save(long id, String username, String hash) {
    // language=PostgreSQL
    return id == 0 ? jdbcTemplate.queryOne(
        """
            INSERT INTO users(username, password) VALUES (?, ?) RETURNING id, username
            """,
        rowMapper,
        username, hash
    ) : jdbcTemplate.queryOne(
        """
            UPDATE users SET username = ?, password = ? WHERE id = ? RETURNING id, username
            """,
        rowMapper,
        username, hash, id
    );
  }

  public Optional<Timestamp> tokenCreatedTime(String token){
    // language=PostgreSQL
    return jdbcTemplate.queryOne(
            """
                SELECT created FROM tokens
                WHERE token = ?
                """,
            timestampRowMapper,
            token
    );
  }

  public void deleteToken(String token){
    // language=PostgreSQL
    jdbcTemplate.update(
            """
                DELETE FROM tokens
                WHERE token = ?
                """,
            token
    );
  }

  public Optional<User> findByToken(String token) {
    if(token==null){
      return Optional.of(new User(-1L, "anonymous"));
    }
    // language=PostgreSQL
    return jdbcTemplate.queryOne(
        """
            SELECT u.id, u.username FROM tokens t
            JOIN users u ON t."userId" = u.id
            WHERE t.token = ?
            """,
        rowMapper,
        token
    );
  }

  public List<String> findRoleById(long userId) {
    if(userId == -1){
      return List.of(Roles.ROLE_ANONYMOUS);
    }
    // language=PostgreSQL
    return (jdbcTemplate.queryAll(
            """
            SELECT role from roles
            WHERE "userId" = ?
            """,
            roleRowMapper,
            userId
    ));
  }

  public void saveRole(long userId, String role){
    // language=PostgreSQL
    jdbcTemplate.update(
            """
                    INSERT INTO roles(role, "userId") VALUES (?, ?)
                    """,
            role, userId
    );
  };

  public void saveToken(long userId, String token) {
    // query - SELECT'ов (ResultSet)
    // update - ? int/long
    // language=PostgreSQL
    jdbcTemplate.update(
        """
            INSERT INTO tokens(token, "userId") VALUES (?, ?)
            """,
        token, userId
    );
  }

  public void saveResetCode(long code, long userId, String status){
    // language=PostgreSQL
    jdbcTemplate.update(
            """
                    INSERT INTO password_reset(code, "userId", status) VALUES (?, ?, ?)
                    """,
            code, userId, status
    );
  }

  public Optional<ResetCodeWithStatus> getResetCode(long userId){
    // language=PostgreSQL
    return jdbcTemplate.queryOne(
            """
                SELECT code, status FROM password_reset WHERE "userId" = ?
                """,
            codeRowMapper,
            userId
    );
  }

  public void saveNewPassword(String password, long userId){
    // language=PostgreSQL
    jdbcTemplate.update(
            """
                UPDATE users
                SET password = ?
                WHERE id = ?
                """,
            password,
            userId
    );
    // language=PostgreSQL
    jdbcTemplate.update(
            """
                DELETE FROM password_reset
                WHERE "userId" = ?
                """,
            userId
    );
  }
}
