package com.connectchat.identity.service.implementation;

import com.connectchat.identity.config.TwilioProperties;
import com.connectchat.identity.service.SmsClient;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TwilioSmsClient implements SmsClient {
    private final TwilioProperties twilioProperties;


    @Override
    public void sendVerificationCode(String phoneNumber, String verificationCode) {
        Twilio.init(
            twilioProperties.accountSid(),
            twilioProperties.authToken()
        );

        Message.creator(
            new PhoneNumber(phoneNumber),
            new PhoneNumber(twilioProperties.fromPhoneNumber()),
            "Your Connect Chat verification code is: " + verificationCode
        ).create();
    }
}
