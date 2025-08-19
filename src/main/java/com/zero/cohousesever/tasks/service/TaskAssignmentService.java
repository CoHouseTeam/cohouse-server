package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentRequest;
import com.zero.cohousesever.tasks.dto.assignment.TaskAssignmentResponse;
import com.zero.cohousesever.tasks.entity.RepeatDay;
import com.zero.cohousesever.tasks.entity.TaskAssignment;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.repository.RepeatDayRepository;
import com.zero.cohousesever.tasks.repository.TaskAssignmentRepository;
import com.zero.cohousesever.tasks.repository.TaskTemplateRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 실제 주간/일간 할일 배정 및 스케줄 생성, 조회 등 담당
 */
@Service
@RequiredArgsConstructor
public class TaskAssignmentService {

  private final TaskAssignmentRepository taskAssignmentRepository;
  private final TaskTemplateRepository taskTemplateRepository;
  private final RepeatDayRepository repeatDayRepository;

  /**
   * 템플릿의 반복요일을 확인한 뒤, 후보 목록에서 한 사람을 랜덤으로 결정
   * 해당 주(요청 date 기준)의 모든 반복요일에 동일 담당자로 배정한다. (한 주에 담당자 1명)
   * 이미 같은 (templateId, date)이 존재하면 생성하지 않고 스킵한다. (중복방지)
   */
  public List<TaskAssignmentResponse> assignTaskManuallyOrRandomly(
      TaskAssignmentRequest req, List<Long> candidateMemberIds) {

    // 1) 필수값 검증: templateId는 필수, 후보 없으면 전용 에러
    if (req.getTemplateId() == null) {
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
    if (candidateMemberIds == null || candidateMemberIds.isEmpty()) {
      throw new CustomException(ErrorCode.CANDIDATE_MEMBERS_REQUIRED);
    }

    // 2) 템플릿/반복요일 조회
    TaskTemplate template = taskTemplateRepository.findById(req.getTemplateId())
        .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

    List<RepeatDay> days = repeatDayRepository.findByTaskTemplate_Id(req.getTemplateId());
    if (days.isEmpty()) {
      throw new CustomException(ErrorCode.REPEAT_DAY_NOT_FOUND);
    }

    // 3) 기준 날짜: 입력 없으면 오늘(Asia/Seoul) 기준 '다음 주', 있으면 파싱 후 '다음 주'
    final LocalDate base;
    try {
      if (req.getDate() == null || req.getDate().isBlank()) {
        base = LocalDate.now(ZoneId.of("Asia/Seoul")).plusWeeks(1);
      } else {
        base = LocalDate.parse(req.getDate()).plusWeeks(1);
      }
    } catch (DateTimeParseException e) {
      throw new CustomException(ErrorCode.DATE_FORMAT_INVALID);
    }
    LocalDate sunday = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

    // 4) 한 번만 랜덤 추출  그 주 전부 같은 담당자
    Long pickedMemberId = candidateMemberIds.get(new java.util.Random().nextInt(candidateMemberIds.size()));

    List<TaskAssignment> toSave = new ArrayList<>();
    for (RepeatDay rd : days) {
      LocalDate d = sunday.plusDays(rd.getDayOfWeek().getValue() % 7); // SUN=0
      // 중복이면 스킵
      if (taskAssignmentRepository.existsByTemplate_IdAndDate(req.getTemplateId(), d)) continue;

      toSave.add(TaskAssignment.builder()
          .template(template)
          .groupMemberId(pickedMemberId)
          .date(d)
          .build()); // @PrePersist → PENDING
    }

    // 5) 저장: 비어있으면 DB 호출도 하지 말고 바로 빈 리스트 반환 (Mockito 검증 통과)
    if (toSave.isEmpty()) return List.of();

    return TaskAssignmentResponse.fromAll(taskAssignmentRepository.saveAll(toSave));
  }
  /**
   * 할일 배정 목록 조회
   */
  public void getAssignmentsByWeekAndMember() {
    // 구현 예정
  }

  /**
   * 할일 상태 변경 (완료, 연기 등)
   */
  public void updateAssignmentStatus() {
    // 구현 예정
  }
}
