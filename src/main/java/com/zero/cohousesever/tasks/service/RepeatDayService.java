package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.common.exception.CustomException;
import com.zero.cohousesever.common.exception.ErrorCode;
import com.zero.cohousesever.tasks.dto.repeat.RepeatDayRequest;
import com.zero.cohousesever.tasks.dto.repeat.RepeatDayResponse;
import com.zero.cohousesever.tasks.entity.RepeatDay;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.repository.RepeatDayRepository;
import com.zero.cohousesever.tasks.repository.TaskTemplateRepository;
import java.time.DayOfWeek;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
   * 반복 요일 수정
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

}