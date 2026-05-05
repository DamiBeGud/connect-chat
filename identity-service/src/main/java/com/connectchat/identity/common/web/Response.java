package com.connectchat.identity.common.web;

public record Response<T>(Metadata metadata, T data, ErrorInfo error) {}
