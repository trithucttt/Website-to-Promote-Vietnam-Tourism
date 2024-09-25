package com.trithuc.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImage;
    private String address;
    private String avatarUser;
}

