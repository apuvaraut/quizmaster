# QuizMaster - AI-powered Quiz Application

Full-stack quiz platform where students take quizzes and admins create them manually or with AI.

**Tech:** HTML, CSS, JavaScript | Core Java 17, Spring Boot, Spring Data JPA, Spring Security (JWT) | MySQL | Claude API

## Features
- Register / login with BCrypt-hashed passwords and JWT authentication
- Role-based authorization: USER (take quizzes) and ADMIN (manage quizzes)
- Admin can **generate a whole quiz from a topic using AI**
- Students get **AI explanations for the questions they got wrong**
- Scores are calculated on the server (correct answers never reach the browser)
- Attempt history and leaderboard

## Run locally
1. Install Java 17, Maven and MySQL. Start MySQL (the database `quizdb` is created automatically).
2. Set environment variables (or edit `application.properties`):
   - `DB_USER`, `DB_PASSWORD` (defaults: root / root)
   - `ANTHROPIC_API_KEY` (from console.anthropic.com, needed only for the AI features)
3. Run: `mvn spring-boot:run`
4. Open http://localhost:8080

Default admin: `admin@quiz.com` / `admin123` (change with `ADMIN_PASSWORD`). A sample Java quiz is added on first start.

## Project structure
```
controller/  -> REST endpoints (Auth, Quiz, Admin, Ai)
service/     -> AiService (calls Claude API)
repository/  -> Spring Data JPA interfaces
model/       -> JPA entities: User, Quiz, Question, Result
security/    -> JwtUtil, JwtFilter, SecurityConfig
dto/         -> request/response records
config/      -> DataInitializer, GlobalExceptionHandler
static/      -> HTML, CSS, JS frontend
```

## API
| Method | URL | Access |
|---|---|---|
| POST | /api/auth/register, /api/auth/login | public |
| GET | /api/quizzes | logged in |
| GET | /api/quizzes/{id}/questions | logged in |
| POST | /api/quizzes/{id}/submit | logged in |
| GET | /api/results/me, /api/leaderboard | logged in |
| POST | /api/ai/explain | logged in |
| POST | /api/admin/quizzes, /api/admin/quizzes/generate | ADMIN |
| POST | /api/admin/quizzes/{id}/questions | ADMIN |
| DELETE | /api/admin/quizzes/{id} | ADMIN |

## Deploy (Docker, e.g. Railway or Render)
1. Push this folder to a GitHub repo.
2. Create a MySQL database on your host (Railway has a MySQL plugin).
3. Create a web service from the repo (it detects the Dockerfile) and set these variables:
   - `DB_URL` = `jdbc:mysql://<host>:<port>/<database>`
   - `DB_USER`, `DB_PASSWORD`
   - `JWT_SECRET` = any random string of 32+ characters
   - `ADMIN_PASSWORD` = a strong password
   - `ANTHROPIC_API_KEY` = your key
4. Deploy. The app reads the `PORT` variable automatically.

## How it works (for interviews)
1. **Login:** password is checked with BCrypt, server returns a JWT containing email and role. The browser stores it and sends `Authorization: Bearer <token>` on every call.
2. **JwtFilter** validates the token on each request and tells Spring Security who the user is. `SecurityConfig` decides which URLs need login or the ADMIN role.
3. **Taking a quiz:** questions are sent without answers. On submit, the server compares answers, saves a `Result` row and returns the review.
4. **AI:** `AiService` sends a prompt to the Claude API using Java's `HttpClient`. For quiz generation the prompt asks for a JSON array, which Jackson converts to Java objects that are saved through JPA.

