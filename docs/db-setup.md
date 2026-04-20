# Meetball DB 설정 가이드

## 공유 원칙

실제 DB 비밀번호는 절대 커밋하지 않습니다.

팀원에게는 전체 URL이 아니라 아래 항목을 분리해서 보안 채널로 공유합니다:

- `SPRING_DATASOURCE_HOST`
- `SPRING_DATASOURCE_PORT`
- `SPRING_DATASOURCE_DB`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

로컬 PC에서 Render DB에 붙을 때는 Render의 External Host를 사용합니다. Internal Host는 Render 내부 서비스끼리만 접근할 수 있습니다.

## 로컬 실행

1. `.env.example`을 `.env`로 복사합니다.
2. 공유받은 실제 DB 값을 채웁니다.
3. Spring Boot 실행 전에 `.env` 값을 프로세스 환경변수로 올립니다.

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

IntelliJ를 쓰는 경우 Run Configuration > Environment variables에 같은 key-value를 등록해도 됩니다.

## 로컬 프로필 선택

SQL 로그와 로컬 seed 설정이 필요하면 `local` 프로필을 사용합니다:

```bash
set -a
source .env
set +a
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

공유 DB에 연결할 때는 `APP_SEED_ENABLED=false`를 권장합니다. 개인 로컬 DB에 샘플 데이터가 필요할 때만 `true`로 변경하세요.

## Render 설정

Render도 분리 환경변수를 기준으로 설정합니다:

- `SPRING_DATASOURCE_HOST`
- `SPRING_DATASOURCE_PORT`
- `SPRING_DATASOURCE_DB`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

현재 앱은 기존 Render 설정과의 호환을 위해 `DATABASE_URL`도 fallback으로 받을 수 있습니다. 다만 팀 표준은 위의 분리 환경변수 방식입니다.
