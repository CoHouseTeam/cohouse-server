package com.zero.cohousesever.settlement.service;


import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.file.service.S3Service;
import com.zero.cohousesever.group.entity.Group;
import com.zero.cohousesever.group.entity.GroupMember;
import com.zero.cohousesever.group.enums.GroupMemberStatus;
import com.zero.cohousesever.group.repository.GroupMemberRepository;
import com.zero.cohousesever.group.repository.GroupRepository;
import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.member.repository.MemberRepository;
import com.zero.cohousesever.settlement.dto.CreateSettlementRequest;
import com.zero.cohousesever.settlement.dto.ParticipantResponse;
import com.zero.cohousesever.settlement.dto.SettlementHistoryResponse;
import com.zero.cohousesever.settlement.dto.SettlementResponse;
import com.zero.cohousesever.settlement.entity.*;
import com.zero.cohousesever.settlement.repository.PaymentHistoryRepository;
import com.zero.cohousesever.settlement.repository.SettlementHistoryRepository;
import com.zero.cohousesever.settlement.repository.SettlementParticipantRepository;
import com.zero.cohousesever.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

<<<<<<< HEAD
import java.io.IOException;
import java.time.LocalDate;
=======
>>>>>>> develop
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {
    public final MemberRepository memberRepository;
    public final GroupRepository groupRepository;
    public final GroupMemberRepository groupMemberRepository;
    public final SettlementParticipantRepository settlementParticipantRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementHistoryRepository settlementHistoryRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    private final S3Service s3Service;

    /**
     * 정산 등록
     */
    public SettlementResponse createSettlement(Long payerId, CreateSettlementRequest request) {
        Member payer = memberRepository.findById(payerId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Group group = groupMemberRepository
                .findByMemberIdAndStatus(payerId, GroupMemberStatus.ACTIVE)
                .map(GroupMember::getGroup)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        Settlement settlement = Settlement.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .settlementAmount(request.getSettlementAmount())
                .status(SettlementStatus.PENDING)
                .payer(payer)
                .group(group)
                .isEqualDistribution(request.isEqualDistribution())
                .build();

        Set<Long> allParticipantIds = new HashSet<>(request.getParticipantIds());
        allParticipantIds.add(payerId); // 결제자 포함

        List<SettlementParticipant> settlementParticipants = new ArrayList<>();
        if (request.isEqualDistribution()) {
            settlementParticipants = createEqualDistributionParticipants(settlement, allParticipantIds, request.getSettlementAmount());
        } else {
            settlementParticipants = createManualDistributionParticipants(settlement, allParticipantIds, request.getManualShares(), request.getSettlementAmount());
        }

        settlement.setSettlementParticipants(settlementParticipants);
        Settlement savedSettlement = settlementRepository.save(settlement);

        SettlementHistory history = SettlementHistory.builder()
                .settlement(savedSettlement)
                .payer(payer)
                .title(savedSettlement.getTitle())
                .status(savedSettlement.getStatus())
                .changedAt(LocalDateTime.now())
                .build();
        settlementHistoryRepository.save(history);

        return SettlementResponse.fromEntity(savedSettlement);
    }

    // 균등 분배 참여자 생성 메서드
    private List<SettlementParticipant> createEqualDistributionParticipants(Settlement settlement, Set<Long> participantIds, Long totalAmount) {
        Long shareAmount = calculateShareAmount(totalAmount, participantIds.size());
        Long remainder = totalAmount % participantIds.size();
        settlement.setPlatformSupportAmount(remainder);

        List<SettlementParticipant> settlementParticipants = new ArrayList<>();
        for (Long memberId : participantIds) {
            Member member = findMemberOrThrow(memberId);

            SettlementParticipant settlementParticipant = SettlementParticipant.builder()
                    .member(member)
                    .settlement(settlement)
                    .status(memberId.equals(settlement.getPayer().getId()) ? PaymentStatus.PAID : PaymentStatus.PENDING)
                    .shareAmount(shareAmount)
                    .build();
            settlementParticipants.add(settlementParticipant);
        }
        return settlementParticipants;
    }

    // 수동 분배 참여자 생성 메서드
    private List<SettlementParticipant> createManualDistributionParticipants(Settlement settlement, Set<Long> participantIds, Map<Long, Long> manualShares, Long totalAmount) {
        if (manualShares == null || manualShares.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_MANUAL_DISTRIBUTION);
        }

        // 참여자들의 금액의 합이 맞는지 계산
        Long sumShares = manualShares.values().stream().mapToLong(Long::longValue).sum();
        Long payerId = settlement.getPayer().getId();

        // 결제자 부담금 계산
        Long payerShare = totalAmount - sumShares;

        // 분배 금액 합이 정산 금액을 초과하면 예외 처리
        if (payerShare < 0) {
            throw new CustomException(ErrorCode.EXCEED_TOTAL_AMOUNT);
        }

        manualShares.put(payerId, payerShare);

        settlement.setPlatformSupportAmount(0L); // 플랫폼 오차 지원금 없음

        List<SettlementParticipant> settlementParticipants = new ArrayList<>();
        for (Long memberId : participantIds) {
            Member member = findMemberOrThrow(memberId);
            SettlementParticipant settlementParticipant = new SettlementParticipant();
            settlementParticipant.setMember(member);
            settlementParticipant.setSettlement(settlement);
            settlementParticipant.setStatus(memberId.equals(settlement.getPayer().getId()) ? PaymentStatus.PAID : PaymentStatus.PENDING);
            settlementParticipant.setShareAmount(manualShares.getOrDefault(memberId, 0L));
            settlementParticipants.add(settlementParticipant);
        }
        return settlementParticipants;
    }

    // 배분 금액 계산 메서드
    public Long calculateShareAmount(Long totalAmount, int participantCount) {
        if (participantCount <= 0) {
            throw new CustomException(ErrorCode.INVALID_PARTICIPANT_COUNT);
        }
        return totalAmount / participantCount;
    }

    /**
     * 정산 취소 처리
     *
     * - 정산 취소 시 송금을 한 정산 참여자만 환불 상태로 변경
     */
    @Transactional
    public void cancelSettlement(Long memberId, Long settlementId) {
        findMemberOrThrow(memberId);
        Settlement settlement = findSettlementOrThrow(settlementId);

        if (!settlement.getPayer().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.SETTLEMENT_PERMISSION_DENIED);
        }

        // 정산 참여자 상태 변경 및 송금 히스토리 생성
        for (SettlementParticipant participant : settlement.getSettlementParticipants()) {

            PaymentStatus previousStatus = participant.getStatus();

            if (previousStatus == PaymentStatus.PAID) {
                participant.setStatus(PaymentStatus.REFUNDED);
                // 송금 히스토리 생성: 환불 기록 추가
                PaymentHistory refundHistory = PaymentHistory.builder()
                        .sender(participant.getMember())
                        .receiver(settlement.getPayer())
                        .settlement(settlement)
                        .amount(participant.getShareAmount())
                        .status(PaymentStatus.REFUNDED)
                        .transferDate(LocalDateTime.now())
                        .build();
                paymentHistoryRepository.save(refundHistory);

            } else if (previousStatus == PaymentStatus.PENDING) {
                participant.setStatus(PaymentStatus.CANCELED);
            }
        }

        SettlementHistory.builder()
                .settlement(settlement)
                .payer(findMemberOrThrow(memberId))
                .title(settlement.getTitle())
                .status(SettlementStatus.CANCELED)
                .changedAt(LocalDateTime.now())
                .build();

        settlement.setStatus(SettlementStatus.CANCELED);
        settlementRepository.save(settlement);
        settlementParticipantRepository.saveAll(settlement.getSettlementParticipants());
    }

    /**
     * 나의 정산 목록 조회
     */
    public Page<SettlementResponse> getMySettlements(Long memberId, Pageable pageable) {
        // 해당 멤버가 참여한 모든 정산 조회
        Member member = findMemberOrThrow(memberId);
        Page<Settlement> settlements = settlementRepository.findAllByParticipantMember(member, pageable);

        return settlements.map(SettlementResponse::fromEntity);
    }

    /**
     * 나의 특정 정산 상세 조회
     */
    public SettlementResponse getSettlementDetail(Long memberId, Long settlementId) {
        Member member = findMemberOrThrow(memberId);
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND));

        // 해당 멤버가 이 정산에 참여하고 있는지 확인
        if (!settlementParticipantRepository.existsBySettlementIdAndMember(settlementId, member)) {
            throw new CustomException(ErrorCode.NOT_A_SETTLEMENT_PARTICIPANT);
        }

        return SettlementResponse.fromEntity(settlement);
    }

    /**
     * 그룹의 정산 목록 조회 (그룹장용)
     */
    public Page<SettlementResponse> getGroupSettlements(Long memberId, Long groupId, Pageable pageable) {
        Member member = findMemberOrThrow(memberId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        // 그룹 멤버 중에서 해당 회원이 그룹장인지 확인
        if (!groupMemberRepository.existsByGroupAndMemberAndIsLeaderTrue(group, member)) {
            throw new CustomException(ErrorCode.NOT_GROUP_LEADER);
        }

        Page<Settlement> settlements = settlementRepository.findAllByGroup(group, pageable);

        return settlements.map(SettlementResponse::fromEntity);
    }

    /**
     * 정산 참여자 목록 조회
     */
    public List<ParticipantResponse> getSettlementParticipants(Long memberId, Long settlementId) {
        Member member = findMemberOrThrow(memberId);
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND));

        // 해당 멤버가 이 정산에 참여하고 있는지 확인
        if (!settlementParticipantRepository.existsBySettlementIdAndMember(settlementId, member)) {
            throw new CustomException(ErrorCode.NOT_A_SETTLEMENT_PARTICIPANT);
        }

        List<SettlementParticipant> participants = settlementParticipantRepository.findAllBySettlement(settlement);

        return participants.stream()
                .map(ParticipantResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 나의 정산 히스토리 조회
     */
    public Page<SettlementHistoryResponse> getMySettlementHistories(Long memberId, Pageable pageable) {
        Member member = findMemberOrThrow(memberId);
        Page<SettlementHistory> settlementHistories =
                settlementHistoryRepository.findAllBySender(member, pageable);

        return settlementHistories.map(SettlementHistoryResponse::fromEntity);
    }

    /**
     * 영수증 이미지 업로드
     */
    public String uploadReceiptImage(Long memberId, MultipartFile file, Long groupId, Long settlementId) throws IOException {
        Settlement settlement = findSettlementOrThrow(settlementId);
        Member member = findMemberOrThrow(memberId);

        if (!settlement.getPayer().equals(member)) {
            throw new CustomException(ErrorCode.NOT_THE_SETTLEMENT_PAYER);
        }

        // 기존 이미지가 있으면 업로드 막음
        if (settlement.getImageUrl() != null && !settlement.getImageUrl().isEmpty()) {
            throw new CustomException(ErrorCode.FILE_ALREADY_EXISTS);
        }

        // 이미지 검증
        s3Service.validateImageFile(file);

        // 경로 생성
        String dirName = String.format("groups/%d/settlements/%d/receipt", groupId, settlementId);

        String imageUrl = s3Service.uploadFile(file, dirName);

        settlement.setImageUrl(imageUrl);
        settlementRepository.save(settlement);

        return imageUrl;
    }

    /**
     * 영수증 이미지 업데이트
     * - 기존 영수증 이미지 삭제 후 최신 이미지 등록
     */
    public String updateReceiptImage(Long memberId, MultipartFile file, Long groupId, Long settlementId) throws IOException {
        try {
            Settlement settlement = findSettlementOrThrow(settlementId);

            Member member = findMemberOrThrow(memberId);
            if (!settlement.getPayer().equals(member)) {
                throw new CustomException(ErrorCode.NOT_THE_SETTLEMENT_PAYER);
            }

            String imageUrl = settlement.getImageUrl();
            if (settlement.getImageUrl() == null || imageUrl.isEmpty()) {
                throw new CustomException(ErrorCode.FILE_NOT_FOUND);
            }

            // 기존 파일 삭제
            deleteReceiptImage(memberId, settlementId);

            // 새 이미지 업로드
            return uploadReceiptImage(memberId, file, groupId, settlementId);

        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 영수증 이미지 삭제
     */
    public void deleteReceiptImage(Long memberId, Long settlementId) throws IOException {
        Settlement settlement = findSettlementOrThrow(settlementId);
        Member member = findMemberOrThrow(memberId);
        if (!settlement.getPayer().equals(member)) {
            throw new CustomException(ErrorCode.NOT_THE_SETTLEMENT_PAYER);
        }

        String imageUrl = settlement.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        String extractedFilePath =  s3Service.extractFilePath(imageUrl);

        s3Service.deleteFile(extractedFilePath);
        settlement.setImageUrl(null);
        settlementRepository.save(settlement);
    }

    // 회원 엔티티 조회 메서드
    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // 정산 엔티티 조회 메서드
    private Settlement findSettlementOrThrow(Long settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(ErrorCode.SETTLEMENT_NOT_FOUND));
    }
}
