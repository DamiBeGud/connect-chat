package com.connectchat.identity.mapper;

import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.api.response.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto registerRequestToUserDto(RegisterRequest registerRequest) {
        return new UserDto(
            registerRequest.phoneNumber(),
            registerRequest.firstName(),
            registerRequest.lastName(),
            registerRequest.nickname(),
            registerRequest.dateOfBirth(),
            registerRequest.country()
        );
    }
}
