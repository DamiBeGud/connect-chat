package com.connectchat.identity.service.implementation;

import com.connectchat.identity.api.request.RegisterRequest;
import com.connectchat.identity.api.response.UserDto;
import com.connectchat.identity.mapper.UserMapper;
import com.connectchat.identity.service.AuthService;
import com.connectchat.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl
    implements AuthService
{

    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public void register(RegisterRequest request) {
        UserDto userDto = userMapper.registerRequestToUserDto(request);
        userService.createUser(userDto);
    }
}
