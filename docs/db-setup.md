# Meetball DB 설정 가이드
## 공유 원칙

실제 DB 비밀번호는 절대 커밋하지 않습니다.

팀원에게는 전체 URL이 아니라 아래 항목을 분리해서 보안 채널로 공유합니다.

- `SPRING_DATASOURCE_HOST`
- `SPRING_DATASOURCE_PORT`
- `SPRING_DATASOURCE_DB`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_PROFILES_ACTIVE`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `SPRING_JPA_SHOW_SQL`
- `APP_SEED_ENABLED`

Render Web Service에서는 Render PostgreSQL의 Internal Host를 사용합니다.

로컬 PC에서 Render DB에 붙을 때만 External Host를 사용합니다. Internal Host는 Render 내부 서비스끼리만 접근할 수 있습니다.

## Render 설정

Render는 `prod` 프로파일과 분리 환경변수를 기준으로 설정합니다.

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_HOST`
- `SPRING_DATASOURCE_PORT`
- `SPRING_DATASOURCE_DB`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

운영 환경 권장값은 아래와 같습니다.

- `SPRING_JPA_HIBERNATE_DDL_AUTO=update`
- `SPRING_JPA_SHOW_SQL=false`
- `APP_SEED_ENABLED=false`

`DATABASE_URL`은 현재 표준 설정에서 사용하지 않습니다.

## 로컬 실행

로컬에서 H2와 샘플 데이터로 확인하려면 `local` 프로파일을 사용합니다.

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

IntelliJ Run Configuration에서는 Environment variables에 아래 값을 추가합니다.

- `SPRING_PROFILES_ACTIVE=local`

Render DB에 직접 붙어서 확인해야 하는 팀원은 `prod` 프로파일과 PostgreSQL 환경변수를 함께 등록해서 실행합니다.

공유 DB에 연결할 때는 `APP_SEED_ENABLED=false`를 유지합니다. 샘플 데이터가 필요한 경우에도 공유 DB가 아니라 개인 DB에서만 `true`로 변경하세요.
