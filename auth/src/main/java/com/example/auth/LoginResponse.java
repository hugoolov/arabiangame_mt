package com.example.auth;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class LoginResponse {
    private Long userId;
    private String username;
}