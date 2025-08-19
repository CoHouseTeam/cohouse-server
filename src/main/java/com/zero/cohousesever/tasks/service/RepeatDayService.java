package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.tasks.dto.repeat.RepeatDayRequest;
import com.zero.cohousesever.tasks.dto.repeat.RepeatDayResponse;
import com.zero.cohousesever.tasks.entity.RepeatDay;
import com.zero.cohousesever.tasks.entity.TaskTemplate;
import com.zero.cohousesever.tasks.repository.RepeatDayRepository;
import com.zero.cohousesever.tasks.repository.TaskTemplateRepository;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

    String raw = request.getDayOfWeek();
    if (raw == null)
      throw new IllegalArgumentException("dayOfWeek is required");

    DayOfWeek dow = DayOfWeek.valueOf(raw); // 대소문자/trim 정규화 제거

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



  /**
   * 반복 요일 삭제
   */
  public void deleteRepeatDay(Long templateId, Long repeatDayId) {
    long deleted = repeatDayRepository.deleteByIdAndTaskTemplate_Id(repeatDayId, templateId);
    if (deleted == 0) {
      throw new IllegalArgumentException("RepeatDay does not belong to template: " + templateId +
          " (id=" + repeatDayId + ")");
    }
  }


}