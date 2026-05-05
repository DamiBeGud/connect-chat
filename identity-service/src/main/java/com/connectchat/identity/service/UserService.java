package com.connectchat.identity.service;

import com.connectchat.identity.dto.UserDto;
import com.connectchat.identity.entity.User;

public interface UserService {
    User createUser(UserDto userDto);
}
