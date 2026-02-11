# Atomic Transactions

## Definition
In Piggy Bank, all money-moving operations are **atomic**: each operation either
completes fully or has **no effect at all**. Partial updates are not allowed.

## Why this matters
Financial operations must never leave account balances or transaction history in
an inconsistent state. If any step fails, the system rolls back to the previous
valid state.

## What makes it atomic in this codebase
Each money operation runs inside a single JDBC database transaction:

1. Begin transaction by disabling autocommit (`conn.setAutoCommit(false)`)
2. Apply the balance change (UPDATE)
3. Record the transaction event (INSERT into transaction history)
4. Commit only if all steps succeed (`conn.commit()`)
5. On any failure → rollback (`conn.rollback()`)
6. Restore the connection’s original autocommit setting in `finally`

This ensures that the balance update and the transaction-history record are
committed together, or not committed at all.

## Failure behavior
Examples of failures that trigger rollback:
- Target user/account is not found (0 rows updated)
- SQL errors while updating balances
- SQL errors while inserting transaction history
- Any exception during the transactional sequence

## Operations covered
- Deposit
- Withdrawal
- Peer-to-peer transfer (debit + credit within the same transaction)

## Concurrency & race conditions
Atomicity guarantees all-or-nothing behavior for a single operation, but safe
behavior under **concurrent requests** also depends on how balances are updated.

To prevent race conditions (e.g., two simultaneous withdrawals against the same
funds), operations should rely on database-enforced safety such as:
- Atomic conditional updates (e.g., UPDATE with a balance constraint), and checking
  affected row count, **or**
- Row-level locking during validation/update (e.g., `SELECT ... FOR UPDATE`), **or**
- An appropriate transaction isolation level

The backend is structured so these protections can be enforced server-side within
the same transaction boundaries described above.