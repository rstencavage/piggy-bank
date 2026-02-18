# Production Deployment Checklist
## Quick Reference Guide for Piggy Bank

Use this as a checklist when preparing for production deployment.

---

## 🔴 **CRITICAL - Must Complete Before Any Production Deployment**

### Security & Authentication
- [ ] **JWT Authentication** - Implement token-based authentication
  - [ ] Add java-jwt dependency
  - [ ] Create JWTUtil class for token generation/validation
  - [ ] Add authentication middleware
  - [ ] Update all endpoints to require valid token
  - [ ] Update frontend to use Authorization header
  - [ ] Remove username from query parameters

- [ ] **HTTPS/TLS Configuration**
  - [ ] Obtain SSL certificate (Let's Encrypt recommended)
  - [ ] Configure Spark to use HTTPS
  - [ ] Force HTTP→HTTPS redirects
  - [ ] Update frontend API URLs to HTTPS
  - [ ] Test SSL certificate validity

- [ ] **Rate Limiting**
  - [ ] Implement rate limiter (per IP/user)
  - [ ] Add rate limit for login attempts (5 per minute)
  - [ ] Add rate limit for API calls (100 per minute)
  - [ ] Return 429 status for rate limit exceeded
  - [ ] Document rate limits in API docs

### Build & Testing
- [ ] **Maven Build System**
  - [ ] Create pom.xml with all dependencies
  - [ ] Configure maven-shade-plugin for fat JAR
  - [ ] Update .gitignore to exclude target/ and lib/ JARs
  - [ ] Verify `mvn clean package` works
  - [ ] Test running the built JAR

- [ ] **Automated Testing** (Minimum)
  - [ ] Unit tests for all handlers (20+ tests)
  - [ ] Integration test for login flow
  - [ ] Integration test for deposit/withdraw
  - [ ] Integration test for transfer
  - [ ] Test transaction rollback scenarios
  - [ ] Achieve >70% code coverage

### Database
- [ ] **Connection Pooling**
  - [ ] Add HikariCP dependency
  - [ ] Configure connection pool (max 10, min 2)
  - [ ] Update Database.java to use pool
  - [ ] Test under concurrent load

- [ ] **Database Constraints**
  - [ ] Add CHECK constraint: balance >= 0
  - [ ] Add CHECK constraint: username length >= 3
  - [ ] Add CHECK constraint: amount > 0
  - [ ] Create indices on transaction_record
  - [ ] Test constraints work correctly

---

## 🟡 **HIGH PRIORITY - Important for Production Quality**

### Logging & Monitoring
- [ ] **Structured Logging**
  - [ ] Replace all printStackTrace() with logger calls
  - [ ] Add SLF4J + Logback dependencies
  - [ ] Create logback.xml configuration
  - [ ] Log all authentication attempts
  - [ ] Log all transactions
  - [ ] Log all errors with context
  - [ ] Set up log rotation

- [ ] **Error Handling**
  - [ ] Return proper HTTP status codes (400, 401, 404, 500)
  - [ ] Create ErrorResponse DTO
  - [ ] Add try-catch blocks to all endpoints
  - [ ] Handle JSON parsing errors
  - [ ] Handle database connection errors
  - [ ] Return user-friendly error messages

### Input Validation
- [ ] **Enhanced Validation**
  - [ ] Validate username format (alphanumeric + underscore)
  - [ ] Validate username length (3-32 chars)
  - [ ] Validate password strength (min 8 chars)
  - [ ] Validate amount (positive, max 2 decimal places)
  - [ ] Validate amount limits (max $1,000,000)
  - [ ] Sanitize all user inputs
  - [ ] Add validation error responses

---

## 🟢 **RECOMMENDED - Best Practices**

### Infrastructure
- [ ] **Docker Containerization**
  - [ ] Create Dockerfile for backend
  - [ ] Create docker-compose.yml
  - [ ] Test Docker build and run
  - [ ] Document Docker setup in README

- [ ] **CI/CD Pipeline**
  - [ ] Create .github/workflows/ci.yml
  - [ ] Configure automated testing on PR
  - [ ] Configure automated build on merge
  - [ ] Set up deployment automation
  - [ ] Add OWASP dependency check

### Documentation
- [ ] **API Documentation**
  - [ ] Create OpenAPI/Swagger specification
  - [ ] Document all endpoints
  - [ ] Document request/response schemas
  - [ ] Document authentication flow
  - [ ] Document error codes
  - [ ] Add example requests

- [ ] **Deployment Documentation**
  - [ ] Document server requirements
  - [ ] Document environment variables
  - [ ] Document database setup
  - [ ] Document SSL certificate setup
  - [ ] Create troubleshooting guide
  - [ ] Document backup procedures

### Configuration
- [ ] **Environment Variables**
  - [ ] DB_URL, DB_USER, DB_PASSWORD ✅
  - [ ] JWT_SECRET (add)
  - [ ] JWT_EXPIRATION (add)
  - [ ] API_PORT (add, default 5230)
  - [ ] CORS_ORIGIN (add, default *)
  - [ ] RATE_LIMIT_PER_MINUTE (add)
  - [ ] LOG_LEVEL (add, default INFO)

- [ ] **Frontend Configuration**
  - [ ] Create config.js with API_URL
  - [ ] Support multiple environments (dev/staging/prod)
  - [ ] Update all fetch calls to use config

---

## 🔵 **OPTIONAL - Nice to Have**

### Monitoring & Observability
- [ ] Set up application monitoring (New Relic/DataDog)
- [ ] Add health check endpoint (/health)
- [ ] Add metrics endpoint (/metrics)
- [ ] Configure alerts for errors
- [ ] Configure alerts for high latency
- [ ] Set up uptime monitoring

### Advanced Features
- [ ] Password reset functionality
- [ ] Email notifications for transactions
- [ ] Two-factor authentication (2FA)
- [ ] Account lockout after failed attempts
- [ ] Transaction search and filtering
- [ ] Export transaction history (CSV/PDF)
- [ ] Admin dashboard

---

## 🎯 **Minimum Viable Production (MVP) Checklist**

If you need to deploy ASAP, complete AT LEAST these items:

- [ ] JWT authentication ⭐⭐⭐
- [ ] HTTPS/TLS configuration ⭐⭐⭐
- [ ] Basic rate limiting ⭐⭐⭐
- [ ] Maven build system ⭐⭐⭐
- [ ] Connection pooling ⭐⭐
- [ ] Structured logging ⭐⭐
- [ ] Basic unit tests (10+ tests) ⭐⭐
- [ ] Database constraints ⭐⭐
- [ ] Error handling with proper status codes ⭐⭐

**MVP Timeline: 2-3 weeks**

---

## 📊 **Pre-Deployment Verification**

Before going live, verify these items:

### Security Checklist
- [ ] All passwords hashed with BCrypt ✅
- [ ] Database credentials in environment variables ✅
- [ ] JWT secret is strong and random
- [ ] HTTPS enforced, no HTTP access
- [ ] Rate limiting active and tested
- [ ] SQL injection prevention verified ✅
- [ ] XSS prevention verified
- [ ] CORS configured properly
- [ ] No sensitive data in logs
- [ ] No API keys in source code

### Testing Checklist
- [ ] All unit tests passing
- [ ] All integration tests passing
- [ ] Load testing completed (100+ concurrent users)
- [ ] Security testing completed (OWASP Top 10)
- [ ] Database backups tested
- [ ] Disaster recovery plan documented

### Operations Checklist
- [ ] Monitoring and alerts configured
- [ ] Log aggregation working
- [ ] Database backups automated
- [ ] SSL certificate auto-renewal configured
- [ ] Deployment rollback plan documented
- [ ] On-call rotation established
- [ ] Incident response plan documented

### Performance Checklist
- [ ] Database queries optimized
- [ ] Indices created on frequently queried columns
- [ ] Connection pooling configured
- [ ] Response time < 200ms for 95% of requests
- [ ] Can handle expected load (users/second)
- [ ] Caching strategy implemented (if needed)

---

## 🚀 **Deployment Day Checklist**

### Before Deployment
- [ ] Notify users of planned maintenance window
- [ ] Back up production database
- [ ] Tag the release in git (e.g., v1.0.0)
- [ ] Build and test the deployment package
- [ ] Review deployment plan with team
- [ ] Prepare rollback plan

### During Deployment
- [ ] Deploy database schema changes
- [ ] Deploy backend application
- [ ] Verify backend is running
- [ ] Deploy frontend application
- [ ] Run smoke tests
- [ ] Monitor error rates and latency

### After Deployment
- [ ] Verify all critical flows work
- [ ] Monitor logs for errors
- [ ] Monitor system metrics
- [ ] Notify users deployment is complete
- [ ] Document any issues encountered
- [ ] Update runbook with lessons learned

---

## 📞 **Emergency Contacts & Resources**

- **Application Owner:** [Your Name]
- **Database Admin:** [Contact]
- **DevOps Lead:** [Contact]
- **On-Call Engineer:** [Rotation]

### Important URLs
- **Production API:** https://api.piggybank.com
- **Monitoring Dashboard:** [URL]
- **CI/CD Pipeline:** [URL]
- **Documentation:** [URL]

### Rollback Procedure
1. Stop the new application
2. Restore previous application version
3. Rollback database changes (if any)
4. Verify old version is working
5. Investigate and fix issues
6. Plan re-deployment

---

**Remember:** Production readiness is not a one-time task. Continuously monitor, improve, and iterate on your production environment.

**Last Updated:** February 18, 2026
