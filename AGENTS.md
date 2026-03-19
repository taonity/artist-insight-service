# AGENTS.md

## Architecture

Multi-module Maven monorepo (Spring Boot 4 / Kotlin backend + Next.js 14 TypeScript frontend).

- **`artist-insight-service/`** — Main backend. Kotlin, Spring Boot 4, JPA/Hibernate, Spring Security OAuth2 (Spotify login). Package-per-feature layout under `org.taonity.artistinsightservice`: `followings/`, `artist/`, `share/`, `user/`, `donation/kofi/`, `devaccess/`, `settings/`, `integration/{spotify,openai}/`, `security/`, `infrastructure/`, `health/`, `advisory/`, `local/`.
- **`spotify-client/`** — OpenAPI-generated Java models from the Spotify Web API spec (generated via `openapi-generator-maven-plugin`, not hand-written).
- **`spotify-contracts/`** / **`openai-contracts/`** — Spring Cloud Contract stubs (Groovy DSL in `src/test/resources/contracts/`). These produce `-stubs.jar` artifacts consumed by `artist-insight-service` for local and test stub runners.
- **`frontend/`** — Next.js app (`src/app/` App Router). All backend calls go through Next.js API routes (`src/app/api/`) which proxy to the backend via `src/lib/backend.ts` using `fetchFromBackend()`.
- **`templates/docker/`** — Docker Compose templates with Flyway migrations in `flyway/sql/tables/`.

### Key data flow

1. User logs in via Spotify OAuth2 → `security/service/OAuth2UserPersistenceService` persists/updates user.
2. Frontend calls `/api/followings` → Next.js route → backend `/followings` → `SpotifyService` fetches from Spotify API → artists validated via `ValidatedArtistObject` → enriched with DB/OpenAI genres → returned with advisories.
3. Genre enrichment: if Spotify provides no genres, system checks DB first; if not found, calls OpenAI GPT-4 (rate-limited by per-user and global GPT usage counters). Ko-Fi donations top up user GPT usages.
4. Share links: users create share codes; public `/share/{shareCode}` endpoint serves artist lists without auth.

## Build & Run

```bash
# Full build (from root)
mvn clean install

# Run backend locally (IntelliJ or CLI)
mvn spring-boot:run '-Dspring-boot.run.jvmArguments="-Dspring.profiles.active=h2,stub-spotify,stub-openai,stub-kofi,local"'

# Run frontend
cd frontend && npm install && npm run dev
```

Backend runs on port **9016**, frontend on **3000**.

### Profile system (one per resource group)

| Resource | Local/stub            | Production         |
|----------|-----------------------|--------------------|
| DB       | `h2`                  | `postgres`         |
| Spotify  | `stub-spotify`        | `prod-spotify`     |
| OpenAI   | `stub-openai`         | `prod-openai`      |
| Ko-Fi    | `stub-kofi`           | `prod-kofi`        |
| General  | `local`               | *(not needed)*     |

Local set: `h2,stub-spotify,stub-openai,stub-kofi,local`. Stubs use Spring Cloud Contract stub runner (classpath mode).

## Conventions & Patterns

- **Package-per-feature**: each feature has `controller/`, `service/`, `dto/`, `entity/`, `repository/`, `exception/` sub-packages. Follow this layout when adding features.
- **Logging**: use `mu.KotlinLogging` (`private val LOGGER = KotlinLogging.logger {}`), placed in a `companion object`.
- **Controller pattern**: `@RestController`, inject services, use `@AuthenticationPrincipal principal: SpotifyUserPrincipal` for auth. **Do NOT add manual entry-point logging** — `ControllerLoggingAspect` (AOP) automatically logs every `@RestController` method invocation with HTTP method, path, class, method name, and execution time.
- **Advisory system**: backend attaches `Advisory` enums to responses (via `ResponseAttachments`) to surface warnings/errors to the frontend (e.g., rate limits, timeouts).
- **CSRF**: SPA pattern with `CookieCsrfTokenRepository` + `SpaCsrfTokenRequestHandler`. Frontend reads CSRF cookie and sends `X-XSRF-TOKEN` header on mutating requests.
- **Frontend API proxy**: every backend call is proxied through Next.js API routes in `src/app/api/`. Never call the backend directly from client components.
- **Models**: `spotify-client` models are auto-generated — edit the OpenAPI spec reference, not the generated code.
- **DB migrations**: Flyway SQL scripts in `templates/docker/flyway/sql/tables/` (naming: `V100000__description.sql`). H2 profile uses `ddl-auto: none` with Flyway disabled — schema comes from `spring.sql.init`.

## Testing

- **MVC integration tests**: extend `ControllerTestsBaseClass` (in `src/test/kotlin/.../mvc/`), which provides `authorizeOAuth2()` for simulating full OAuth2 flow via MockMvc. Uses `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("h2")`.
- **Contract stubs**: tests use `@AutoConfigureStubRunner` with `stubs-mode: CLASSPATH` to wire Spotify/OpenAI stubs.
- **Test data**: `src/test/resources/sql/test-data.sql` + `clear-data.sql` loaded via `@Sql` annotations.
- Run tests: `mvn test` from root or from `artist-insight-service/`.

## External Integrations

- **Spotify API**: OAuth2 authorization code flow + client credentials. Rest clients configured per profile. Models from `spotify-client` module.
- **OpenAI**: GPT-4 for genre inference. Service in `integration/openai/`. Stub contract in `openai-contracts/`.
- **Ko-Fi**: Webhook at `/callback/kofi` (CSRF-exempt). Parses donation data, extracts Spotify ID from message, tops up GPT usages. Local stub at `/N4N11KVW3E` (profile `stub-kofi`).

