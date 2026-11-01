# RoomSync – Bill Split for Roommates

A simple Spring Boot 3 + Thymeleaf + MySQL web app for sharing household expenses. There is **no login**. The dashboard opens at `http://localhost:8080`.

## What you can do

- Add and manage roommates
- Add expenses and split them equally or with custom amounts
- Create weekly/monthly recurring bills (auto-generate expenses when due)
- Record full or partial payments
- See paid, pending and net balances

## Requirements

- Java 21 or newer (Java 25 is fine)
- Maven Wrapper is included (`mvnw` / `mvnw.cmd`) — you do not need Maven installed
- MySQL 8 running locally

## MySQL setup

1. Start MySQL.
2. Open `src/main/resources/application.properties` and set your username/password:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

The app creates the `roomsync` database automatically (`createDatabaseIfNotExist=true`).

If you previously ran an older RoomSync version, drop the old schema once:

```sql
DROP DATABASE IF EXISTS roomsync;
```

If MySQL is installed but not running on Windows:

```powershell
Get-Service *mysql*
Start-Service MySQL84
```

(The service name may differ on your machine.)

## Run the app

From the `RoomSync` folder:

```powershell
.\mvnw.cmd spring-boot:run
```

Then open [http://localhost:8080](http://localhost:8080).

## Suggested demo flow

1. **Roommates** → add 3–4 people.
2. **Expenses** → add an electricity bill, paid by one roommate, equal split.
3. **Payments** → record a partial payment for someone who still owes money.
4. **Recurring Bills** → add monthly rent. If the next due date is today or earlier, an expense is generated on startup.
5. **Balances** → check who paid the original bill vs who still owes their share.
6. **Dashboard** → summary cards and monthly chart are loaded from MySQL, not hardcoded.

## Project layout

```
com.roomsync
  controller   web pages and form posts
  model        Roommate, Expense, ExpenseShare, Payment, RecurringBill
  repository   Spring Data JPA
  service      split, payment and balance logic
  RoomSyncApplication.java
```

Money values use `BigDecimal`. Equal split remainder (rounding) is added to the first roommate so the shares always match the bill total.
