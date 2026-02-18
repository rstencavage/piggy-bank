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