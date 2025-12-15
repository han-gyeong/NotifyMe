# 🚀 NotifyMe
NotifyMe는 사용자가 예약한 알림을 **정해진 시각에 자동 발송**하는 Notification 플랫폼입니다.  
MSA 구조를 기반으로 하며, **Kafka 기반 비동기 처리** 를 기반으로 하고 있습니다.

---

## ✨ Features

### 🔐 Authentication
- 회원가입/로그인 (JWT 기반)
- Access Token + Refresh Token
- API Gateway 수준에서 토큰 검증

### 📝 Notification Reservation
- Email / Slack 알림 예약
- 예약 데이터 대기중(WAITING) 상태 저장
- 알림 취소(CANCEL)
- 사전에 예약된 방식으로만 알림 가능

### 📝 User Notification Preferences
- 사용자별 알림 수신 채널 등록/조회 (Email / Slack)
- 기본 채널 설정 및 활성/비활성 토글
- 수신 채널 등록 시 본인 소유 확인

### ⏰ Scheduled Dispatching
- Scheduler가 예약 시간이 된 알림을 Kafka로 enqueue

### 📩 Multi-Channel Sending
- Email Service (SMTP)
- Slack Service (Webhook)
- 채널 확장 구조

### 📊 Delivery History
- 발송 성공/실패 내역 기록
- 사용자 단위 조회 API

# Modules
- notifyme-auth-api
- notifyme-notification-api
- notifyme-scheduler
- notifyme-dispatcher
- notifyme-email-sender
- notifyme-slack-sender