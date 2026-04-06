## artist-insight
A service that allows you to fetch your Spotify followings and share them with others.

### Features
- Fetch user followings and show them with genres in a list
- Download the followings list as a CSV file
- Share followings page with a share link
- Define missing artist genres using OpenAI
- Log in with a Spotify user using OAuth

<img src="images/site-user-page-v2.png" width="600" />

### Roadmap
- Add user roles and admin panel
- Add more ways to donate to the service
- Add selectors for grouping artists by details like genre
- Review site design
- Adopt site for mobile devices
- Add Liked Songs details list
- Add more music service integrations

### Project structure
- Backend - Java Spring Boot application on Maven
- Frontend - TypeScript Next.js with client and server parts
- Deployment - Docker Compose template with backend, frontend, Postgres, and Flyway

### How to run
#### Backend
The project has all its resources stubbed for the most comfortable local development. It has a list of profiles for any running requirements.

| Profile      | Resource    | Description                                                        |
|--------------|-------------|--------------------------------------------------------------------|
| stub-spotify | Spotify     | Spring Boot Contract Stub with stubbed endpoints                   |
| prod-spotify | Spotify     | Production configuration that requires Spotify project credentials |
| stub-openai  | OpenAI      | Spring Boot Contract Stub with stubbed endpoints                   |
| prod-openai  | OpenAI      | Production configuration that requires OpenAI project api key      |
| h2           | Postgres    | H2 in-memory database                                              |
| postgres     | Postgres    | Regular Postgres configuration for a local or remote instance      |
| stub-kofi    | Ko-Fi       | Stub Spring MVC endpoint that generates Ko-Fi webhook              |
| prod-kofi    | Ko-Fi       | Production configuration that requires verification token          |
| local        | No resource | Common local configurations for development                        |

Only one profile from a resource group can be used. For example, the set for the production environment looks like
`postgres,prod-spotify,prod-openai`, and for local development - `h2,stub-spotify,stub-openai,stub-kofi,local`.

Use IntelliJ to run the backend locally. Add a Run/Debug configuration with Main class `org.taonity.artistinsightservice.MainKt`
and VM options `-Dspring.profiles.active=h2,stub-spotify,stub-openai,stub-kofi,local` and run the backend.

To run it from PS use a command like this:
```bash
mvn spring-boot:run '-Dspring-boot.run.jvmArguments="-Dspring.profiles.active=h2,stub-spotify,stub-openai,stub-kofi,local"'
```

#### Frontend
I recommend opening /frontend directory in Visual Code. Run `npm insatll`, and then `npm run dev`.

### Docker Compose deployment
Docker Compose runs the backend and frontend with all stubs except the Spotify one. The Spotify stub is not workable there yet.
Therefore, the production configs are used, and credentials should be provided.

Run this. These are some shared networks required for production deployment.
```bash
docker network create prodenv-shared-internal
docker network artist-insight-shared
```
Run this
```bash
# Prepares Docker Compose templates for running. Make sure you are on the last released tag in git.
mvn clean -P automation compile -DskipTests=true
# Runs Docker Compose template with images from Dockerhub. Make sure you placed all required env vars.
docker compose -f artist-insight-service/target/docker/test/docker-compose.yml up -d
```

Or you can build the images yourself by following the instructions. Run this
```bash
# Builds backend Docker image and prepares Docker Compose templates for running
mvn clean -P docker,automation -pl artist-insight-service install -DskipTests=true
# Installs npm modules for Next.js frontend
npm install --prefix frontend/
# Builds the latest image of the frontend app using Dockerfile
docker build -t generaltao725/artist-insight-frontend -t generaltao725/artist-insight-frontend frontend/
# Runs Docker Compose template with images from Dockerhub. Make sure you placed all required env vars.
docker compose -f artist-insight-service/target/docker/test/docker-compose.yml up -d
```

### Environment variable
The project requires a set of environment variables to be configured for some services, depending on which profile set you use.

| Env var                              | Service  | Description                                                         |
|--------------------------------------|----------|---------------------------------------------------------------------|
| COMPOSE_PROJECT_NAME                 | Postgres | Name for Docker Compose project                                     |
| POSTGRES_USER                        | Postgres | Used by Flyway                                                      |
| POSTGRES_PASSWORD                    | Postgres | Used by Flyway                                                      |
| POSTGRES_DB                          | Postgres | DB name                                                             |
| POSTGRES_APP_USER                    | Postgres | Used by backend                                                     |
| POSTGRES_APP_PASSWORD                | Postgres | Used by backend                                                     |
| POSTGRES_PORT                        | Postgres |                                                                     |
| POSTGRES_ADDRESS                     | Postgres |                                                                     |
| SPOTIFY_CLIENT_ID                    | Backend  | Taken from Spotify developer dashboard                              |
| SPOTIFY_CLIENT_SECRET                | Backend  | Taken from Spotify developer dashboard                              |
| DEFAULT_SUCCESS_URL                  | Backend  | Redirect for a user after a successful login                        |
| LOGIN_URL                            | Backend  | Redirect for a user after a failed login                            |
| SERVER_SERVLET_SESSION_COOKIE_DOMAIN | Backend  | Base domain for frontend and backend                                |
| SERVER_SERVLET_SESSION_COOKIE_NAME   | Backend  | Cookie name for frontend and backend, for ex. JSESSIONID-STAGE      |
| CSRF_COOKIE_NAME                     | Backend  | CSRF cookie name for frontend and backend, for ex. XSRF-TOKEN-STAGE |
| ADMIN_EMAIL                          | Backend  | Admin email address for receiving development access requests       |
| ORGANISATION_EMAIL                   | Backend  | Gmail account used for sending emails via SMTP                      |
| ORGANISATION_EMAIL_PASSWORD          | Backend  | Password/app password for the organisation Gmail account            |
| OPENAI_API_KEY                       | Backend  | Taken from OpenAI platform organisation                             |
| KOFI_VERIFICATION_TOKEN              | Backend  | Taken from Ko-Fi API webhook settings                               |
| SPRING_PROFILES_ACTIVE               | Backend  | See the table in [backend](#backend)                                |
| PUBLIC_BACKEND_URL                   | Frontend | Redirect to backend for OAuth initiation                            |

### PostgreSQL database ERD diagram

<!-- mermerd-start -->
```mermaid
erDiagram
    ...
```
<!-- mermerd-end -->

### Prod deployment
The service is deployed in a cheap VPS. [taonity/docker-webhook](https://github.com/taonity/docker-webhook) is used for
deployment in my custom production environment - [taonity/prodenv](https://github.com/taonity/prodenv/tree/defr-prodenv).

#### Grafana dashboard
The project supports Grafana [dashboard](https://github.com/taonity/prodenv/blob/defr-prodenv/logging/grafana/provisioning/dashboards/artist-insight-dashboard.json) with Prometheus

<img src="images/dashboard.png" width="600" />

### Tech debts
- invesitgate auth metris missmatch in dashboard
- Find a way forward to the login page early
- add db table visualiser
- Create PR to disable logging https://github.com/spring-cloud/spring-cloud-contract/blob/44c634d0e9e82515d2fba66343530eb7d2ba8223/spring-cloud-contract-stub-runner/src/main/java/org/springframework/cloud/contract/stubrunner/provider/wiremock/WireMockHttpServerStub.java#L130
- Wait for https://github.com/spring-cloud/spring-cloud-contract/pull/2092

#### Notes
Servlet filters order:
OrderedCharacterEncodingFilter -2147483648
ServerHttpObservationFilter -2147483647
AllRequestsLoggingFilter -2147483646
OrderedFormContentFilter -9900
OrderedRequestContextFilter -105
DelegatingFilterProxyRegistrationBean

#### Error notes

org.springframework.security.oauth2.core.OAuth2AuthenticationException: [invalid_user_info_response] An error occurred while attempting to retrieve the UserInfo Resource: I/O error on GET request for "https://api.spotify.com/v1/me": Network is unreachable
org.springframework.security.oauth2.core.OAuth2AuthenticationException: [authorization_request_not_found] 


integrate that action for db diagram creation into your release flow https://github.com/taonity/mermerd-flyway-github-action. the action should fork from release stage and run in parallel with deploy ones. make sure the new commit with the readme update to not trigger any ci cd workflows. do necessary check. ask if somthing, or mention if something should be alligned on the onther end

Things to align on the action side (mermerd-flyway-github-action):

Placeholder handling — The init scripts (V000001, V000002) use custom Flyway placeholders ({{ app_user }}, etc.) with non-default prefix/suffix. I excluded them since they only create users/grant privileges and don't define tables. If you ever want to include init scripts, the action would need to support custom FLYWAY_PLACEHOLDER_PREFIX/FLYWAY_PLACEHOLDER_SUFFIX env vars.

First run behavior — The action docs say it "detects changes by comparing against the previous commit." On the very first run after merging this, there won't be a previous migration change to diff against. You may need to verify the action handles the initial population case (i.e., generates the diagram even when migration files aren't newly changed). If it doesn't, a workflow_dispatch trigger or a no-op migration file change would bootstrap it.

README markers — Already present in README.md:116-121, no changes needed.