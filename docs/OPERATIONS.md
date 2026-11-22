# Operations

## Environment configuration

Keep Maven, Java 17, the existing Spotify-generated client and Jackson 2 integration. The frontend uses Next.js 16 and React 19. All browser requests continue through Next.js API routes.

Backend `stage` and `prod` profile groups select `postgres,prod-spotify,prod-openai,prod-kofi,prod-mail`. Local development uses `h2,stub-spotify,stub-openai,stub-kofi,stub-mail,local`. The `container-local` group selects the PostgreSQL stub stack on port 3002; `demo` selects the same resources with fictional fixtures and port 3008. For an H2 demo, add `demo-data` to the local profile set. Fixtures require both `local` and `stub-spotify`, and explicitly reject `stage`/`prod`.

Frontend `FRONTEND_PROFILE` is `local`, `demo`, `stage`, or `prod`. It is required for a production server and defaults to `local` during development. Public topology may be overridden through `PUBLIC_BACKEND_URL`, `CSRF_COOKIE_NAME`, and `KOFI_URL`; the internal proxy uses `LOCAL_BACKEND_URL`. `/api/config` exposes only public configuration. Invalid configuration fails visibly instead of using blank values. `BACKEND_REQUEST_TIMEOUT_MS` defaults to 60000; browser operation timeouts remain independent.

Backend shared application settings bind through validated `AppProperties`. Stable URLs, cookie policies, database name/address, JVM limits and export schedule live in committed profile/Compose files. Select the environment and supply credentials plus owner Spotify IDs; individual topology variables are no longer required. For exceptional deployments, standard Spring properties such as `APP_DEFAULT_SUCCESS_URL`, `APP_LOGIN_URL`, `APP_CSRF_COOKIE_NAME` and `SPRING_DATASOURCE_URL` can override profile values. Add explicit Compose overrides when forwarding these into containers.

## Administration

`/admin` is separate from the ordinary Spotify application. Production users start with no console access. Configure comma-separated **Spotify account emails** in `CONSOLE_OWNER_EMAILS` to bootstrap owners at login. Matching trims whitespace and ignores case; missing emails never grant owner access. The application requests `user-read-email`, so existing users need to sign in again to provide it. [Spotify documents this email as unverified](https://developer.spotify.com/documentation/web-api/reference/get-current-users-profile); bootstrap deliberately matches it directly, without email confirmation. Sessions and OAuth credentials remain keyed by Spotify ID.

- `NONE`: no console access; ordinary application features still work.
- `ADMIN`: inspect users/GPT balances, development-access submissions, runtime settings and audit history.
- `OWNER`: administrator access plus role and runtime-setting changes.

Owners cannot change their own role through the console. Configured owner accounts are managed through deployment configuration, based on their email from the latest Spotify login. Removing an email stops future bootstrapping; another owner must revoke its already persisted role. Role checks query the database on every administrative request, so demotion is effective for existing sessions. There is no console-access application/approval workflow.

Development-access records retain their existing **email delivery** statuses (`PENDING`, `SENT`, `FAILED`). Grant Spotify development access separately in Spotify's dashboard. The console does not automatically approve access, resend mail, delete users, edit artists or adjust balances.

Admin tables support pagination, sorting, search, saved column widths, keyboard resizing, overflow inspection, silent refresh and mobile navigation. Below 1100px, rows become labeled records; phone records span the available width without horizontal scrolling. Audit history adds exact search, field selection and inclusive UTC calendar-day filtering. Revisiting admin tabs performs reads only; it never triggers genre enrichment.

## Responsive application pages

Below 1100px, the main and public shared artist lists use full-width records with local search, button sorting, fully visible genres and pagination. CSV exports still include the full list. Account navigation uses buttons; phone settings, donation, login, legal pages, notifications, cookie notices and access-request dialogs fit the available width. Loading uses the same record and owner-header components as loaded content. These presentation controls never call the enrichment endpoint.

## Runtime settings

Explicitly allowed settings are:

| Key | Default | Meaning |
|---|---|---|
| `app.initial-user-gpt-usages` | 10 | Credits for newly created users |
| `app.donation-credit-price` | 0.1 | Donation amount per credit; decimal arithmetic, rounded down to whole credits |
| `app.share-lifetime-days` | 30 | Expiry for created/renewed links |
| `app.retention.audit-days` | 90 | Audit record retention |
| `app.retention.audit-cron` | `0 30 3 * * *` | Cleanup schedule, UTC |
| `app.console.max-page-size` | 100 | Server cap on admin list page size |

Owners can save overrides and reset individual fields to deployment defaults. A batch with any invalid value is rejected atomically. Administrative changes record actor, target, timestamp and before/after values in `audit_log`. Settings are read from committed database state rather than a node-local cache, including across backend instances. A changed cleanup cron takes effect when the scheduler next calculates an execution time.

Existing user balances, the global GPT budget and existing share expiry dates are not reset by policy changes. They remain application data managed by their existing transactional services. Credentials, OAuth endpoints and cookie security are not editable settings.

## Persistent authentication

Flyway migrations add `spring_session`, `spring_session_attributes`, and `oauth2_authorized_client`. The session cookie remains a browser-session cookie, with a default server inactivity timeout of 12 hours; override `SERVER_SERVLET_SESSION_TIMEOUT` if required. Cookie names/domains must agree across frontend and backend.

Application-owned principal fields are serializable; generated Spotify models are reconstructed for responses. The principal name is the stable Spotify ID. Spotify access and refresh tokens are stored through JDBC authorized-client services, and expired tokens can refresh after a restart. Logout removes the current session and its stored authorized client; account deletion removes that user's sessions and credentials. Database storage and backups now include OAuth credentials and should retain the same restricted access as other application secrets.

Existing in-memory sessions require a fresh login when this change is first deployed. Apply migrations before starting the new application. `V100009__add_spotify_user_email.sql` adds a nullable email column; existing users populate it at their next login. Replace `CONSOLE_OWNER_SPOTIFY_IDS` with `CONSOLE_OWNER_EMAILS` in deployment environments. Keep existing applied migration files unchanged.

## Docker Compose

Base Compose uses an isolated backend network and explicit service environment variables; the frontend receives no database, mail, Spotify or OpenAI credentials. Local Compose no longer requires production networks.

```powershell
docker compose --env-file templates/docker/.test-env -f templates/docker/docker-compose.yml -f templates/docker/docker-compose.ports-local.yml up -d
```

Run the fictional demo (frontend 3008, backend 8088, Spotify stub 8100):

```powershell
docker compose --project-name artist-insight-demo --env-file templates/docker/.test-env -f templates/docker/docker-compose.yml -f templates/docker/docker-compose.demo.yml up -d --wait
```

Use **Developer tools** to sign in as Local owner, admin or user. The demo includes 120 invented listeners, 96 access requests, 240 audit events and 36 fictional artists, with bundled artwork avatars, a few synthetic portraits and some accounts without a picture. Artwork prompts are recorded in `docs/demo-avatar-prompts.json`. Seeding is idempotent and preserves edited records. No real Spotify or OpenAI calls are used. Loading previews retain the loaded content geometry.

Local published ports are backend 8080, Spotify stub 8100, PostgreSQL 5432, frontend 3002. Add `docker-compose.debug.yml` explicitly for a localhost-only debugger on 5005. Normal containers do not enable JDWP.

Production adds `docker-compose.prodenv.yml`. This preserves the external `prodenv-shared-internal` and `artist-insight-shared` network aliases and attaches the exporter to the external backup staging volume. Set credentials, profile and owner emails explicitly. Public URLs and cookies come from the selected profile. Resource limits are committed in Compose; adjust these files using representative enrichment traffic.

Example selectors (supply credentials separately):

```dotenv
COMPOSE_PROJECT_NAME=artist-insight-prod
SPRING_PROFILES_ACTIVE=prod
FRONTEND_PROFILE=prod
CONSOLE_OWNER_EMAILS=owner@example.com
```

Production network aliases, existing database volume names and the `app` Compose service name are preserved. Include the production override in deployment webhook configuration before updating an existing deployment. Pin `IMAGE_TAG` to a completed release when a fixed version is required.

## PostgreSQL exports and restore checks

The production override runs an export on startup and daily at 01:00 **UTC**. The schedule is committed in `templates/docker/backup/docker-compose.yml`. Exports use PostgreSQL 16 tools, a custom-format database dump, globals, checksums and metadata. A lock prevents concurrent exports; completed exports replace the current export only after dumping succeeds. The health check detects exports older than 26 hours.

The external volume defaults to `prodenv-backup-staging`; override `BACKUP_STAGING_VOLUME` for another existing backup volume. Exports are separated by `COMPOSE_PROJECT_NAME`. Connect this staging volume to the deployment's backup system for historical/off-host snapshots; the exporter itself retains only the current export.

Using the same environment/Compose file arguments as the deployed stack:

```bash
docker compose ... exec postgres-export sh /opt/backup/export-postgres.sh
docker compose ... logs --tail=100 postgres-export
docker compose ... exec -e RESTORE_DATABASE=artist_insight_restore_check postgres-export sh /opt/backup/restore-postgres.sh /backup-staging/artist-insight-prod/postgres
```

The restore helper validates checksums and archive readability, creates a separate database, restores its schema/data and queries the user table. It refuses to restore over the application database. The temporary validation database is left for review; remove it explicitly afterwards. This checks application data restoration; review `globals.sql` separately when recovering cluster roles onto a new server.

After an unclean container termination, inspect the export log and verify no export is active before manually clearing a stale `.export-in-progress` directory. Export files are published in a directory swap; the snapshot consumer should validate checksums and retry if it encounters publication in progress.

## Releases and verification

Releases compute a version first, build/publish backend and frontend images independently, then publish the Git tag only after both image jobs succeed. Maven's `git.tag` override and frontend build arguments align metadata with that version. Production retains its manual approval environment. Optional ntfy deployment notifications require repository variable `ENABLE_DEPLOY_NOTIFICATIONS=true` plus `NTFY_URL`, `NTFY_TOPIC`, `NTFY_TOKEN` secrets.

CI explicitly runs frontend ESLint/build/Vitest and uploads backend Surefire/Failsafe reports even on failure. Opt-in smoke tests still run through `mvn -B -P build-docker-image,smoke-tests verify`.

```powershell
mvn test
npm run lint --prefix frontend
npm test --prefix frontend
npm run build --prefix frontend
```

Regression coverage includes console role boundaries, CSRF, atomic settings validation, UTC audit filtering, JDBC session reload, Spotify refresh-token persistence, logout, account deletion and demo fixture idempotency/profile guards. Verify PostgreSQL migrations, export/restore and authenticated browser behavior when changing deployment/authentication code.
