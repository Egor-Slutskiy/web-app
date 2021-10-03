package org.example.app.handler;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.example.app.domain.Card;
import org.example.app.domain.User;
import org.example.app.dto.RegistrationRequestDto;
import org.example.app.dto.TransferRequestDto;
import org.example.app.service.CardService;
import org.example.app.util.UserHelper;
import org.example.framework.attribute.RequestAttributes;
import org.example.framework.security.Authentication;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;

@Log
@RequiredArgsConstructor
public class CardHandler { // Servlet -> Controller -> Service (domain) -> domain
  private final CardService service;
  private final Gson gson;

  public void getAll(HttpServletRequest req, HttpServletResponse resp) {
    try {
      final var user = UserHelper.getUser(req);
      List<Card> data;
      if(UserHelper.isAdmin(req)){
        data = service.getAll();
      }
      // cards.getAll?ownerId=1
      else {
        data = service.getAllByOwnerId(user.getId());
      }
      resp.setHeader("Content-Type", "application/json");
      resp.getWriter().write(gson.toJson(data));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void getById(HttpServletRequest req, HttpServletResponse resp) {
    final var cardId = Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR)).group("cardId"));
    log.log(Level.INFO, "getById");
  }

  public void transfer(HttpServletRequest req, HttpServletResponse resp){
    try{
      final var user = UserHelper.getUser(req);
      final var requestDto = gson.fromJson(req.getReader(), TransferRequestDto.class);
      final var responseDto = service.transfer(requestDto, user);
      resp.setHeader("Content-Type", "application/json");
      resp.getWriter().write(gson.toJson(responseDto));
    } catch (IOException e){
      throw new RuntimeException(e);
    }
  }

  public void order(HttpServletRequest req, HttpServletResponse resp) {
    final User user = UserHelper.getUser(req);
    service.order(user.getId());
    resp.setHeader("Content-Type", "application/json");
    try {
      resp.getWriter().write("{\"status\": \"ok\"}");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void blockById(HttpServletRequest req, HttpServletResponse resp) {
    final var cardId = Long.parseLong(((Matcher) req.getAttribute(RequestAttributes.PATH_MATCHER_ATTR)).group("cardId"));
    service.blockById(cardId);
    resp.setHeader("Content-Type", "application/json");
    try {
      resp.getWriter().write("{\"status\": \"ok\"}");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
