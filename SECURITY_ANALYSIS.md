# Security Analysis - February 2026
## Piggy Bank Repository Security Assessment

**Analysis Date:** February 18, 2026  
**Analyzed Commits:** Up to `7550309` (Update README to document bcrypt authentication)

---

## 🎯 Executive Summary

### **Is the repo safe to keep public? ⚠️ MOSTLY YES, with caveats**

**Short Answer:** The repository is **MUCH SAFER** than before and can remain public with appropriate warnings, but there are still some concerns that need attention.

**Grade Improvement:** **F (Critical) → B- (Acceptable for Public)**

---

## ✅ **Major Security Improvements Made**

### 1. **BCrypt Password Hashing** ✅ FIXED

**Before:**
```java
// LoginHandler.java (OLD)
if (storedPass.equals(password)) {  // Plain text comparison
```

**After:**
```java
// LoginHandler.java (NEW)
import org.mindrot.jbcrypt.BCrypt;
...
if (storedPass != null && BCrypt.checkpw(password, storedPass)) {
```

**Impact:**
- ✅ Passwords now hashed with BCrypt (12 rounds)
- ✅ Salted hashing prevents rainbow table attacks
- ✅ Industry-standard implementation
- ✅ Schema updated to `CUS_PASSWD_HASH` column
- ✅ Both RegisterHandler and LoginHandler use BCrypt

**Security Rating:** 🟢 **EXCELLENT** - Properly implemented

---

### 2. **Environment Variables for Credentials** ✅ FIXED

**Before:**
```java
// Database.java (OLD)
private static final String USER = "root";
private static final String PASSWORD = "RyanSql05$";  // ⚠️ Hardcoded!
```

**After:**
```java
// Database.java (NEW)
private static final Dotenv dotenv = Dotenv.load();
private static final String URL = dotenv.get("DB_URL");
private static final String USER = dotenv.get("DB_USER");
private static final String PASSWORD = dotenv.get("DB_PASSWORD");
```

**Files Added:**
- `.env.example` - Template with placeholder values
- `.gitignore` - Excludes `.env` and `*.env` files

**Impact:**
- ✅ No hardcoded credentials in current code
- ✅ `.env` properly excluded from git
- ✅ `.env.example` provides setup template
- ✅ Uses industry-standard dotenv library

**Security Rating:** 🟢 **EXCELLENT** - Properly implemented

---

### 3. **Dependencies Added**

New JARs in `backend/api/lib/`:
- ✅ `jbcrypt-0.4.jar` - BCrypt password hashing
- ✅ `java-dotenv-5.2.2.jar` - Environment variable management

**Security Rating:** 🟢 **GOOD** - Standard, trusted libraries

---

### 4. **Documentation Updated**

`README.md` now includes:
```markdown
## Security
- Passwords are hashed using bcrypt before being stored
- Prepared statements are used to mitigate SQL injection risks
- Database transactions ensure atomic operations
```

**Impact:**
- ✅ Clear security documentation
- ✅ Sets expectations for users
- ✅ Shows security awareness

---

## 🔴 **Remaining Security Concerns**

### 1. **Git History Still Contains Old Password** ⚠️ HIGH PRIORITY

**Issue:**
The password `RyanSql05$` is still visible in git history:

```bash
$ git log --all --patch -- backend/api/src/bankapp/Database.java
# Shows: private static final String PASSWORD = "RyanSql05$";
```

**Commits containing the password:**
- `db11d5a` - Initial project setup
- Earlier commits before `dcef6cb`

**Impact:**
- ⚠️ Anyone with repo access can see historical password
- ⚠️ If this was a real production password, the database could be compromised
- ⚠️ Password is searchable in GitHub's history

**Is this a dealbreaker for public repo?**
- If `RyanSql05$` was **only used for local development** → ✅ **Acceptable**
- If this password was **ever used in production** → ❌ **Must be rotated**
- If this is your **personal MySQL password for other systems** → ⚠️ **Change it everywhere**

**Recommendation:**
1. **If local-only password:** Add warning in README that this was a dev-only password
2. **If real password:** Change password on any systems that used it
3. **Optional:** Rewrite git history (advanced, can break forks)

---

### 2. **Schema Has Typo** 🟡 MEDIUM PRIORITY

**Issue:**
```sql
-- db/schema.sql line 11
CUS_PASSWD_HASH_HASH VARCHAR(255),  -- Double "HASH"!
```

**Should be:**
```sql
CUS_PASSWD_HASH VARCHAR(255),
```

**Impact:**
- ⚠️ Code uses `CUS_PASSWD_HASH` but schema says `CUS_PASSWD_HASH_HASH`
- ⚠️ Schema won't match running database
- ⚠️ New users following schema will get SQL errors

**Recommendation:** Fix the typo in schema.sql

---

### 3. **Frontend Still Has Hardcoded API URL** 🟡 MEDIUM PRIORITY

**Issue:**
```javascript
// frontend/web/js/auth.js
fetch('http://localhost:5230/login', {  // Hardcoded!
```

All frontend files use `http://localhost:5230`

**Impact:**
- 🟡 Not a security vulnerability per se
- 🟡 Makes deployment harder
- 🟡 Can't easily switch environments

**Recommendation:** 
- Use a config file or environment variable for frontend API URL
- Not critical for security, but good practice

---

### 4. **No Authentication Tokens** 🟡 MEDIUM PRIORITY

**Issue:**
- Frontend stores username in `localStorage`
- No session tokens or JWT
- API endpoints accept username as query parameter
- No way to verify the request is from the actual user

**Example:**
```javascript
// Anyone can query anyone's balance
fetch(`http://localhost:5230/balance?username=alice`)
```

**Impact:**
- 🟡 Authorization bypass - can query other users' data
- 🟡 No session management
- 🟡 No logout mechanism (other than clearing localStorage)

**Is this a dealbreaker?**
- For a **learning project:** ✅ Acceptable
- For **production:** ❌ Needs JWT or session tokens

**Recommendation for production:**
1. Implement JWT tokens after login
2. Require token in Authorization header for all API calls
3. Validate token on server before processing requests

---

### 5. **No Database Balance Constraint** 🟢 LOW PRIORITY

**Issue:**
```sql
CUS_BALANCE DECIMAL(15,2)  -- Can be negative!
```

No `CHECK (CUS_BALANCE >= 0)` constraint

**Impact:**
- 🟢 Application code prevents negative balances
- 🟢 But direct SQL could bypass this
- 🟢 Defense in depth would add DB constraint

**Recommendation:** Add constraint in future update (not critical)

---

### 6. **No HTTPS/TLS Configuration** 🟡 MEDIUM PRIORITY

**Issue:**
- API runs on HTTP (localhost:5230)
- No TLS/SSL configuration
- Passwords sent in plain text over network

**Impact:**
- 🟡 Fine for localhost development
- 🟡 **CRITICAL if deployed** - would need HTTPS

**Recommendation:** Document that HTTPS is required for production deployment

---

## 📊 **Security Scorecard**

| Category | Before | After | Status |
|----------|--------|-------|--------|
| **Password Storage** | ❌ Plain text | ✅ BCrypt hashed | FIXED |
| **Credential Management** | ❌ Hardcoded | ✅ Environment vars | FIXED |
| **SQL Injection** | ✅ Prepared statements | ✅ Prepared statements | GOOD |
| **Transaction Safety** | ✅ ACID compliant | ✅ ACID compliant | GOOD |
| **Git History** | ❌ Password exposed | ⚠️ Still in history | CONCERN |
| **Authentication** | ❌ None | ❌ None | NEEDS WORK |
| **Authorization** | ❌ None | ❌ None | NEEDS WORK |
| **Input Validation** | 🟡 Basic | 🟡 Basic | ADEQUATE |
| **HTTPS/TLS** | ❌ HTTP only | ❌ HTTP only | DEV ONLY |

---

## 🎯 **Public Repository Recommendation**

### **✅ YES, Safe to Keep Public With These Conditions:**

1. **✅ For Educational/Portfolio Use**
   - Clearly mark as learning project
   - Don't use in production without security hardening
   - Document known limitations

2. **✅ If Password Was Dev-Only**
   - Add note in README: "Note: Historical commits contain a development password that was never used in production"
   - Verify password isn't used elsewhere

3. **✅ With Proper Documentation**
   - Add security warnings in README
   - Document that authentication/authorization need improvement
   - Warn against production use without JWT implementation

### **❌ NOT Safe If:**

1. ❌ The exposed password (`RyanSql05$`) is used on any real system
2. ❌ You plan to deploy this publicly without adding authentication
3. ❌ The database contains real user data

---

## 🚀 **Recommendations for Public Release**

### **Before Making Public (High Priority):**

1. **Fix Schema Typo** (5 minutes)
   ```sql
   -- Change line 11 in db/schema.sql
   CUS_PASSWD_HASH VARCHAR(255),  -- Remove duplicate HASH
   ```

2. **Add Security Warning to README** (5 minutes)
   ```markdown
   ## ⚠️ Security Notice
   This is a learning project demonstrating secure coding practices. 
   While it implements bcrypt password hashing and proper transaction 
   handling, it is NOT production-ready. A real banking application 
   would require:
   - JWT or session-based authentication
   - HTTPS/TLS encryption
   - Rate limiting and brute force protection
   - Additional input validation
   - Comprehensive security testing
   
   Historical git commits contain a development password that was 
   never used in production.
   ```

3. **Verify Password Isn't Used Elsewhere** (immediate)
   - Change `RyanSql05$` on any system where it might be used
   - Ensure it was truly only a local development password

### **Nice to Have (Medium Priority):**

4. **Fix Frontend API URL** (30 minutes)
   - Create `frontend/config.js` with configurable API URL
   - Update all fetch calls to use config

5. **Add Setup Instructions** (15 minutes)
   ```markdown
   ## Setup
   1. Copy `.env.example` to `.env`
   2. Update `.env` with your MySQL credentials
   3. Run `db/schema.sql` to create database
   4. Start backend API
   5. Open frontend in browser
   ```

### **Future Enhancements (Low Priority):**

6. **Add JWT Authentication** (4-6 hours)
7. **Add Unit Tests** (ongoing)
8. **Add Rate Limiting** (2-3 hours)
9. **Add Database Constraint** for balance (5 minutes)

---

## 📝 **Summary**

### **What You Fixed (Excellent!):**
✅ BCrypt password hashing - **properly implemented**  
✅ Environment variable configuration - **properly implemented**  
✅ Added proper .gitignore - **properly implemented**  
✅ Updated documentation - **clear and accurate**  
✅ Added required dependencies - **appropriate choices**

### **What Still Needs Attention:**
⚠️ Schema typo (`CUS_PASSWD_HASH_HASH`)  
⚠️ Git history contains old password (acceptable if dev-only)  
⚠️ No authentication/authorization system (acceptable for learning project)  
⚠️ Hardcoded frontend API URL (minor issue)

### **Final Verdict:**

**🟢 SAFE TO KEEP PUBLIC** for a portfolio/learning project with:
1. Schema typo fixed
2. README security warning added
3. Confirmation that exposed password was dev-only

**Grade:** **B- → B** (after typo fix and README update)

**Excellent work on addressing the critical security issues!** The BCrypt implementation and environment variable migration show solid understanding of security best practices. With minor documentation updates, this is a great portfolio project that demonstrates secure coding practices.

---

## 🔍 **Verification Checklist**

Before making the repo public, verify:

- [ ] Schema typo fixed (`CUS_PASSWD_HASH_HASH` → `CUS_PASSWD_HASH`)
- [ ] README includes security warning
- [ ] Old password `RyanSql05$` confirmed as dev-only
- [ ] Password changed on any systems that used it
- [ ] .gitignore properly excludes .env files (✅ already done)
- [ ] .env.example exists with placeholder values (✅ already done)
- [ ] Documentation clearly states this is a learning project

---

**Author:** GitHub Copilot Security Analysis  
**Date:** February 18, 2026
