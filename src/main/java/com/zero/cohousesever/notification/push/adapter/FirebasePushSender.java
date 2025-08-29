package com.zero.cohousesever.notification.push.adapter;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.notification.push.port.PushCommand;
import com.zero.cohousesever.notification.push.port.PushSender;
import com.zero.cohousesever.notification.service.push.DeviceTokenAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebasePushSender implements PushSender {

    private final DeviceTokenAdminService tokenAdminService; // 실패 시 토큰 정리용

    @Override
    public void send(PushCommand cmd) {
        try {
            if (cmd.getToken() == null || cmd.getToken().isBlank()) {
                throw new CustomException(ErrorCode.FCM_TOKEN_MISSING);
            }

            Notification notif = Notification.builder()
                    .setTitle(cmd.getTitle())
                    .setBody(cmd.getBody())
                    .build();

            // 웹만 쓴다 해도 설정 있어도 무해. 필요 없으면 나중에 제거해도 됨.
            AndroidConfig android = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .build();

            // 웹만 쓴다 해도 설정 있어도 무해. 필요 없으면 나중에 제거해도 됨.
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
        } catch (FirebaseMessagingException e) {
            log.error("[FCM] send fail (firebase): {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FCM_SEND_FAIL);
        } catch (Exception e) {
            log.error("[FCM] send fail: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FCM_SEND_FAIL);
        }
    }
}