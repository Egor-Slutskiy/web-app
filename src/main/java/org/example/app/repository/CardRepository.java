package org.example.app.repository;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.C;
import org.example.app.domain.Card;
import org.example.jdbc.JdbcTemplate;
import org.example.jdbc.RowMapper;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CardRepository {
  private final JdbcTemplate jdbcTemplate;
  private final RowMapper<Card> cardRowMapper = resultSet -> new Card(
      resultSet.getLong("id"),
      resultSet.getString("number"),
      resultSet.getLong("balance")
  );
  private final RowMapper<Card> cardWithOwnerRowMapper = resultSet -> new Card(
          resultSet.getLong("id"),
          resultSet.getString("number"),
          resultSet.getLong("balance"),
          resultSet.getLong("ownerId")
  );

  public List<Card> getAllByOwnerId(long ownerId) {
    // language=PostgreSQL
    return jdbcTemplate.queryAll(
        "SELECT id, number, balance FROM cards WHERE \"ownerId\" = ? AND active = TRUE",
        cardRowMapper,
        ownerId
    );
  }

  public Optional<Card> getOneByNumber(String number){
    //  language=PostgreSQL
    return jdbcTemplate.queryOne(
            "SELECT id, number, balance, \"ownerId\" FROM cards WHERE number = ? AND active = TRUE",
            cardWithOwnerRowMapper,
            number
    );
  }

  public List<Card> getAll(){
    return jdbcTemplate.queryAll(
            "SELECT id, number, balance FROM cards WHERE active = TRUE",
            cardRowMapper
    );
  }

  public void transfer(long fromCardId, long toCardId, long amount){
    // language=PostgreSQL
    jdbcTemplate.update("UPDATE cards SET balance = balance - ? WHERE id = ?", amount, fromCardId);
    jdbcTemplate.update("UPDATE cards SET balance = balance + ? WHERE id = ?", amount, toCardId);
  }

  public void blockById(long cardId){
    jdbcTemplate.update("UPDATE cards SET active = false WHERE id = ?", cardId);
  }

  public void order(long userId, long cardNumber){
    jdbcTemplate.update("INSERT INTO cards(\"ownerId\", number, balance)\n" +
            "VALUES (?, '**** *?', 0),\n", userId, cardNumber);
  }
}
