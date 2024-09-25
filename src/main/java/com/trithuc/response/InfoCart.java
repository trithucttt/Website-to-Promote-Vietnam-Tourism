package com.trithuc.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoCart {
    private Long cartItemId;
    private ListTourInCart listTourInCart;
    private Integer quantity;


}
