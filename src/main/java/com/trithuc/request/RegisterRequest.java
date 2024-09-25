package com.trithuc.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String lastName;
    private String firstName;
    private String username;
    private String email;
    private String password;
    private String confirmPass;
    private String role;
}
