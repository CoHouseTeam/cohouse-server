package com.zero.cohousesever.tasks.service;

import com.zero.cohousesever.tasks.repository.RepeatDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 반복 요일 설정 관련 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
public class RepeatDayService {

  private final RepeatDayRepository repeatDayRepository;

  /**
   * 반복 요일 조회
   */
  public void getRepeatDaysByTemplateId(Long templateId) {
    // 구현 예정
  }

  /**
   * 반복 요일 생성
   */
  public void addRepeatDay(Long templateId, String dayOfWeek) {
    // 구현 예정
  }

  /**
   * 반복 요일 삭제
   */
  public void deleteRepeatDay(Long templateId, Long repeatDayId) {
    // 구현 예정
  }

}
