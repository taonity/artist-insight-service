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
- Backend - Kotlin Spring Boot 4 application on Maven
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
| stub-mail    | Email       | Stub mail sender that logs outgoing messages without SMTP delivery |
| prod-mail    | Email       | Production SMTP configuration for sending emails                   |
| local        | No resource | Common local configurations for development                        |

Only one profile from a resource group can be used. For example, the set for the production environment looks like
`postgres,prod-spotify,prod-openai,prod-kofi,prod-mail`, and for local development - `h2,stub-spotify,stub-openai,stub-kofi,stub-mail,local`.

Use IntelliJ to run the backend locally. Add a Run/Debug configuration with Main class `org.taonity.artistinsightservice.MainKt`
and VM options `-Dspring.profiles.active=h2,stub-spotify,stub-openai,stub-kofi,stub-mail,local` and run the backend.

To run it from PS use a command like this:
```bash
mvn spring-boot:run '-Dspring-boot.run.jvmArguments="-Dspring.profiles.active=h2,stub-spotify,stub-openai,stub-kofi,stub-mail,local"'
```

#### Frontend
I recommend opening /frontend directory in Visual Code. Run `npm install`, and then `npm run dev`.

### Docker Compose deployment
Docker Compose runs the backend and frontend in fully stubbed mode, including Spotify and mail.

Run this. These are some shared networks required for production deployment.
```bash
docker network create prodenv-shared-internal
docker network create artist-insight-shared
```
Run this
```bash
# Prepares Docker Compose templates for running. Make sure you are on the last released tag in git.
mvn clean -P build-automation-docker-compose-project compile -DskipTests=true
# Runs Docker Compose template with images from Dockerhub. Make sure you placed all required env vars.
docker compose -f artist-insight-service/target/docker/test/docker-compose.yml up -d
```

Or you can build the images yourself by following the instructions. Run this
```bash
# Builds backend Docker image and prepares Docker Compose templates for running
mvn clean -P build-docker-image,build-automation-docker-compose-project -pl artist-insight-service install -DskipTests=true
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
| LOCAL_BACKEND_URL                    | Frontend | Internal backend URL used by frontend server-side requests          |

### PostgreSQL database ERD diagram

<!-- mermerd-start -->
```mermaid
erDiagram
    app_settings {
        integer global_gpt_usages_left "{NOT_NULL}"
        integer id PK "{NOT_NULL}"
    }

    artist {
        character_varying artist_id PK "{NOT_NULL}"
        character_varying artist_name "{NOT_NULL}"
    }

    artist_genres {
        character_varying artist_id PK,FK "{NOT_NULL}"
        character_varying genre PK "{NOT_NULL}"
    }

    development_access_requests {
        timestamp_with_time_zone created_at 
        character_varying email "{NOT_NULL}"
        uuid id PK "{NOT_NULL}"
        character_varying ip_address 
        text message 
        character_varying status 
        text user_agent 
    }

    shared_link {
        timestamp_with_time_zone created_at "{NOT_NULL}"
        timestamp_with_time_zone expires_at "{NOT_NULL}"
        uuid id PK "{NOT_NULL}"
        character_varying share_code UK "{NOT_NULL}"
        character_varying user_id FK,UK "{NOT_NULL}"
    }

    shared_link_artist {
        character_varying artist_id UK "{NOT_NULL}"
        uuid shared_link_id FK,UK "{NOT_NULL}"
    }

    spotify_user {
        character_varying display_name "{NOT_NULL}"
        integer gpt_usages_left "{NOT_NULL}"
        character_varying spotify_id PK "{NOT_NULL}"
        character_varying token_value "{NOT_NULL}"
    }

    spotify_user_enriched_artists {
        character_varying artist_id PK,FK "{NOT_NULL}"
        character_varying spotify_id PK,FK "{NOT_NULL}"
    }

    artist_genres }o--|| artist : "artist_id"
    spotify_user_enriched_artists }o--|| artist : "artist_id"
    shared_link }o--|| spotify_user : "user_id"
    shared_link_artist }o--|| shared_link : "shared_link_id"
    spotify_user_enriched_artists }o--|| spotify_user : "spotify_id"
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
- consider tests for frontend
- consider simpliying the other side
- fix smoke test env vars
- add mechanism to send dev access request in mock env
- add db table visualiser
- think about alternative for branch-name-like snapshot version
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


