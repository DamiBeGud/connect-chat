package com.connectchat.presence.common.web;

public record Response<T>(Metadata metadata, T data, ErrorInfo error) {}
