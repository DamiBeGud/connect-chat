package com.connectchat.group.common.web;

public record Response<T>(Metadata metadata, T data, ErrorInfo error) {}
