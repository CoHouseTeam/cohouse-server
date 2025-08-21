package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.RepeatDay;
import java.time.DayOfWeek;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

  // pr 내용 수정 : 템플릿 집합 중 반복요일 있는 템플릿 id들 조회
  @Query("select distinct rd.taskTemplate.id from RepeatDay rd where rd.taskTemplate.id in :templateIds")
  Set<Long> findTemplateIdsHavingRepeat(@Param("templateIds") Collection<Long> templateIds);

}
