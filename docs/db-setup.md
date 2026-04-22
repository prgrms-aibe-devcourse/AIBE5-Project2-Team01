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
- `GOOGLE_CLIENT_ID`

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
- Google 로그인 기능을 사용할 경우 `GOOGLE_CLIENT_ID`를 Render 환경변수에 추가

`DATABASE_URL`은 현재 표준 설정에서 사용하지 않습니다.

Render 로그가 `No active profile set, falling back to 1 default profile: "prod"`로 보이면 `SPRING_PROFILES_ACTIVE`가 Render 환경변수에 직접 적용되지 않은 상태입니다. 애플리케이션 기본 프로파일이 `prod`라 PostgreSQL로 기동되지만, Render Dashboard의 Environment에도 `SPRING_PROFILES_ACTIVE=prod`를 추가해 두는 것을 권장합니다.

## 로컬 실행

로컬에서 H2와 샘플 데이터로 확인하려면 `local` 프로파일을 사용합니다. 기본 로컬 DB는 `.local/meetballdb` 파일 기반 H2이며, `.local/`은 Git 추적에서 제외됩니다.

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

IntelliJ Run Configuration에서는 Environment variables에 아래 값을 추가합니다.

- `SPRING_PROFILES_ACTIVE=local`

또는 Spring Boot Run Configuration의 Active profiles 항목에 `local`을 입력합니다. 프로파일을 지정하지 않으면 기본값 `prod`가 적용되어 Render용 PostgreSQL 환경변수를 찾기 때문에 로컬 실행이 실패합니다.

Render DB에 직접 붙어서 확인해야 하는 팀원은 `prod` 프로파일과 PostgreSQL 환경변수를 함께 등록해서 실행합니다.

공유 DB에 연결할 때는 `APP_SEED_ENABLED=false`를 유지합니다. 샘플 데이터가 필요한 경우에도 공유 DB가 아니라 개인 DB에서만 `true`로 변경하세요.
