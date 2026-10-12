# Personal Expense & Budget Analyzer

Track income and expenses, set a monthly budget, and see charts and warnings on a dashboard.
**Stack:** Java 17, Spring Boot 3.2, Maven, Spring Data JPA/Hibernate, MySQL 8, HTML/CSS/JS, Chart.js.

## Project structure
```
src/main/java/com/example/expenseanalyzer
├── controller/   REST endpoints (Transaction, Budget, Dashboard)
├── service/      Business logic and calculations
├── repository/   Spring Data JPA interfaces + queries
├── model/        JPA entities: Category, Transaction, Budget
├── dto/          Request/response objects (what travels over JSON)
└── exception/    Custom exception + global JSON error handler
src/main/resources
├── application.properties   DB settings
└── static/                  index.html, style.css, app.js (served by Spring Boot)
```

## Setup
1. Install **JDK 17**, **Maven 3.8+** and **MySQL 8**; make sure MySQL is running.
2. Edit `src/main/resources/application.properties` and set your MySQL `username` / `password`.
   The database `expense_db` and all tables are created automatically on first start.
3. Run:
   ```bash
   mvn spring-boot:run
   ```
4. Open **http://localhost:8080**.

Run the unit tests (budget alerts, calculations; no database needed): `mvn test`

## REST API
| Method | URL | Purpose |
|---|---|---|
| GET | `/api/transactions?keyword=&category=&type=&month=YYYY-MM&sortBy=date\|amount&direction=asc\|desc` | Search, filter, sort |
| GET | `/api/transactions/{id}` | One transaction |
| POST | `/api/transactions` | Add (returns 201) |
| PUT | `/api/transactions/{id}` | Edit |
| DELETE | `/api/transactions/{id}` | Delete (204) |
| GET | `/api/categories` | Category names |
| GET / PUT / DELETE | `/api/budgets/{YYYY-MM}` | Read / create-or-update / remove a month's budget |
| GET | `/api/dashboard?month=YYYY-MM` | Totals, budget status, averages, chart data |

Example:
```bash
curl -X POST localhost:8080/api/transactions -H "Content-Type: application/json" \
  -d '{"amount":250.50,"type":"EXPENSE","categoryName":"Food","date":"2026-10-03","description":"Groceries"}'
curl -X PUT localhost:8080/api/budgets/2026-10 -H "Content-Type: application/json" -d '{"amount":1000}'
```
Errors come back as JSON, e.g. `400 {"message":"Validation failed","errors":{"amount":"Amount must be greater than 0"}}`.

## Business rules
- Budget used % = expenses ÷ budget × 100.
- **Warning** when expenses are above 80% of the budget; **Exceeded** when expenses are above the budget.
- Average daily expense = expenses ÷ days elapsed (current month), ÷ days in month (past months).
- Trend chart shows the selected month and the 5 months before it.

## Use it on your phone over mobile data (no Wi-Fi)
Your phone must reach the app through the internet, so the app needs a public URL. Two options:

### Option A: Host it online (works 24/7, computer can be off) — recommended
1. Push this project to a GitHub repository.
2. Create a **MySQL database** on a host such as Railway, Aiven or Render's partners and note its host, port, user, password and database name.
3. Create a **Web Service** from the repo on Render, Railway or Fly.io. They detect the `Dockerfile` automatically.
4. Add these environment variables to the service:
   ```
   DB_URL=jdbc:mysql://<host>:<port>/<database>?useSSL=true&serverTimezone=UTC
   DB_USERNAME=<user>
   DB_PASSWORD=<password>
   ```
   (`PORT` is set by the platform.) Tables are created automatically on first start.
5. Open the URL the platform gives you (https://...) on your phone, then use "Add to Home Screen".

### Option B: Quick tunnel from your own computer (computer must stay on)
Run the app locally (`mvn spring-boot:run`), then in another terminal use a tunnel tool:
```bash
cloudflared tunnel --url http://localhost:8080     # or:  ngrok http 8080
```
It prints a public https URL you can open on mobile data.

### Security warning
This app has **no login**. Anyone who knows the public URL can read and change your finances.
Keep the URL private, or add authentication before sharing it (Spring Security basic auth is a small change).
