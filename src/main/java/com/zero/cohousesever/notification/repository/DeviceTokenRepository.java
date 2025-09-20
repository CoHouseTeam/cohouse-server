package com.zero.cohousesever.notification.repository;

import com.zero.cohousesever.member.entity.Member;
import com.zero.cohousesever.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * DeviceTokenRepository
 * - 멤버의 최신 활성 토큰 1개를 빠르게 조회하기 위한 메서드 제공.
 */
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    /** lastUsedAt 내림차순으로 최신 활성 토큰 1개 */
    Optional<DeviceToken> findTopByMember_IdAndActiveTrueOrderByLastUsedAtDesc(Long memberId);

    /** 보조 정렬(초기 데이터 등 lastUsedAt 비어 있을 때 대비) */
    Optional<DeviceToken> findTopByMember_IdAndActiveTrueOrderByUpdatedAtDesc(Long memberId);

    /** 토큰 문자열로 단건 조회(사용 흔적 갱신 등에 활용) */
    Optional<DeviceToken> findByToken(String token);

    /** 멤버의 토큰 목록 조회 */
    List<DeviceToken> findByMember_IdAndActiveTrue(Long memberId);

    /** 여러 멤버 토큰 목록 조회 */
    List<DeviceToken> findByMemberInAndActiveTrue(List<Member> members);

    /** 토큰 PK와 소유자 회원ID 조회 */
    Optional<DeviceToken> findByIdAndMember_Id(Long id, Long memberId);
}