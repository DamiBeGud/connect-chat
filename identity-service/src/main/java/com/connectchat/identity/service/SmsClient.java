package com.connectchat.identity.service;

public interface SmsClient {
    void sendVerificationCode(String phoneNumber, String verificationCode);
}
