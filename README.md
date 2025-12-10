# 🚀 NotifyMe
**Scheduled Notification Service with MSA · Kafka · Redis · JWT Authentication**

NotifyMe는 사용자가 예약한 알림을 **정해진 시각에 자동 발송**하는 Notification 플랫폼입니다.  
MSA 구조를 기반으로 하며, **Kafka 기반 비동기 처리**, **Redis 기반 스케줄링/Retry**, **멀티 채널 발송**, **DLQ**,  
그리고 **JWT 로그인 인증**까지 포함한 실전 아키텍처를 제공합니다.

---

## ✨ Features

### 🔐 Authentication
- 회원가입/로그인 (JWT 기반)
- Access Token + Refresh Token
- API Gateway 수준에서 토큰 검증

### 📝 Notification Reservation
- Email / Slack 알림 예약
- 예약 데이터 PENDING 상태 저장
- 알림 취소(CANCEL)

### ⏰ Scheduled Dispatching
- Scheduler가 예약 시간이 된 알림을 Kafka로 enqueue
- Redis Delay Queue 기반 초정밀 스케줄링(옵션)

### 📩 Multi-Channel Sending
- Email Service (SMTP)
- Slack Service (Webhook)
- 채널 확장 구조

### 🔁 Retry / Backoff
- 일시적 오류 시 자동 재시도
- Redis ZSET 기반 Delay Queue
- Exponential Backoff

### 💀 Dead Letter Queue (DLQ)
- 재시도 초과 또는 영구 오류 시 DLQ 이동
- DLQ 조회 / 재처리 제공

### 📊 Delivery History
- 발송 성공/실패 내역 기록
- 사용자 단위 조회 API

---

# 🏗 Architecture Overview
```text
[Client]
    |
    | (signup/login/API requests)
    v
[API Gateway] ─────→ [Auth-Service] (회원가입, 로그인, 토큰 재발급)
    |
    +─→ [Notification-API] (예약/조회/취소)
    |         |
    |         +──→ [DB] ←→ [Redis] (cache, delay queue, lock)
    |
    +─→ [History-Service] (발송 이력 조회)
    
[Scheduler] ──→ Kafka: notification.dispatch-request
     |
[Dispatcher] (Kafka Consumer)
     |
 ┌───────────────┴────────────────┐
 ↓                                 ↓
[Email-Service]                [Slack-Service]
     |                                 |
 Kafka: notification.delivery-result ←──┘
     |
[History-Service] → Delivery Log DB

DLQ:
   Kafka: notification.dlq
   → DLQ Table / Admin Requeue
```

# Modules
- notifyme-auth-service
- notifyme-notification-service ← 여기에 Controller/Service/Repository/Entity 다 포함
- notifyme-scheduler
- notifyme-dispatcher
- notifyme-email-service
- notifyme-slack-service
- notifyme-common