package org.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransferRequestDto {
    private String fromCard;
    private String username;
    private String toCard;
    private long amount;
}