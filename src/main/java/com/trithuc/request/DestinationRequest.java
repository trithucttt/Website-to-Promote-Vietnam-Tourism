package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DestinationRequest {
    private String destinationName;
    private MultipartFile image;
    private String address;
    private Long wardId;
}
