package com.trithuc.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfoResponse {
    private String orderInfo;
    private String paymentCode;
    private String bankTranNo;
    private LocalDateTime payDay;
    private String message;
    private String resCode;
    private Double totalPrice;
}
