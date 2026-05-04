package com.connectchat.identity.util;

public record Response<T>(Metadata metadata, T data, ErrorInfo error) {}
