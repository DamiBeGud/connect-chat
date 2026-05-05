package com.connectchat.identity;

import com.connectchat.identity.dto.RegisterRequest;
import com.connectchat.identity.dto.UserDto;
import com.connectchat.identity.entity.User;
import com.connectchat.identity.service.UserService;
import com.connectchat.identity.util.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityApplicationServiceImpl
    implements IdentityApplicationService
{

    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public void register(RegisterRequest request) {
        UserDto userDto = userMapper.registerRequestToUserDto(request);
        User user = userService.createUser(userDto);
        
    }
}
