# 🎵 BEAT-UP Platform - Backend

> **관람의 시작과 끝까지 책임지는 관객 중심의 동행 이동 플랫폼**  
> 공연 관람객들의 안전하고 편리한 동행 이동을 지원하는 Spring Boot 기반 백엔드 시스템

## 📋 프로젝트 개요

BEAT-UP은 공연장으로의 접근성 문제를 해결하고, 관람객들 간의 동행 이동 매칭을 통해 더 나은 공연 경험을 제공하는 플랫폼입니다. 특히 수도권 외곽이나 지방 공연장으로 분산되는 대형 콘서트에 참여하는 관람객들의 이동 불편을 해소합니다.

### 주요 기능
- 🎫 **공연 정보 관리**: KOPIS API 연동을 통한 공연 정보 수집 및 관리
- 🚖 **동승 매칭 시스템**: 위치 기반 실시간 택시 동승 매칭
- 💬 **실시간 채팅**: WebSocket 기반 공연별 채팅방
- 📍 **장소 검색**: 카카오 로컬 API 연동 장소 검색 및 PostGIS 기반 위치 쿼리
- 🔔 **푸시 알림**: Firebase Cloud Messaging을 통한 실시간 알림
- 👥 **커뮤니티**: 공연별 게시글 및 후기 시스템

## 🛠️ 기술 스택

### Backend Framework
- **Spring Boot** - Java 17 기반 애플리케이션 프레임워크
- **Spring Data JPA** - 데이터베이스 접근 계층
- **QueryDSL** - 타입 안전한 동적 쿼리 작성
- **Spring WebSocket** - 실시간 양방향 통신

### Database & Storage
- **PostgreSQL** - 메인 관계형 데이터베이스
- **PostGIS** - 공간 데이터 처리 및 위치 기반 쿼리 최적화
- **Redis** - 캐싱 및 세션 관리
- **Flyway** - 데이터베이스 마이그레이션 관리

### Message Queue & Communication
- **RabbitMQ** - 비동기 메시지 큐 (FCM 알림 처리)
- **Firebase Cloud Messaging** - 푸시 알림 서비스
- **WebSocket** - 실시간 채팅 및 매칭 상태 전송

### API Integration
- **KOPIS API** - 공연 정보 수집 (한국문화예술위원회)
- **카카오 로컬 API** - 장소 검색 및 지도 서비스
- **RESTful API** - HTTP 표준 메서드 기반 API 설계

### Infrastructure & DevOps
- **Docker & Docker Compose** - 컨테이너 기반 개발 환경
- **Kubernetes** - 컨테이너 오케스트레이션 (k8s 설정 포함)
- **Prometheus** - 메트릭 수집 및 모니터링
- **Spring Boot Actuator** - 애플리케이션 모니터링

### Development Tools
- **Lombok** - 보일러플레이트 코드 감소
- **SpringDoc OpenAPI (Swagger)** - API 문서화
- **JUnit 5** - 단위 테스트 프레임워크
- **WireMock** - 외부 API 모킹



### 필수 요구사항
- **Java 17** 이상
- **Docker & Docker Compose**
- **Gradle 8.x** (Gradle Wrapper 포함)


### 마이그레이션 실행
애플리케이션 시작 시 자동으로 실행됩니다. 수동 실행이 필요한 경우:

```bash
./gradlew flywayMigrate
```

## 🧪 테스트

### 단위 테스트 실행
```bash
./gradlew test
```

### 특정 테스트 클래스 실행
```bash
./gradlew test --tests "com.BeatUp.BackEnd.Places.PlacesServiceTest"
```


### 주요 API 엔드포인트

#### 인증
- `POST /api/auth/login` - 로그인
- `POST /api/auth/register` - 회원가입
- `GET /api/auth/me` - 현재 사용자 정보

#### 공연
- `GET /api/concerts` - 공연 목록 조회
- `GET /api/concerts/{id}` - 공연 상세 조회
- `POST /api/admin/concerts/sync` - 공연 정보 동기화 (관리자)

#### 장소 검색
- `GET /api/places/search` - 장소 검색
- `GET /api/places/nearby` - 주변 장소 조회

#### 매칭
- `POST /api/match/request` - 매칭 요청
- `GET /api/match/status` - 매칭 상태 조회

#### 채팅
- `GET /api/chat/rooms` - 채팅방 목록
- `WebSocket /ws/chat` - 실시간 채팅

## 🔧 주요 설정

### Redis 캐싱
- **타입**: Caffeine
- **최대 크기**: 1000개
- **만료 시간**: 1시간

### RabbitMQ 설정
- **Exchange**: `fcm.exchange` (Topic Exchange)
- **Queue**: `fcm.notifications.queue`
- **Failed Queue**: `fcm.notifications.failed.queue`
- **Routing Keys**:
    - `fcm.chat` - 채팅 알림
    - `fcm.match` - 매칭 알림

### 메트릭 수집
Prometheus 메트릭이 `/actuator/prometheus` 엔드포인트에서 제공됩니다.



## ☸️ Kubernetes 배포

Kubernetes 설정 파일은 `k8s/` 디렉토리에 있습니다:

```bash
kubectl apply -f k8s/
```

주요 리소스:
- `namespace.yaml` - 네임스페이스
- `configmap.yaml` - 설정
- `secret.yaml` - 시크릿
- `deployment.yaml` - 애플리케이션 배포
- `service.yaml` - 서비스
- `ingress.yaml` - 인그레스
- `postgres-stateful.yaml` - PostgreSQL StatefulSet
- `redis-deployment.yaml` - Redis 배포

## 📈 모니터링

### Actuator 엔드포인트
- `/actuator/health` - 헬스 체크
- `/actuator/metrics` - 메트릭 목록
- `/actuator/prometheus` - Prometheus 메트릭

### 주요 모니터링 지표
- API 호출 시간 (P50, P95, P99)
- 데이터베이스 쿼리 실행 시간
- 트랜잭션 실행 시간
- WebSocket 메시지 처리 시간
- 매칭 처리 시간

## 🔐 보안
- **Firebase Authentication**: 소셜 로그인 지원
- **Spring Security**: 엔드포인트 보안 설정
- **CORS 설정**: WebMvcConfig에서 관리

## 🚧 향후 계획

### 알고리즘 고도화
- **머신러닝 기반** 개인화 매칭
- **예측 모델링**을 통한 수요 예측
- **최적 경로** 추천 시스템

### 성능 최적화
- 데이터베이스 쿼리 최적화
- 캐싱 전략 개선
- 비동기 처리 확대

### 기능 확장
- 실시간 위치 추적
- 결제 시스템 연동
- 리뷰 및 평점 시스템 고도화


