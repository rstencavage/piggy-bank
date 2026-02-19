# Piggy Bank

Piggy Bank is a full-stack banking web application that demonstrates  
core backend engineering concepts such as **atomic transactions,  
data consistency, secure authentication, and RESTful API design**, paired with a modern,  
responsive frontend.

## How to Run

### Prerequisites
- Java 11+
- MySQL 8.0+

### 1. Set up the database
```sql
mysql -u root -p < db/schema.sql
```

### 2. Configure environment variables
Copy `.env.example` to a new file called `.env` and fill in your values:
```
cp .env.example .env
```
Open `.env` and set your MySQL credentials and a strong random JWT secret:
```
DB_URL=jdbc:mysql://localhost:3306/bankdb
DB_USER=your_mysql_user
DB_PASSWORD=your_mysql_password
JWT_SECRET=replace_with_a_long_random_secret
JWT_TTL_MINUTES=60
```

### 3. Compile the backend
From the `backend/api` directory:
```bash
javac -cp "lib/*" -d out \
    src/bankapp/*.java \
    src/bankapp/dto/*.java \
    src/bankapp/handlers/*.java \
    src/bankapp/security/*.java
```
*(Windows: replace `:` with `;` in classpath separators)*

### 4. Start the backend
```bash
java -cp "out:lib/*" bankapp.BankServer
```
The API will be available at `http://localhost:5230`.

### 5. Open the frontend
Open `frontend/web/index.html` in your browser.

---


The application supports common banking operations including  
account management, deposits, withdrawals, peer-to-peer transfers,  
and transaction history tracking. All monetary operations are handled  
server-side to ensure correctness and consistency.

## Key Features
- Secure user authentication using **salted bcrypt password hashing**
- **JWT-based session management** — every protected endpoint requires a valid signed token
- Deposit and withdrawal operations  
- Peer-to-peer account transfers  
- Atomic server-side transaction handling  
- Persistent transaction history  
- Responsive, mobile-friendly frontend UI  

## Security
- Passwords are hashed using bcrypt before being stored (no plaintext password storage)
- All financial endpoints (`/balance`, `/deposit`, `/withdraw`, `/transfer`, `/history`) are protected by JWT — unauthenticated requests receive HTTP 401
- JWT tokens are signed with a secret loaded from the environment (`JWT_SECRET`) and expire after a configurable lifetime (`JWT_TTL_MINUTES`, default 60 min)
- Prepared statements are used to mitigate SQL injection risks
- Database transactions ensure atomic and consistent financial operations

## Tech Stack

### Backend
- Java  
- Spark Java (REST API)  
- MySQL  
- JDBC  

### Frontend
- HTML  
- CSS (modern styling)  
- JavaScript  

## Architecture
- RESTful API backend responsible for all business logic and transactions  
- Frontend consumes API endpoints and handles presentation only  
- Database-backed transaction system ensures consistent financial state  

## Project Structure
- `backend/` – Java REST API and business logic  
- `frontend/` – client-side UI  
- `db/` – database schema and related scripts  

## Focus
This project emphasizes **correctness, security, and transactional integrity**,  
with server-side enforcement of financial operations.