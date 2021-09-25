package org.example.app.handler;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.example.app.domain.User;
import org.example.app.dto.LoginRequestDto;
import org.example.app.dto.RegistrationRequestDto;
import org.example.app.dto.ResetPassRequestDto;
import org.example.app.dto.ResetPassResponseDto;
import org.example.app.service.CardService;
import org.example.app.service.UserService;
import org.example.app.util.UserHelper;
import org.example.framework.attribute.RequestAttributes;

import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Matcher;

@Log
@RequiredArgsConstructor
public class UserHandler {
  private final UserService service;
  private final Gson gson;

  public void register(HttpServletRequest req, HttpServletResponse resp) {
    try {
      log.log(Level.INFO, "register");
      final var requestDto = gson.fromJson(req.getReader(), RegistrationRequestDto.class);
      final var responseDto = service.register(requestDto);
      resp.setHeader("Content-Type", "application/json");
      resp.getWriter().write(gson.toJson(responseDto));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void login(HttpServletRequest req, HttpServletResponse resp) {
    try {
      log.log(Level.INFO, "login");
      final var requestDto = gson.fromJson(req.getReader(), LoginRequestDto.class);
      final var responseDto = service.login(requestDto);
      resp.setHeader("Content-Type", "application/json");
      resp.getWriter().write(gson.toJson(responseDto));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void resetPassword(HttpServletRequest req, HttpServletResponse resp) {
    try{
      final var requestDto = gson.fromJson(req.getReader(), ResetPassRequestDto.class);
      ResetPassResponseDto responseDto;
      final var user = UserHelper.getUser(req);
      if(requestDto == null){
        responseDto = service.createResetCode(user.getId());
        resp.setHeader("Content-Type", "application/json");
        resp.getWriter().write(gson.toJson(responseDto));
        return;
      }
      if(!requestDto.getCode().isEmpty() & !requestDto.getPassword().isEmpty()){
        if(service.confirmResetCode(Long.parseLong(requestDto.getCode()), user.getId())){
          responseDto = service.resetPassword(requestDto.getPassword(), user.getId());
          resp.setHeader("Content-Type", "application/json");
          resp.getWriter().write(gson.toJson(responseDto));
        }
      }else {
        throw new RuntimeException("Wrong reset request");
      }
    } catch (IOException e){
      throw new RuntimeException(e);
    }
  }
}
