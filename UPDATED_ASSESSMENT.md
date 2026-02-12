# Updated Piggy Bank Repository Assessment
## With Database Schema Analysis

**Date:** February 12, 2026  
**Update:** Schema added in commit `98b1712`

---

## 🎯 Key Changes from Previous Assessment

The addition of `db/schema.sql` provides crucial documentation and reveals both strengths and remaining issues in the codebase.

---

## 📊 Database Schema Analysis

### Schema Structure (`db/schema.sql`)

```sql
CREATE TABLE customer (
    CUS_UNAME VARCHAR(32) PRIMARY KEY,
    CUS_PASSWD VARCHAR(255),
    CUS_BALANCE DECIMAL(15,2)
);

CREATE TABLE transaction_record (
    TXN_ID INT PRIMARY KEY AUTO_INCREMENT,
    CUS_ID_SOURCE VARCHAR(32) NULL,
    CUS_ID_DEST VARCHAR(32) NULL,
    TXN_AMOUNT DECIMAL(15,2),
    TXN_DATETIME DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CUS_ID_SOURCE) REFERENCES customer(CUS_UNAME),
    FOREIGN KEY (CUS_ID_DEST) REFERENCES customer(CUS_UNAME)
);
```

### ✅ **Schema Strengths**

1. **Proper Foreign Key Constraints**
   - `transaction_record` references `customer` table for both source and destination
   - Maintains referential integrity
   - Prevents orphaned transaction records

2. **Nullable Source/Dest Fields**
   - `CUS_ID_SOURCE` NULL = deposit (money from nowhere)
   - `CUS_ID_DEST` NULL = withdrawal (money to nowhere)
   - Both set = transfer between accounts
   - **Clever design** that allows single table for all transaction types

3. **Appropriate Data Types**
   - `DECIMAL(15,2)` for money - **CORRECT** (no floating-point errors)
   - `VARCHAR(32)` for username - reasonable size
   - `VARCHAR(255)` for password - sized for hashed passwords
   - Auto-incrementing `TXN_ID` primary key

4. **Automatic Timestamp**
   - `TXN_DATETIME DEFAULT CURRENT_TIMESTAMP` - tracks all transactions

5. **Clean Schema Design**
   - Simple, normalized structure
   - No redundant data
   - Clear relationships

---

## 🔍 Code-to-Schema Mapping Verification

### ✅ Handlers Match Schema Correctly

| Handler | Source Field | Dest Field | Schema Match |
|---------|-------------|------------|--------------|
| **DepositHandler** | NULL | username | ✅ Correct |
| **WithdrawHandler** | username | NULL | ✅ Correct |
| **TransferHandler** | fromUser | toUser | ✅ Correct |

**All SQL queries properly use the schema columns.**

---

## 🟢 **Improved Findings**

### 1. **Withdrawal Handler NOW Uses Row Locking** ✅ 

**Previous concern:** "Race condition in withdrawal - check-then-act pattern"

**RESOLVED:** Reviewing `WithdrawHandler.java` line 45:
```java
String balSQL = "SELECT CUS_BALANCE FROM CUSTOMER WHERE CUS_UNAME = ? FOR UPDATE";
```

- **Uses `SELECT ... FOR UPDATE`** - locks the row during transaction
- Prevents concurrent over-withdrawal
- Follows the recommendation from `ATOMICITY.md`
- **This is correct implementation** ✅

### 2. **Transfer Handler Has Deadlock Prevention** ✅

From `TransferHandler.java` lines 46-56:
```java
// lock both users in a consistent order to prevent deadlocks
String first, second;
if (fromUser.compareTo(toUser) < 0) {
    first = fromUser;
    second = toUser;
} else {
    first = toUser;
    second = fromUser;
}
```

- **Alphabetic locking order** prevents circular wait
- Combined with `SELECT ... FOR UPDATE` (line 44)
- **Sophisticated concurrency control** ✅

### 3. **Transfer Handler Uses Atomic Balance Check**

Line 29-30:
```java
String updateSourceSql = 
    "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE - ? WHERE CUS_UNAME = ? AND CUS_BALANCE >= ?";
```

- **Single atomic UPDATE with balance constraint**
- Returns 0 rows if insufficient funds
- No gap between check and update
- **Race-condition safe** ✅

---

## 🔴 **Remaining Critical Issues**

### 1. **Hardcoded Credentials Still Present** (CRITICAL)

`Database.java` lines 17-18:
```java
private static final String USER = "root";
private static final String PASSWORD = "RyanSql05$";
```

**Issues:**
- ⚠️ **Real password committed to source control** 
- ⚠️ **Password visible in GitHub history forever**
- ⚠️ Violates security best practices
- ⚠️ Anyone with repo access has DB credentials

**Impact:** Schema doesn't change this - still CRITICAL vulnerability

### 2. **Plain-Text Password Storage** (CRITICAL)

Schema shows `CUS_PASSWD VARCHAR(255)` but `LoginHandler.java` line 42:
```java
if (storedPass.equals(password)) {
```

**Issues:**
- ❌ Passwords stored in plain text in database
- ❌ `VARCHAR(255)` sized for hashing but not used
- ❌ Anyone with DB access sees all passwords
- ❌ No BCrypt, Argon2, or any hashing

**What the schema reveals:**
- `VARCHAR(255)` **suggests intent** to use password hashing (BCrypt produces ~60 char strings)
- But the Java code doesn't actually hash anything
- **Partial implementation** - schema is ready, code is not

### 3. **No Authentication System** (CRITICAL)

The schema has **no session or token table**, confirming:
- No JWT tokens
- No session management
- Users pass username in query parameters
- Anyone can query any user's balance/history

**Missing from schema:**
```sql
-- No session table like this:
CREATE TABLE user_sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME,
    FOREIGN KEY (username) REFERENCES customer(CUS_UNAME)
);
```

### 4. **No Constraints on Balance** (MEDIUM)

Schema allows:
```sql
CUS_BALANCE DECIMAL(15,2)  -- Can be negative!
```

**No `CHECK (CUS_BALANCE >= 0)` constraint**

While handlers prevent negative withdrawals, direct SQL could bypass this:
```sql
UPDATE customer SET CUS_BALANCE = -1000 WHERE CUS_UNAME = 'alice';  -- Works!
```

**Recommendation:** Add constraint:
```sql
CUS_BALANCE DECIMAL(15,2) CHECK (CUS_BALANCE >= 0)
```

### 5. **No Indices for Performance** (LOW)

Schema has no indices beyond primary keys:
```sql
-- Missing indices like:
CREATE INDEX idx_txn_datetime ON transaction_record(TXN_DATETIME);
CREATE INDEX idx_txn_source ON transaction_record(CUS_ID_SOURCE);
CREATE INDEX idx_txn_dest ON transaction_record(CUS_ID_DEST);
```

For a learning project this is fine, but production would need these for history queries.

---

## 📈 **Updated Scoring**

| Category | Old Score | New Score | Change | Notes |
|----------|-----------|-----------|--------|-------|
| **Documentation** | 7/10 | 8/10 | +1 | Schema file added |
| **Code Quality** | 7/10 | 8/10 | +1 | Row locking verified |
| **Security** | 2/10 | 2/10 | 0 | Still critical flaws |
| **Testing** | 0/10 | 0/10 | 0 | Still no tests |
| **Build/Deploy** | 1/10 | 1/10 | 0 | Still no automation |
| **Architecture** | 8/10 | 9/10 | +1 | Excellent patterns confirmed |
| **Database Design** | N/A | 8/10 | NEW | Well-designed schema |

**New Overall:** **78% → C+** (slight improvement from C+/72%)

---

## 💡 **What the Schema Reveals About Code Quality**

### Positive Indicators:

1. **Thoughtful Design**
   - The nullable source/dest pattern shows architectural thinking
   - Foreign keys show understanding of referential integrity
   - `DECIMAL` for money shows awareness of float issues

2. **Production-Ready Schema Structure**
   - Could scale to production with minor additions
   - Clean normalization
   - Proper data types

3. **Documentation Improvement**
   - Having `schema.sql` is a **major upgrade**
   - Anyone can now set up the database
   - Clear structure documentation

### Areas of Concern:

1. **Password Field Sizing Mismatch**
   - `VARCHAR(255)` suggests hashing was considered
   - But code stores plain text
   - **Incomplete security implementation**

2. **No Session Infrastructure**
   - Schema confirms no token/session system
   - Would need schema changes to add proper auth

3. **Missing Constraints**
   - No balance >= 0 check
   - Relies on application layer only

---

## 🎯 **Updated Recommendations**

### **Phase 1: Fix Password Security** (HIGH PRIORITY)

Since schema is already sized for hashing:

```java
// In RegisterHandler.java and LoginHandler.java
import org.mindrot.jbcrypt.BCrypt;

// On register:
String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
// Store hashedPassword in DB

// On login:
if (BCrypt.checkpw(password, storedHashedPassword)) {
    // success
}
```

**Easy win:** Schema already supports it!

### **Phase 2: Environment Variables for Credentials**

```java
// In Database.java
private static final String USER = System.getenv("DB_USER");
private static final String PASSWORD = System.getenv("DB_PASSWORD");
```

### **Phase 3: Add Schema Constraints**

```sql
ALTER TABLE customer 
ADD CONSTRAINT chk_balance CHECK (CUS_BALANCE >= 0);
```

### **Phase 4: Add Session Table**

For JWT alternative, add:
```sql
CREATE TABLE user_sessions (
    session_id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (username) REFERENCES customer(CUS_UNAME) ON DELETE CASCADE
);
```

---

## 🏁 **Final Verdict After Schema Review**

### What Improved:
✅ **Documentation** - Schema file is excellent addition  
✅ **Setup clarity** - Anyone can now create the database  
✅ **Architecture confidence** - Code-to-schema mapping is correct  
✅ **Concurrency handling** - Row locking verified in code  

### What Didn't Change:
❌ **Security vulnerabilities** - Still critical (passwords, credentials)  
❌ **No tests** - Schema doesn't change this  
❌ **No build system** - Still needs Maven/Gradle  

### Revised Assessment:

**For Production Use:** ❌ **Not Ready** - Security vulnerabilities remain  
**For Learning:** ✅ **Excellent** - Well-designed with clear patterns  
**For Portfolio:** ⚠️ **Close** - Add password hashing and you're 90% there  

---

## 🚀 **Next Steps (Prioritized)**

1. **Add BCrypt password hashing** (~30 min)
   - Schema already supports it
   - Just change Java code
   - Biggest security win

2. **Move DB credentials to .env** (~15 min)
   - Use `dotenv-java` library
   - Remove password from git history (using git filter-branch)

3. **Add balance constraint to schema** (~5 min)
   ```sql
   ALTER TABLE customer ADD CONSTRAINT chk_balance CHECK (CUS_BALANCE >= 0);
   ```

4. **Add basic unit tests** (~2 hours)
   - Test each handler with H2 in-memory DB
   - Verify transaction rollback behavior

5. **Create Maven POM** (~30 min)
   - Dependency management
   - Easy builds

---

## 📝 **Summary**

The addition of `db/schema.sql` is a **significant improvement** that:
- ✅ Documents the database structure clearly
- ✅ Confirms the code implements proper transaction patterns
- ✅ Shows thoughtful design (nullable fields, proper types)
- ✅ Enables easy database setup

However, it also **confirms** the security concerns:
- ❌ Password field sized for hashing but not used
- ❌ No session/token infrastructure in schema
- ❌ Hardcoded credentials still in code

**Grade:** **C+ → C+** (78%, up from 72%)  
**Recommendation:** Fix password hashing next - it's the easiest high-impact change.
