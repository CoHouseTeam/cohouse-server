# CoHouse: 쉐어하우스 통합 관리 플랫폼
🏠 [CoHouse 앱 바로가기](https://cohouse-client.vercel.app/)

🔗 [**프로젝트 기획 및 상세 설계 문서 (Notion)**](https://onyx-cloak-677.notion.site/CoHouse-2428e1f790e98080ab7cdfe10843bb78?source=copy_link)


## 🚀 프로젝트 소개
CoHouse는 공동생활의 비효율과 갈등을 해결하는 통합 관리 플랫폼입니다. 집안일 분담, 생활비 정산, 그리고 소통 문제를 한 곳에서 관리하여 투명하고 효율적인 쉐어하우스 생활을 돕습니다.

## 👥 팀원 소개
### 프론트엔드 팀 (Frontend)
| 이름           | 역할          | 담당 페이지 / 기능                                       |
|----------------|---------------|---------------------------------------------------------|
| 김소민         | 팀원          | 메인 페이지, 할일 페이지                                |
| 문하늘         | 팀원          | 그룹 페이지, Navbar, 로그인 및 회원가입 페이지          |
| 민희원         | 팀원          | 보드게시판 페이지, 정산 및 송금 페이지, 마이페이지, 푸시 알림 |

### 백엔드 팀 (Backend)
| 이름           | 역할          | 담당 영역                                                 |
|----------------|---------------|-----------------------------------------------------------|
| 박준엽         | 팀장          | 정산 및 송금, 정산 및 송금 푸시 알림 시스템, 개발서버 인프라 구축 및 운영, CI/CD 파이프라인 구축 |
| 박홍준         | 팀원          | 할일 배정 및 관리, 할일 푸시 알림 시스템 |
| 변상훈         | 팀원          | 인증/인가 시스템, 회원 및 프로필 관리, 그룹 시스템 |
| 최홍목         | 팀원          | 게시판, 게시판 푸시 알림 시스템, 푸시 알림 시스템 구축 |

## 프로젝트 기술 스택
#### **Frontend**
![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Vue.js](https://img.shields.io/badge/Vue.js-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white)

#### **Backend**
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=spring&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)

#### **Database**
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

#### **Infrastructure & Deployment**
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazon-aws&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)

#### **Authentication & Others**
![OAuth2](https://img.shields.io/badge/OAuth2-FB542B?style=for-the-badge&logo=oauth&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![OCR](https://img.shields.io/badge/OCR-FF8800?style=for-the-badge&logo=googledocs&logoColor=white)

## 🏗️ 아키텍처
![git architecture image-001](https://github.com/user-attachments/assets/15b9506b-1dd7-4dcf-9873-3d1288546bba)

## 📊 ERD
![git architecture image-001](https://github.com/user-attachments/assets/adca13a8-3736-4d09-bbe5-825f2f35fc4e)
- 회원 & 그룹
    - 회원과 그룹을 조인 테이블로 연결하여 다대다 관계의 유연성을 확보하고 한 회원이 여러 그룹에 속하는 기능으로 쉽게 확장할 수 있습니다.

- 할일
    - 반복되는 할일을 템플릿과 실제 내역으로 분리해 효율성을 높였습니다. 스케줄러를 통한 할일 자동 배정이 용이합니다.

- 정산
    - 모든 거래 이력을 별도 히스토리 테이블에 기록해 정산의 투명성과 오류 추적을 용이하게 했습니다.

- 게시판
    - 게시글과 좋아요 정보를 분리해 쿼리 성능을 최적화했습니다.

- 알림
    - 알림 설정과 기기 정보를 분리해 개인별 맞춤 알림과 멀티 디바이스 지원이 가능합니다.
  
## ✨ 주요 기능
- 그룹 초대
    - 그룹장은 공유 링크를 생성해 원하는 대상에게 초대할 수 있습니다.

    - 가입 시 닉네임 설정이 가능하며, 기존 그룹 가입자는 탈퇴 후 재가입할 수 있습니다.

    - 그룹 탈퇴 시에는 그룹장의 승인이 필요하며, 그룹장이 1명일 경우에는 직접 탈퇴 또는 그룹 해체가 가능합니다.

- 할일 분담
    - 주 단위 반복 할 일을 가사 항목과 요일 조합을 바탕으로 랜덤 배정합니다.

    - 할일 템플릿 추가 및 삭제가 가능하며, 주별 자동 배정 설정을 지원합니다.

    - 캘린더 시각화와 당일 할일 체크리스트 제공으로 업무 현황을 한눈에 확인할 수 있습니다.

    - 개인별 지정 시간에 푸시 알림을 발송하며, 미이행 시 리마인드 알림도 자동 전송합니다.

    - 업무 교환 요청과 자동 알림 기능을 제공하며, 수락 시 담당자 변경 및 히스토리 기록이 이루어집니다.

    - 미이행 리스트 및 개인 업무 내역 조회 기능도 지원합니다.

- 정산
    - 식비, 생활용품, 문화생활 등 카테고리별로 정산을 관리합니다.

    - 정산 등록과 함께 알림을 발송하며, 참여자 확인과 실시간 상태 업데이트가 가능합니다.

    - 송금 완료 처리 및 미송금 시 매일 알림 기능을 제공합니다.

    - 환불 실패 상태를 관리하고 실패 이력을 기록합니다.

    - 영수증 이미지 OCR 기능을 통해 결제 금액을 자동 추출하며, 오차 금액도 처리합니다.

    - 정산 및 송금 히스토리 관리 기능을 제공합니다.

- 게시판
    - 공지사항 작성 시 전체 알림이 발송됩니다.

    - 자유게시판을 통해 생활 소통 및 개선안을 공유할 수 있습니다.

    - 좋아요 기능 및 알림 설정을 지원합니다.

- 알림
    - 사용자별 알림 ON/OFF 설정과 할일 푸시 알림 시간 지정을 지원합니다.

    - 공지사항, 정산, 할일 등 다양한 푸시 알림을 발송합니다.
---
