package org.example.app.service;

import lombok.RequiredArgsConstructor;
import org.example.app.domain.Card;
import org.example.app.domain.User;
import org.example.app.dto.TransferRequestDto;
import org.example.app.dto.TransferResponseDto;
import org.example.app.repository.CardRepository;

import java.util.List;

@RequiredArgsConstructor
public class CardService {
  private final CardRepository cardRepository;

  public List<Card> getAllByOwnerId(long ownerId) {
    return cardRepository.getAllByOwnerId(ownerId);
  }

  public List<Card> getAll(){ return cardRepository.getAll(); }

  public void blockById(long cardId){cardRepository.blockById(cardId);}

  public TransferResponseDto transfer(TransferRequestDto requestDto, User owner){
    //TODO:
    //  Получать карту не по номеру, а по номеру и OwnerID (?)
    final var toCard = cardRepository.getOneByNumber(requestDto.getToCard());
    final var fromCard = cardRepository.getOneByNumber(requestDto.getFromCard());
    final var amount = requestDto.getAmount();
    if(toCard.isPresent() & fromCard.isPresent()){
      if(fromCard.get().getBalance()>=amount & fromCard.get().getOwnerId() == owner.getId()){
        cardRepository.transfer(fromCard.get().getId(), toCard.get().getId(), amount);
        return new TransferResponseDto("ok", "transfer complete");
      }
      return new TransferResponseDto("fail", "Карта не ваша или недостаточно средств");
    }
    return new TransferResponseDto("fail", "Не верный номер карты");
  }

  public void order(long userId){

    final long cardNumber = (long) ((Math.random() * (999 - 100)) + 100);
    cardRepository.order(userId, cardNumber);
  }
}
