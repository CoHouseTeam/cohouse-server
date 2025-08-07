package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.RepeatDay;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 특정 할일 템플릿(TaskTemplate)이 반복되는 요일 정보를 관리
 * 예: 매주 월, 수, 금 반복되는 설거지
 */

public interface RepeatDayRepository extends JpaRepository<RepeatDay, Long> {
  List<RepeatDay> findByTaskTemplateId(Long templateId);
}
