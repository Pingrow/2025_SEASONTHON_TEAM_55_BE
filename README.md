# Fingrow

## 프로젝트 실행 방법

### 🚀 Docker로 한 번에 실행 (권장)
```bash
# 전체 애플리케이션 실행 (데이터베이스 + 앱)
docker-compose up -d

# 실행 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f app
```

### 🔧 개발 모드 실행
```bash
# 데이터베이스만 실행
docker-compose up -d mysql

# 애플리케이션을 로컬에서 실행
./gradlew bootRun
```

## API 엔드포인트

### 인증 API
- **POST** `/api/v1/auth/kakao` - 카카오 OAuth 로그인
- **POST** `/api/v1/auth/refresh` - 토큰 갱신
- **GET** `/api/v1/auth/me` - 현재 사용자 정보
- **POST** `/api/v1/auth/logout` - 로그아웃

### 카카오 로그인 사용법
```javascript
// 1. 카카오에서 인가코드 받기
// 2. 백엔드로 인가코드 전송
const response = await fetch('/api/v1/auth/kakao', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ code: '인가코드' })
});

const { accessToken, refreshToken, user } = await response.json();
```

## 설정 정보

### 데이터베이스 접속 정보
- **Host**: localhost
- **Port**: 3306
- **Database**: fingrow
- **Username**: fingrow
- **Password**: fingrow123

### 카카오 OAuth 설정
`application.yml`에서 다음 값들을 설정하세요:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: your-kakao-client-id
            client-secret: your-kakao-client-secret
```

## 유용한 명령어

```bash
# 전체 서비스 중지
docker-compose down

# 데이터베이스 포함 완전 삭제
docker-compose down -v

# 애플리케이션 다시 빌드
docker-compose up -d --build

# 개별 서비스 로그 확인
docker-compose logs mysql
docker-compose logs app

# 컨테이너 내부 접속
docker-compose exec app bash
docker-compose exec mysql mysql -u fingrow -p fingrow
```

## 개발 환경 요구사항

### Docker 사용 시
- Docker & Docker Compose

### 로컬 개발 시
- Java 17
- Docker & Docker Compose (DB용)
- Gradle