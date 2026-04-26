# 🚀 NotifyMe
NotifyMe는 사용자가 예약한 알림을 정해진 시각에 자동 발송하는 Notification 플랫폼입니다.  
MSA 구조를 기반으로 하며, Kafka와 Outbox 패턴을 사용해 알림 발송 요청을 비동기로 처리합니다.

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
  │ - Manage Token   │  	      │ - Result Handler    │
  └──────────────────┘            └─────────┬───────────┘
                                            │
                                            │ Shared DB
                                            ▼
                                  ┌──────────────────┐
                                  │   Notification   │
                                  │    Scheduler     │
                                  │                  │
                                  │ - Dispatch Poll  │
                                  │ - Outbox Pattern │
                                  │ - Outbox Publish │
                                  └─────────┬────────┘
                                            │
                                            │ Kafka (send-request topic)
                                            ▼
                                  ┌──────────────────┐
                                  │   Notification   │
                                  │      Sender      │
                                  │                  │
                                  │ - Email / Slack  │
                                  │                  │
                                  └─────────┬────────┘
                                            │
                                            │ Kafka (send-result topic)
                                            ▼
                                  ┌──────────────────┐
                                  │ Notification API │
                                  │ (Result Handler) │
                                  └──────────────────┘
```
#### 모듈 구성

| 모듈                         | 역할            | 주요 역할                                                        |
|----------------------------|---------------|--------------------------------------------------------------|
| **notification-api**       | 알림 관리 API     | 알림 생성/수정/조회/취소, 발송 결과 수신 및 최종 상태 반영 |
| **notification-auth**      | 인증 서비스        | 회원가입/로그인, JWT 토큰 발급/갱신, 로그아웃 (토큰 블랙리스트)                      |
| **notification-core**      | 공통 모듈         | 공통 도메인 모델 및 이벤트 정의                                    |
| **notification-domain**    | 영속성 도메인       | `Notification`, `NotificationOutbox`, `OutboxStatus` 등 JPA 엔티티 |
| **notification-gateway**   | API Gateway   | 요청 게이트웨이, JWT 토큰 검증 및 헤더를 통한 유저 정보 전달                        |
| **notification-scheduler** | 스케줄러          | 예약 알림 claim, Outbox 저장, Outbox polling으로 Kafka 발송 요청 발행 |
| **notification-sender**    | 발송 서비스        | Email 발송 (SMTP), Slack 발송 (Webhook), 발송 결과를 Kafka로 전송                                                        |


## 데이터 흐름
1. **알림 등록/변경**: API가 `Notification`을 저장하거나 수정/취소합니다.
2. **발송 대상 claim**: Scheduler의 `NotificationDispatchScheduler`가 `WAITING` 상태이고 `notifyAt <= now`인 알림을 polling합니다.
3. **Outbox 저장**: `NotificationDispatchService`가 대상 알림을 `IN_PROGRESS`로 변경하고, 같은 트랜잭션에서 `NotificationOutbox`에 `SendRequest` payload를 저장합니다.
4. **발송 요청 발행**: `NotificationOutboxPublisher`가 `WAITING` Outbox를 읽어 채널별 Kafka send-request topic으로 발행합니다.
5. **발송 처리**: Sender가 Email 또는 Slack으로 실제 발송한 뒤 Kafka send-result topic으로 결과를 반환합니다.
6. **결과 반영**: API가 발송 결과를 수신해 `Notification` 상태를 `SUCCESS` 또는 `FAILED`로 업데이트합니다.

## Scheduler Outbox
Scheduler는 DB 상태 변경과 Kafka 발행 사이의 유실을 줄이기 위해 Outbox 패턴을 사용합니다.

- `NotificationDispatchScheduler`: 1초 주기로 발송 시간이 된 알림을 조회합니다.
- `NotificationDispatchService`: 알림을 `IN_PROGRESS`로 claim하고 `NotificationOutbox` row를 생성합니다.
- `NotificationOutboxPublisher`: 1초 주기로 발행 가능한 Outbox를 조회해 Kafka로 발행합니다.
- 발행 성공 시 Outbox는 `SENT`, 알림은 `SENT`로 변경됩니다.
- 발행 실패 시 `retryCount`와 `nextPublishAt`을 갱신하고, 재시도 한도를 넘거나 payload/채널 오류가 있으면 `FAILED`로 변경합니다.

## Kafka 토픽
| 토픽 | Producer | Consumer | 용도 |
|------|----------|----------|------|
| `notification.email.send-request` | notification-scheduler | notification-sender | Email 발송 요청 |
| `notification.slack.send-request` | notification-scheduler | notification-sender | Slack 발송 요청 |
| `notification.common.send-result` | notification-sender | notification-api | 발송 결과 반환 |

## 기술 스택
- **Backend**: Kotlin, Coroutines, Spring Boot, Spring Cloud Gateway
- **Messaging**: Kafka, Zookeeper
- **Database**: MySQL, Redis
- **Monitoring**: Prometheus, Grafana
- **Infrastructure**: Docker, Docker Compose, AWS ECS, AWS ECR
- **CI/CD**: GitHub Actions