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
    // 불변 리스트 방지를 위해 가변 리스트로 복사
    List<RepeatDay> list = new ArrayList<>(repeatDayRepository.findByTaskTemplate_Id(templateId));

    /**
     * DayOfWeek는 MON=1 SUN=7 구조
     * 일요일부터 시작해야하기 때문에 순서 변경
     */
    list.sort((a, b) ->
        Integer.compare(a.getDayOfWeek().getValue() % 7, b.getDayOfWeek().getValue() % 7)
    );

    return list.stream()
        .map(rd -> RepeatDayResponse.builder()
            .repeatDayId(rd.getId())
            .templateId(rd.getTaskTemplate().getId())
            .dayOfWeek(rd.getDayOfWeek().name())
            .build())
        .toList();
  }

  /**
   * 반복 요일 생성
   */
  public RepeatDayResponse addRepeatDay(Long templateId, RepeatDayRequest request) {
    TaskTemplate template = taskTemplateRepository.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

    // 대소문자 허용
    String norm = request.getDayOfWeek() == null ? "" : request.getDayOfWeek().trim().toUpperCase(
        Locale.ROOT);
    DayOfWeek dow = DayOfWeek.valueOf(norm);

    // 이미 존재하면 그대로 반환(중복X)
    for (RepeatDay rd : repeatDayRepository.findByTaskTemplate_Id(templateId)) {
      if (rd.getDayOfWeek() == dow) {
        return RepeatDayResponse.builder()
            .repeatDayId(rd.getId())
            .templateId(templateId)
            .dayOfWeek(rd.getDayOfWeek().name())
            .build();
      }
    }

    RepeatDay saved = repeatDayRepository.save(
        RepeatDay.builder().taskTemplate(template).dayOfWeek(dow).build()
    );

    return RepeatDayResponse.builder()
        .repeatDayId(saved.getId())
        .templateId(templateId)
        .dayOfWeek(saved.getDayOfWeek().name())
        .build();
  }

  /**
   * 반복 요일 삭제
   */
  public void deleteRepeatDay(Long templateId, Long repeatDayId) {
    RepeatDay rd = repeatDayRepository.findById(repeatDayId)
        .orElseThrow(() -> new IllegalArgumentException("RepeatDay not found: " + repeatDayId));
    if (!rd.getTaskTemplate().getId().equals(templateId)) {
      throw new IllegalArgumentException("RepeatDay does not belong to template: " + templateId);
    }
    repeatDayRepository.deleteById(repeatDayId);
  }

}