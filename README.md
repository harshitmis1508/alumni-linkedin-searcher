# Alumni LinkedIn Profile Searcher

Spring Boot REST API that searches LinkedIn alumni profiles through the **PhantomBuster API**
and stores them in **PostgreSQL**. No UI - test with Postman or Swagger.

- Java 17+, Spring Boot 3.3, Spring Data JPA, PostgreSQL, springdoc-openapi (Swagger)
- Tests: JUnit 5, Mockito, MockMvc (`mvn test`)

## API

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/alumni/search` | Scrape alumni (university, designation, optional passoutYear), save to DB, return list |
| GET  | `/api/alumni/all` | All saved alumni |

Swagger UI: `http://localhost:8080/swagger-ui.html` - OpenAPI JSON: `/v3/api-docs`

Request:
```json
{ "university": "IIT Bombay", "designation": "Software Engineer", "passoutYear": 2020 }
```
Success response:
```json
{ "status": "success",
  "data": [ { "name": "John Doe", "currentRole": "Software Engineer", "university": "IIT Bombay",
              "location": "New York, NY", "linkedinHeadline": "Software Engineer at XYZ",
              "passoutYear": 2020, "linkedinUrl": "https://www.linkedin.com/in/..." } ] }
```
Error response: `{ "status": "error", "message": "Validation failed", "errors": ["university: university is required"] }`

| Situation | HTTP |
|-----------|------|
| Blank university/designation, bad year, malformed JSON | 400 |
| Wrong content type / method / URL | 415 / 405 / 404 |
| PhantomBuster API error / misconfigured | 502 |
| PhantomBuster agent timeout | 504 |
| Anything unexpected | 500 |

## Run locally (macOS)

### Option A - everything in Docker (no Java/Maven needed)
```bash
docker compose up --build
```
App on http://localhost:8080 , Postgres on localhost:5432.

### Option B - app on your machine, DB in Docker
```bash
brew install openjdk@17 maven      # once
docker compose up -d postgres
mvn clean install                  # compiles + runs tests
mvn spring-boot:run
```

## Mock mode vs real PhantomBuster

`PHANTOMBUSTER_MOCK_ENABLED=true` (default) -> `MockAlumniDataProvider` returns deterministic sample
profiles so the full flow (API -> DB) works without a PhantomBuster account.

For real scraping:
1. Create a PhantomBuster account, add the **LinkedIn Search Export** phantom, note its **Agent ID**.
2. Copy your **API key** (PhantomBuster -> Workspace settings) and your LinkedIn **li_at** session cookie.
3. Run with:
```bash
export PHANTOMBUSTER_MOCK_ENABLED=false
export PHANTOMBUSTER_API_KEY=xxxx
export PHANTOMBUSTER_AGENT_ID=1234567890
export LINKEDIN_SESSION_COOKIE=AQE...        # optional if already saved in the agent
mvn spring-boot:run
```
Flow: `POST /agents/launch` -> poll `GET /containers/fetch-output` until `finished` ->
`GET /containers/fetch-result-object` -> parse -> save.
If your phantom's output field names differ, only `PhantomBusterResponseParser` needs a tweak.

## Environment variables

| Variable | Default |
|----------|---------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/alumni_db` |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `postgres` |
| `PORT` | `8080` |
| `PHANTOMBUSTER_MOCK_ENABLED` | `true` |
| `PHANTOMBUSTER_API_KEY`, `PHANTOMBUSTER_AGENT_ID`, `LINKEDIN_SESSION_COOKIE` | empty |
| `PHANTOMBUSTER_MAX_PROFILES` | `10` |
| `PHANTOMBUSTER_POLL_INTERVAL_MS` / `PHANTOMBUSTER_TIMEOUT_SECONDS` | `5000` / `180` |

## Design

```
controller  -> thin REST layer + validation
service     -> orchestration: fetch -> de-duplicate -> upsert -> map
provider    -> Strategy: AlumniDataProvider (PhantomBusterAlumniDataProvider | MockAlumniDataProvider)
              PhantomBusterClient (HTTP), LinkedInSearchUrlBuilder, PhantomBusterResponseParser
mapper      -> entity/candidate/DTO conversion
exception   -> custom exceptions + @RestControllerAdvice
```
Patterns: Strategy (provider switch via config), Adapter (PhantomBusterClient/Parser isolate the external API),
DTO, Repository, Dependency Injection, uniform response envelope.

## Assumptions
- LinkedIn does not expose graduation year in search results, so `passoutYear` = value found in the scraped data,
  else the year in the request, else `null`.
- `university` in the response is the searched university.
- Profiles are de-duplicated by LinkedIn URL (re-searching updates existing rows).
- Scraping LinkedIn may violate its Terms of Service - use responsibly, for this assignment only.

## Postman
Import `postman/Alumni-Searcher.postman_collection.json`.
