# 프로젝트 명

## 프로젝트 설명
이 프로젝트는 그룹 내 지출 정산을 효율적으로 관리하기 위한 비용 공유 플랫폼입니다.  
사용자들은 지출을 쉽게 등록하고, 자동으로 정산 내역을 계산하여 투명한 자금 관리를 할 수 있습니다.

## 프로젝트 기술 스택
- **백엔드:** Java, Spring Boot, JPA/Hibernate  
- **프론트엔드:** React (또는 Vue.js, Thymeleaf 등)  
- **데이터베이스:** MySQL  
- **인프라/배포:** Docker, AWS EC2, AWS S3, GitHub Actions 기반 CI/CD  
- **기타:** OCR, Firebase 푸시 알림, OAuth2, JWT 인증

## 아키텍처
- 모놀리식 아키텍처 기반, REST API를 통한 클라이언트-서버 통신  
- AWS EC2 위 Docker 컨테이너로 배포 및 운영  
- S3를 활용한 이미지 및 파일 저장  
- Firebase FCM을 통한 푸시 알림 시스템 연동  
- OAuth2 및 JWT를 활용한 인증과 권한 관리

## ERD (Entity Relationship Diagram)
- 주요 테이블: User, Group, Expense, Settlement, DeviceToken 등  
- User ↔ Group: 다대다 관계  
- Expense가 Group에 속하며, Settlement는 Expense에 대한 정산 내역  
- DeviceToken은 사용자 별 푸시 토큰 저장 (회원과 1:N 관계)

## 주요 기능
- 회원가입 / 로그인 / OAuth2 (Google, Naver) 연동  
- 그룹 생성 및 멤버 초대  
- 지출 내역 등록, 수정, 삭제  
- 정산 자동 계산 및 분배  
- 푸시 알림 및 이메일 알림 발송  
- OCR을 이용한 영수증 이미지 자동 인식  
- AWS S3 연동 파일 업로드 및 관리

## 본인의 역할 및 기여
- 백엔드 API 설계 및 개발 (Spring Boot, JPA)  
- 정산 생성 로직 비즈니스 분리 및 트랜잭션 최소화 리팩토링  
- Docker, nginx, SSL 설정을 통한 운영 환경 구축 및 배포 자동화  
- Firebase Push 알림 연동 및 서비스워커 구현  
- OCR기능 도입을 위한 Tesseract 통합 및 Docker 이미지 빌드

## 개선점 및 배운 점
- 트랜잭션 범위를 최소화하여 동시성 문제 완화와 성능 향상 경험  
- 환경변수 기반 프로젝트 설정으로 보안 및 개발-운영 분리 구현  
- 클라우드 환경에서의 컨테이너 관리 및 CI/CD 자동화  
- 모바일 및 웹 클라이언트와의 실시간 푸시 알림 구현 과정에서 서비스워커 이해도 향상

---
