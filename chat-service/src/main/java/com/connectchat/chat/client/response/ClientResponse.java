package com.connectchat.chat.client.response;

public record ClientResponse<T>(
    ClientMetadata metadata,
    T data,
    ClientErrorInfo error
) {}
