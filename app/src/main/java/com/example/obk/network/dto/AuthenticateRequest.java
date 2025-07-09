package com.example.obk.network.dto;

public class AuthenticateRequest {
    public final String username;
    public final String response;
    public AuthenticateRequest(String username, String response) {
        this.username = username;
        this.response = response;
    }
}
