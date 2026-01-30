# 🚀 NotifyMe
NotifyMe는 사용자가 예약한 알림을 정해진 시각에 자동 발송하는 Notification 플랫폼입니다.  
MSA 구조를 기반으로 하며, Kafka 기반 비동기 처리를 기반으로 하고 있습니다.

#### 시스템 아키텍처
```
              ┌───────────────────────────┐
              │          Client           │
              └─────────────┬─────────────┘
                            │
                            ▼
              ┌───────────────────────────┐
              │   Notification Gateway    │
              │   - JWT Auth / Routing    │
              └─────────────┬─────────────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
            ▼                               ▼
  ┌──────────────────┐            ┌─────────────────────┐
  │Notification Auth │            │ Notification API    │
  │                  │            │                     │
  │ - Sign up/In     │  	      │ - Notification CRUD │
  │ - Manage Token   │  	      │ - Outbox Pattern    │
  └──────────────────┘            └─────────┬───────────┘
                                            │
                                            │ Kafka (notification-topic)
                                            ▼
                                  ┌──────────────────┐
                                  │   Notification   │
                                  │    Scheduler     │
                                  │                  │
                                  │ - Event Consumer │
                                  │ - DB Polling     │
                                  └─────────┬────────┘
                                            │
                                            │ Kafka (request-topic)
                                            ▼
                                  ┌──────────────────┐
                                  │   Notification   │
                                  │      Sender      │
                                  │                  │
                                  │ - Email / Slack  │
                                  │                  │
                                  └─────────┬────────┘
                                            │
                                            │ Kafka (result-topic)
                                            ▼
                                  ┌──────────────────┐
                                  │ Notification API │
                                  │ (Result Handler) │
                                  └──────────────────┘
```
#### 모듈 구성

| 모듈                         | 역할            | 주요 역할                                                        |
|----------------------------|---------------|--------------------------------------------------------------|
| **notification-api**       | 알림 관리 API     | 알림 생성/수정/조회/취소, Outbox 패턴을 통한 이벤트 발행, 발송 결과 수신                                            |
| **notification-auth**      | 인증 서비스        | 회원가입/로그인, JWT 토큰 발급/갱신, 로그아웃 (토큰 블랙리스트)                      |
| **notification-core**      | 공통 모듈         | 공통 도메인 모델 및 이벤트 정의                                    |
| **notification-eureka**    | Eureka Server | MSA 방식에서 모듈 관리를 위한 Eureka 서버                                 |
| **notification-gateway**   | API Gateway   | 요청 게이트웨이, JWT 토큰 검증 및 헤더를 통한 유저 정보 전달                        |
| **notification-scheduler** | 스케줄러          | Kafka 이벤트 수신 및 스케줄 저장, Polling으로 발송 시간 확인, Kafka로 발송 요청 |
| **notification-sender**    | 발송 서비스        | Email 발송 (SMTP), Slack 발송 (Webhook), 발송 결과를 Kafka로 전송                                                        |


## 데이터 흐름
1. **알림 생성**: API → Outbox 저장 → Kafka 이벤트 발행
2. **스케줄링**: Scheduler가 Kafka 이벤트 수신 → DB 저장 → Polling으로 발송 시간 확인
3. **발송**: 발송 시간 도래 시 Kafka로 요청 → Sender가 실제 발송 → 결과를 Kafka로 전송
4. **결과 처리**: API가 결과 수신 → Notification 상태 업데이트

## 기술 스택
- **Backend**: Kotlin, Coroutines, Spring Boot, Spring Cloud Gateway
- **Messaging**: Kafka, Zookeeper
- **Database**: MySQL, Redis
- **Monitoring**: Prometheus, Grafana
- **Infrastructure**: Docker, Docker Compose