package com.zero.cohousesever.notification.policy;

import com.zero.cohousesever.notification.entity.Notification;
import com.zero.cohousesever.notification.entity.NotificationSetting;
import com.zero.cohousesever.notification.type.NotificationStatus;
import com.zero.cohousesever.notification.type.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationPolicyTest {

    private Notification sample(NotificationType type) {
        return Notification.builder()
                .type(type)
                .title("t").content("c")
                .isRead(false)
                .status(NotificationStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("채널 OFF면 SKIP")
    void decide_skip_whenTypeDisabled() {
        NotificationPolicy policy = new NotificationPolicy();
        Notification n = sample(NotificationType.ANNOUNCEMENT);

        NotificationSetting setting = NotificationSetting.builder()
                .announcementEnabled(false)
                .taskEnabled(true)
                .settlementEnabled(true)
                .build();

        DeliveryDecision d = policy.decide(n, Optional.of(setting), false);
        assertThat(d).isEqualTo(DeliveryDecision.SKIP);
    }

    @Test
    @DisplayName("긴급(DELETE_REQUEST)은 즉시 발송")
    void decide_sendNow_forDeleteRequest() {
        NotificationPolicy policy = new NotificationPolicy();
        Notification n = sample(NotificationType.DELETE_REQUEST);

        DeliveryDecision d = policy.decide(n, Optional.empty(), false);
        assertThat(d).isEqualTo(DeliveryDecision.SEND_NOW);
    }

    @Test
    @DisplayName("앱 접속 중이면 즉시 발송")
    void decide_sendNow_whenAppActive() {
        NotificationPolicy policy = new NotificationPolicy();
        Notification n = sample(NotificationType.SETTLEMENT);

        NotificationSetting setting = NotificationSetting.builder()
                .settlementEnabled(true)
                .taskEnabled(true)
                .announcementEnabled(true)
                .build();

        DeliveryDecision d = policy.decide(n, Optional.of(setting), true);
        assertThat(d).isEqualTo(DeliveryDecision.SEND_NOW);
    }

    @Test
    @DisplayName("앱 미접속이면 예약 발송")
    void decide_scheduled_whenAppInactive() {
        NotificationPolicy policy = new NotificationPolicy();
        Notification n = sample(NotificationType.TASK);

        NotificationSetting setting = NotificationSetting.builder()
                .taskEnabled(true)
                .announcementEnabled(true)
                .settlementEnabled(true)
                .build();

        DeliveryDecision d = policy.decide(n, Optional.of(setting), false);
        assertThat(d).isEqualTo(DeliveryDecision.SCHEDULED);
    }
}