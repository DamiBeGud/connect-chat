package com.connectchat.identity.service;

import com.connectchat.identity.api.response.UserDto;
import com.connectchat.identity.entity.User;

public interface UserService {
    User createUser(UserDto userDto);
}
