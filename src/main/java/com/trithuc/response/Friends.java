package com.trithuc.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friends {
    private Long userId;
    private String fullNameUser;
    private String avatar;
}
