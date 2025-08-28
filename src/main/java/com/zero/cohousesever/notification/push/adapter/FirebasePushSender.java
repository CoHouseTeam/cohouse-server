// src/main/java/com/zero/cohousesever/notification/push/adapter/FirebasePushSender.java
package com.zero.cohousesever.notification.push.adapter;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.notification.push.port.PushCommand;
import com.zero.cohousesever.notification.push.port.PushSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class FirebasePushSender implements PushSender {

    @Override
    public void send(PushCommand cmd) throws CustomException {
        try {
            if (cmd.getToken() == null || cmd.getToken().isBlank()) {
                throw new CustomException(ErrorCode.FCM_TOKEN_MISSING);
            }

            Notification notif = Notification.builder()
                    .setTitle(cmd.getTitle())
                    .setBody(cmd.getBody())
                    .build();

            AndroidConfig android = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build();

            ApnsConfig apns = ApnsConfig.builder()
                    .setAps(Aps.builder().setContentAvailable(true).build())
                    .build();

            Message.Builder builder = Message.builder()
                    .setToken(cmd.getToken())
                    .setNotification(notif)
                    .setAndroidConfig(android)
                    .setApnsConfig(apns);

            Map<String, String> data = cmd.getData();
            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            String messageId = FirebaseMessaging.getInstance().send(builder.build());
            log.info("[FCM] sent: memberId={}, messageId={}", cmd.getMemberId(), messageId);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[FCM] send fail: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FCM_SEND_FAIL);
        }
    }
}