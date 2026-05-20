package com.connectchat.identity.service.implementation;

import com.connectchat.identity.config.TwilioProperties;
import com.connectchat.identity.service.SmsClient;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioSmsClient implements SmsClient {
    private final TwilioProperties twilioProperties;

    @Override
    public void sendVerificationCode(String phoneNumber, String verificationCode) {
        if (!isConfigured()) {
            log.info(
                "Twilio is not configured; verification code for phoneNumber={} is {}",
                phoneNumber,
                verificationCode
            );
            return;
        }

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

    private boolean isConfigured() {
        return isRealValue(twilioProperties.accountSid()) &&
            twilioProperties.accountSid().startsWith("AC") &&
            isRealValue(twilioProperties.authToken()) &&
            isRealValue(twilioProperties.fromPhoneNumber()) &&
            twilioProperties.fromPhoneNumber().startsWith("+");
    }

    private boolean isRealValue(String value) {
        return StringUtils.hasText(value) &&
            !"null".equalsIgnoreCase(value) &&
            !"none".equalsIgnoreCase(value) &&
            !"changeme".equalsIgnoreCase(value);
    }
}
