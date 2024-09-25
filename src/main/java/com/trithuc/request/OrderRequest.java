package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
//    private Integer quantity;
    private Double totalPrice;
    private List<Long> cartItemId;
    private String bankCode;
    private LocalDateTime bankDate;
}
