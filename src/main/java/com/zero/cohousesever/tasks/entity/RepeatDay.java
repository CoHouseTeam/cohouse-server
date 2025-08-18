package com.zero.cohousesever.tasks.entity;

import com.zero.cohousesever.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 반복 요일
 * 하나의 TaskTemplate에 여러 요일이 지정될 수 있음
 * DayOfWeek는 기본값이 월~일 진행
 * RepeatDayService에서 일~월 형태로 수정
 */

@Entity
@Table(name = "repeat_day")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RepeatDay extends BaseEntity {

  /**
   * 연관된 할일 템플릿
   * N:1 관계
   */

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "template_id", nullable = false)
  private TaskTemplate taskTemplate;

  /**
   * 반복 요일
   * java.time.DayOfWeek: SUNDAY, MONDAY, ..., SATURDAY
   */

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DayOfWeek dayOfWeek; // 서비스에서 일~토 형태로 변경 후 컨트롤러 사용 예정
}



