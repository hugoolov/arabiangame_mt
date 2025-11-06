package com.example.auth;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class LoginRequest {
    private String username;
    private String password;
}
