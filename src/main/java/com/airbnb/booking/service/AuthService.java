package com.airbnb.booking.service;

import com.airbnb.booking.dto.request.LoginRequest;
import com.airbnb.booking.dto.request.RegisterRequest;
import com.airbnb.booking.dto.response.AuthResponse;
import com.airbnb.booking.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
