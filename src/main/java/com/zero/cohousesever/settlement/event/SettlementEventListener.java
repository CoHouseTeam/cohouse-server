package com.zero.cohousesever.settlement.event;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.notification.entity.DeviceToken;
import com.zero.cohousesever.notification.push.adapter.FirebasePushSender;
import com.zero.cohousesever.notification.push.port.PushCommand;
import com.zero.cohousesever.notification.repository.DeviceTokenRepository;
import com.zero.cohousesever.settlement.entity.settlement.SettlementParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementEventListener {
    private final FirebasePushSender firebasePushSender;

    private final DeviceTokenRepository deviceTokenRepository;

    //TODO 실패 시 재시도 로직 필요
    @Async
    @EventListener
    public void onSettlementCreated(SettlementCreatedEvent event) {
        try {
            Long groupId = event.getGroupId();
            Long settlementId = event.getSettlementId();
            List<SettlementParticipant> participants = event.getParticipants();

            if (participants == null || participants.isEmpty()) {
                log.warn("정산 참가자가 없음 - settlementId: {}", settlementId);
                return;
            }

            List<Member> participantsForToken = participants.stream()
                    .map(SettlementParticipant::getMember)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (participantsForToken.isEmpty()) {
                log.warn("유효한 멤버가 없음 - settlementId: {}", settlementId);
                return;
            }

            List<DeviceToken> tokens = deviceTokenRepository.findByMemberInAndActiveTrue(participantsForToken);

            if (tokens.isEmpty()) {
                log.info("알림 전송할 활성 토큰이 없음 - settlementId: {}", settlementId);
                return;
            }

            // 푸시 알림 전송
            for (DeviceToken token : tokens) {
                try {
                    PushCommand cmd = PushCommand.builder()
                            .memberId(token.getMember().getId())
                            .token(token.getToken())
                            .title("정산 알림")
                            .body("새로운 정산이 등록되었습니다.")
                            .data(Map.of(
                                    "settlementId", String.valueOf(settlementId),
                                    "groupId", String.valueOf(groupId),
                                    "type", "SETTLEMENT_CREATED"
                            ))
                            .build();

                    firebasePushSender.send(cmd);
                } catch (Exception e) {
                    // 개별 토큰 전송 실패해도 다른 토큰들은 계속 전송
                    log.error("푸시 알림 전송 실패 - memberId: {}, token: {}",
                            token.getMember().getId(), token.getToken(), e);
                }
            }

            log.info("정산 생성 알림 전송 완료 - settlementId: {}, groupId: {}, 전송 대상: {}명",
                    settlementId, groupId, tokens.size());

        } catch (Exception e) {
            log.error("정산 생성 알림 처리 중 오류 발생 - settlementId: {}",
                    event.getSettlementId(), e);
        }
    }

    @Async
    @EventListener
    public void onSettlementCanceled(SettlementCanceledEvent event) {
        try {
            Long groupId = event.getGroupId();
            Long settlementId = event.getGroupId();
            List<SettlementParticipant> participants = event.getParticipants();

            if (participants == null || participants.isEmpty()) {
                log.warn("정산 참가자가 없음 - settlementId: {}", settlementId);
                return;
            }

            List<Member> participantsForToken = participants.stream()
                    .map(SettlementParticipant::getMember)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (participantsForToken.isEmpty()) {
                log.warn("유효한 멤버가 없음 - settlementId: {}", settlementId);
                return;
            }

            List<DeviceToken> tokens = deviceTokenRepository.findByMemberInAndActiveTrue(participantsForToken);

            if (tokens.isEmpty()) {
                log.info("알림 전송할 활성 토큰이 없음 - settlementId: {}", settlementId);
                return;
            }

            // 푸시 알림 전송
            for (DeviceToken token : tokens) {
                try {
                    PushCommand cmd = PushCommand.builder()
                            .memberId(token.getMember().getId())
                            .token(token.getToken())
                            .title("정산 취소 알림")
                            .body("정산이 취소 되었습니다.")
                            .data(Map.of(
                                    "settlementId", String.valueOf(settlementId),
                                    "groupId", String.valueOf(groupId),
                                    "type", "SETTLEMENT_CANCELED"
                            ))
                            .build();

                    firebasePushSender.send(cmd);
                } catch (Exception e) {
                    // 개별 토큰 전송 실패해도 다른 토큰들은 계속 전송
                    log.error("푸시 알림 전송 실패 - memberId: {}, token: {}",
                            token.getMember().getId(), token.getToken(), e);
                }
            }

            log.info("정산 생성 알림 전송 완료 - settlementId: {}, groupId: {}, 전송 대상: {}명",
                    settlementId, groupId, tokens.size());

        } catch (Exception e) {
            log.error("정산 생성 알림 처리 중 오류 발생 - settlementId: {}",
                    event.getSettlementId(), e);
        }
    }

    @Async
    @EventListener
    public void onSettlementPaymentCompleted(PaymentCompletedEvent event) {
        Long settlementId = event.getSettlementId();
        Long senderId = event.getSenderId();

        DeviceToken token = null;

        try {
            PushCommand cmd = PushCommand.builder()
                    .memberId(token.getMember().getId())
                    .token(token.getToken())
                    .title("송금 완료 알림")
                    .body("송금이 완료되었습니다.")
                    .data(Map.of(
                            "settlementId", String.valueOf(settlementId),
                            "senderId", String.valueOf(senderId),
                            "type", "PAYMENT_COMPLETED"
                    ))
                    .build();

            firebasePushSender.send(cmd);
        } catch (Exception e) {
            // 개별 토큰 전송 실패해도 다른 토큰들은 계속 전송
            log.error("푸시 알림 전송 실패 - memberId: {}, token: {}",
                    token.getMember().getId(), token.getToken(), e);
        }
    }

    @Async
    @EventListener
    public void onSettlementPaymentFailed(PaymentCompletedEvent event) {
        Long settlementId = event.getSettlementId();
        Long senderId = event.getSenderId();

        DeviceToken token = null;

        try {
            PushCommand cmd = PushCommand.builder()
                    .memberId(token.getMember().getId())
                    .token(token.getToken())
                    .title("송금 실패 알림")
                    .body("송금이 실패하였습니다. 다시 한 번 시도해 주세요.")
                    .data(Map.of(
                            "settlementId", String.valueOf(settlementId),
                            "senderId", String.valueOf(senderId),
                            "type", "PAYMENT_COMPLETED"
                    ))
                    .build();

            firebasePushSender.send(cmd);
        } catch (Exception e) {
            // 개별 토큰 전송 실패해도 다른 토큰들은 계속 전송
            log.error("푸시 알림 전송 실패 - memberId: {}, token: {}",
                    token.getMember().getId(), token.getToken(), e);
        }
    }
}
