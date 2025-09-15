package com.zero.cohousesever.task.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.task.dto.repeat.RepeatDayRequest;
import com.zero.cohousesever.task.dto.repeat.RepeatDayResponse;
import com.zero.cohousesever.task.entity.RepeatDay;
import com.zero.cohousesever.task.entity.TaskTemplate;
import com.zero.cohousesever.task.repository.RepeatDayRepository;
import com.zero.cohousesever.task.repository.TaskTemplateRepository;
import java.time.DayOfWeek;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 반복 요일 설정 관련 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
public class RepeatDayService {

  private final RepeatDayRepository repeatDayRepository;
  private final TaskTemplateRepository taskTemplateRepository;


  /**
   * 반복 요일 조회
   */
  public List<RepeatDayResponse> getRepeatDaysByTemplateId(Long templateId) {
    return repeatDayRepository.findByTaskTemplate_Id(templateId).stream()
        .sorted(
            java.util.Comparator.comparingInt(rd -> rd.getDayOfWeek().getValue() % 7)) // SUNDAY 먼저
        .map(RepeatDayResponse::from)
        .toList();
  }

  /**
   * 반복 요일 생성
   */
  public RepeatDayResponse addRepeatDay(Long templateId, RepeatDayRequest request) {
    TaskTemplate template = taskTemplateRepository.findById(templateId)
        .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

    String raw = request.getDayOfWeek();
    if (raw == null || raw.isBlank()) {
      throw new CustomException(ErrorCode.REPEAT_DAY_NOT_FOUND);
    }

    final DayOfWeek dow;
    try {
      dow = DayOfWeek.valueOf(raw.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new CustomException(ErrorCode.DATE_FORMAT_INVALID);
    }

    return repeatDayRepository.findByTaskTemplate_IdAndDayOfWeek(templateId, dow)
        .map(RepeatDayResponse::from)
        .orElseGet(() -> {
          RepeatDay saved = repeatDayRepository.save(
              RepeatDay.builder().taskTemplate(template).dayOfWeek(dow).build()
          );
          return RepeatDayResponse.from(saved);
        });
  }

  /**
   * 반복 요일 수정 단일
   */
  public RepeatDayResponse updateRepeatDay(Long templateId, Long repeatDayId, RepeatDayRequest request) {
    TaskTemplate template = taskTemplateRepository.findById(templateId)
        .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

    // 반복요일 파라미터 검증
    String raw = request.getDayOfWeek();
    if (raw == null || raw.isBlank()) {
      throw new CustomException(ErrorCode.INVALID_REQUEST); // ← 비어있음: 요청값 오류
    }

    final DayOfWeek dow;
    try {
      dow = DayOfWeek.valueOf(raw.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new CustomException(ErrorCode.DATE_FORMAT_INVALID); // ← 잘못된 요일 문자열
    }

    // 수정 대상 조회 + 템플릿 소속 검증
    RepeatDay target = repeatDayRepository.findById(repeatDayId)
        .orElseThrow(() -> new CustomException(ErrorCode.REPEAT_DAY_NOT_FOUND));
    if (!target.getTaskTemplate().getId().equals(template.getId())) {
      // 다른 템플릿의 항목을 수정하려는 경우
      throw new CustomException(ErrorCode.REPEAT_DAY_NOT_FOUND);
    }
    // 동일 요일 중복 방지(자기 자신 제외)
    var dup = repeatDayRepository.findByTaskTemplate_IdAndDayOfWeek(templateId, dow);
    if (dup.isPresent() && !dup.get().getId().equals(repeatDayId)) {
      throw new CustomException(ErrorCode.REPEAT_DAY_ALREADY_EXISTS);
    }

    // 변경 없으면 그대로 반환
    if (target.getDayOfWeek() == dow) {
      return RepeatDayResponse.from(target);
    }

    target.setDayOfWeek(dow);
    RepeatDay saved = repeatDayRepository.save(target);
    return RepeatDayResponse.from(saved);
  }

  /**
   * 반복 요일 삭제
   */
  public void deleteRepeatDay(Long templateId, Long repeatDayId) {
    long deleted = repeatDayRepository.deleteByIdAndTaskTemplate_Id(repeatDayId, templateId);
    if (deleted == 0) {
      throw new CustomException(ErrorCode.REPEAT_DAY_NOT_FOUND);
    }
  }

  // 일괄 수정
  @Transactional
  public void replaceRepeatDays(Long templateId, List<String> days) {
    TaskTemplate template = taskTemplateRepository.findById(templateId)
        .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

    // 1) 입력 파싱/검증
    java.util.Set<java.time.DayOfWeek> target = new java.util.LinkedHashSet<>();
    if (days != null) {
      for (String raw : days) {
        if (raw == null || raw.isBlank()) {
          throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        try {
          target.add(java.time.DayOfWeek.valueOf(raw.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
          throw new CustomException(ErrorCode.DATE_FORMAT_INVALID);
        }
      }
    }

    // 2) 현재 상태 조회
    java.util.List<RepeatDay> current = repeatDayRepository.findByTaskTemplate_Id(templateId);
    java.util.Map<java.time.DayOfWeek, RepeatDay> byDow = new java.util.HashMap<>();
    for (RepeatDay rd : current) byDow.put(rd.getDayOfWeek(), rd);

    // 3) 삭제: 현재에 있지만 target에 없는 요일 제거
    for (RepeatDay rd : current) {
      if (!target.contains(rd.getDayOfWeek())) {
        repeatDayRepository.delete(rd);
      }
    }

    // 4) 추가: target에 있지만 현재에 없는 요일 추가
    for (java.time.DayOfWeek dow : target) {
      if (!byDow.containsKey(dow)) {
        repeatDayRepository.save(
            RepeatDay.builder().taskTemplate(template).dayOfWeek(dow).build()
        );
      }
    }
  }
}