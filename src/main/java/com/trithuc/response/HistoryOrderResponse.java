package com.trithuc.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoryOrderResponse {
    private Long bookingId;
    private String bankCode;
    private LocalDateTime bookingDate;
    private String status;
    private Double totalBooking;
    private List<InfoTourItems> infoTourItems;
}
