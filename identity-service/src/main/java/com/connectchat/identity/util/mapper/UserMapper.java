package com.connectchat.identity.util.mapper;

import com.connectchat.identity.dto.RegisterRequest;
import com.connectchat.identity.dto.UserDto;
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
