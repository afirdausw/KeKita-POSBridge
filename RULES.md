# RULES.md

Project Name:
KeKita POS Bridge Android

Primary Goal:

Application must continue operating even when:

* Internet is unavailable
* Local network is unavailable
* Central server is down

Offline First Principles:

1. Local database is the source of truth.
2. API is only used for synchronization.
3. User must never wait for API response to complete a transaction.
4. Every transaction receives a local UUID.
5. Every transaction enters sync queue.
6. Sync happens in background.

Transaction Flow:

Create Transaction
↓
Save Local
↓
Print Receipt
↓
Mark Pending Sync
↓
Background Sync
↓
Mark Synced

Sync Rules:

* Retry indefinitely.
* Exponential backoff.
* Never delete unsynced records.
* Never overwrite local data blindly.
* Log all synchronization failures.

Database Rules:

* Room Database only.
* Soft delete preferred.
* Every table must contain:

id
uuid
created_at
updated_at
sync_status

sync_status:

PENDING
SYNCED
FAILED

Performance Rules:

* UI must remain responsive.
* Avoid blocking Main Thread.
* Use Coroutines.
* Use Paging when necessary.

Code Quality Rules:

* Single Responsibility Principle.
* No duplicated logic.
* No magic numbers.
* No hardcoded URLs.
* Use constants.
* Use dependency injection.

Expected Devices:

* Android Tablets
* Android 10+
* 4GB RAM minimum

Priority:

Reliability > Features
Data Integrity > Speed
Offline Capability > Realtime Capability
