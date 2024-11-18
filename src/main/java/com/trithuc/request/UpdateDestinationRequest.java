package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDestinationRequest {
    private Long destinationId;
    private String destinationName;
    private String address;
    private Long wardId;
    private String description;
}
