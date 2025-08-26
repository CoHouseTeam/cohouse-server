package com.zero.cohousesever.tasks.repository;

import com.zero.cohousesever.tasks.entity.TaskTemplate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 할일 카테고리, 생성일 등을 기준으로 할일 템플릿을 관리
 * 하나의 그룹에 속하는 반복적 할일 정의
 */

public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {
  List<TaskTemplate> findByGroupId(Long groupId);
}
