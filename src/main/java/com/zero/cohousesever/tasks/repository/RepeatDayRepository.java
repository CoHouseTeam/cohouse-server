package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.RepeatDay;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 특정 할일 템플릿(TaskTemplate)이 반복되는 요일 정보를 관리
 * 예: 매주 월, 수, 금 반복되는 설거지
 * Java DayOfWeek 기본 구조 변경을 위해 서비스에서 수정
 */
public interface RepeatDayRepository extends JpaRepository<RepeatDay, Long> {
  List<RepeatDay> findByTaskTemplate_Id(Long templateId);
  Optional<RepeatDay> findByTaskTemplate_IdAndDayOfWeek(Long templateId, DayOfWeek dayOfWeek);
  long deleteByIdAndTaskTemplate_Id(Long id, Long templateId);
  boolean existsByTaskTemplate_Id(Long taskTemplateId);

}
