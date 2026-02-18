# Piggy Bank

Piggy Bank is a full-stack banking web application that demonstrates  
core backend engineering concepts such as **atomic transactions,  
data consistency, secure authentication, and RESTful API design**, paired with a modern,  
responsive frontend.

## Overview
The application supports common banking operations including  
account management, deposits, withdrawals, peer-to-peer transfers,  
and transaction history tracking. All monetary operations are handled  
server-side to ensure correctness and consistency.

## Key Features
- Secure user authentication using **salted bcrypt password hashing**
- Deposit and withdrawal operations  
- Peer-to-peer account transfers  
- Atomic server-side transaction handling  
- Persistent transaction history  
- Responsive, mobile-friendly frontend UI  

## Security
- Passwords are hashed using bcrypt before being stored (no plaintext password storage)
- Prepared statements are used to mitigate SQL injection risks
- Database transactions ensure atomic and consistent financial operations

### ⚠️ Security Notice
This is a **learning project** demonstrating secure coding practices. While it implements bcrypt password hashing, prepared statements, and proper transaction handling, it is **NOT production-ready**. 

**Known Limitations:**
- No session token or JWT authentication (authorization bypass possible)
- No rate limiting or brute force protection
- API expects username as query parameter without verification
- Requires HTTPS/TLS for production deployment

A real banking application would require comprehensive security testing, proper authentication/authorization, rate limiting, and additional hardening measures.

**Note:** Historical git commits contain a development password (`RyanSql05$`) that was never used in production and is only for local development setup.

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

## Setup

### Prerequisites
- Java 8 or higher
- MySQL database
- Web browser

### Installation Steps

1. **Set up the database**
   ```bash
   mysql -u root -p < db/schema.sql
   ```

2. **Configure environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your MySQL credentials:
   # DB_URL=jdbc:mysql://localhost:3306/bankdb
   # DB_USER=your_username
   # DB_PASSWORD=your_password
   ```

3. **Compile and run the backend**
   ```bash
   cd backend/api/src
   javac -cp "../lib/*:." bankapp/*.java bankapp/**/*.java
   java -cp "../lib/*:." bankapp.BankServer
   ```
   The API will start on `http://localhost:5230`

4. **Open the frontend**
   - Open `frontend/web/index.html` in your web browser
   - Register a new account to get started

### Usage
- Register a new account from the login page
- Deposit money into your account
- Withdraw money from your account
- Transfer money to other users
- View your transaction history  

## Focus
This project emphasizes **correctness, security, and transactional integrity**,  
with server-side enforcement of financial operations.