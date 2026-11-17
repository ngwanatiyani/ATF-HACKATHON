# RoutePulse

RoutePulse is a community transport reporting platform for commuters who rely on taxis and buses. A commuter can report a delay, long queue, disruption, route change, or service that is not running. Other commuters can then see the latest route conditions before travelling.

## What the application does

- Shows a public board of routes and recent commuter reports.
- Lets commuters create an account and sign in with a phone number and password.
- Lets signed-in commuters report route conditions and expected waiting time.
- Calculates a reliability score for every route from its five newest reports.
- Refreshes the public board every 15 seconds.
- Optionally uses Exa to find published road disruptions for a route.

## Reliability scoring

Every route starts at 100. The five newest reports reduce its score:

| Report | Penalty |
| --- | ---: |
| Delay | 20 |
| Long queue | 15 |
| Disruption | 35 |
| Route change | 30 |
| Not running | 50 |

A score of 70 or higher is **High**, 40–69 is **Medium**, and below 40 is **Low**.

## Technology

- React 19 and Vite 6
- Java 21 and Spring Boot 4
- Spring Security with HTTP Basic authentication
- Spring Data JPA and MySQL 8
- Optional Exa road intelligence

## Prerequisites

Install or start the following before running RoutePulse:

- Java Development Kit (JDK) 21
- Node.js 18 or newer with npm
- MySQL 8 or another compatible MySQL server

The repository includes Maven Wrapper, so a separate Maven installation is not required.

## Local setup

### 1. Clone the repository

```powershell
git clone https://github.com/ngwanatiyani/ATF-HACKATHON.git
cd ATF-HACKATHON
```

### 2. Configure MySQL

Make sure MySQL is running and that your account can create or access the `atfdabase` database. Copy the example configuration:

```powershell
Copy-Item database.local.properties.example database.local.properties
```

Then edit `database.local.properties` with your local MySQL credentials:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

The application creates `atfdabase` when the configured MySQL account has permission to create databases. You can also override the connection without a file by setting these environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

`database.local.properties` is ignored by Git and must not be committed.

### 3. Configure Exa (optional)

The **Check the road** feature uses Exa. Create `exa.local.properties` in the repository root:

```properties
exa.api-key=YOUR_EXA_API_KEY
```

You can use the `EXA_API_KEY` environment variable instead. The application runs without Exa, but road checks will report that the feature is not configured. `exa.local.properties` is ignored by Git and must not be committed.

## Run the application

Start the backend from the repository root:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux, use:

```bash
./mvnw spring-boot:run
```

The API starts at `http://localhost:8081`.

Open a second terminal and start the frontend:

```powershell
cd frontend
npm ci
npm run dev
```

Open **http://localhost:5174/**. Vite proxies local `/api` requests to the Spring Boot API on port 8081.

## Using RoutePulse

1. Open the route board. Reading reports does not require an account.
2. Join with your name, phone number, and password, or sign in with an existing account.
3. Enter a route and select the condition you want to report.
4. Optionally add the waiting time and a short note.
5. Submit the report. The route score and live feed update immediately.
6. If Exa is configured, select **Check the road** after entering a route to find published disruptions and sources.

## API summary

| Method | Endpoint | Authentication | Purpose |
| --- | --- | --- | --- |
| `GET` | `/api/board` | Public | Return route scores and latest reports |
| `POST` | `/api/register` | Public | Register a commuter |
| `GET` | `/api/me` | HTTP Basic | Return the signed-in commuter |
| `POST` | `/api/reports` | HTTP Basic | Submit a route report |
| `POST` | `/api/brief` | HTTP Basic | Check published road information with Exa |

Example registration body:

```json
{
  "name": "Aya",
  "phone": "0712345678",
  "password": "your-password"
}
```

Example report body:

```json
{
  "route": "Khayelitsha - Cape Town CBD",
  "kind": "DELAY",
  "note": "Traffic is moving slowly near the station",
  "minutes": 25
}
```

Valid report kinds are `DELAY`, `QUEUE`, `DISRUPTION`, `CHANGE`, and `CLOSED`.

## Build for production

Build the backend:

```powershell
.\mvnw.cmd package
```

Build the frontend:

```powershell
cd frontend
npm ci
npm run build
```

The frontend output is written to `frontend/dist`.

## Deployment

For Vercel, set the project root to `frontend` and the build command to `npm run build`. Deploy the Spring Boot API and MySQL database separately. The API must be available over HTTPS. On the deployed RoutePulse page, enter the public backend origin in the **API** field at the bottom of the page.

Set production database credentials through `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. Set `EXA_API_KEY` only on the backend when road checks are enabled. Never commit either local properties file.

## Project structure

```text
frontend/                         React and Vite user interface
src/main/java/.../controller/     HTTP API controllers
src/main/java/.../domain/         Member and report entities
src/main/java/.../factory/        Domain object creation
src/main/java/.../helper/         Validation and score penalties
src/main/java/.../repository/     Spring Data repositories
src/main/java/.../request/        API request models
src/main/java/.../response/       API response models
src/main/java/.../service/        Registration, reports, board, and road checks
src/main/resources/               Spring Boot configuration
```

The frontend only calls the API. The backend owns authentication, validation, persistence, scoring, and Exa integration.
