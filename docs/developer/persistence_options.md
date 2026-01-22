# Persistence Storage Options

This document compares available persistence storage solutions for the **Kaimera Tablet** project and recommends strategies for different use cases.

## Overview

Android provides several options for saving persistent data. Choosing the right one depends on the nature of the data (structured vs. unstructured), the amount of data, and performance requirements.

## Comparison Table

| Feature | **DataStore** | **Room (SQLite)** | **SharedPreferences** | **File Storage** |
| :--- | :--- | :--- | :--- | :--- |
| **Type** | Key-Value or Typed Objects | Relational Database | Key-Value | Raw Bytes/Text |
| **API Surface** | Kotlin Coroutines / Flow | SQL / DAOs | Synchronous/blocking Getters/Setters | Filesystem APIs |
| **Thread Safety** | Safe (runs on IO dispatcher) | Safe (Support for Coroutines/Flow) | Not guaranteed to be main-safe | Manual handling required |
| **Transactional** | Yes (Atomic updates) | Yes | No (apply is async, commit is sync) | No |
| **Use Case** | Settings, user preferences, small state | Large datasets, complex queries, relations | **LEGACY** (Avoid) | Images, Audio, Large Documents |

---

## Detailed Options

### 1. Jetpack DataStore (Recommended for Settings)

DataStore is the modern replacement for SharedPreferences. It is built on Kotlin Coroutines and Flow, ensuring data changes are handled asynchronously and consistently.

**Variations:**
*   **Preferences DataStore:** Key-value pairs (like SharedPreferences). No schema definition required.
*   **Proto DataStore:** Stores typed objects (defined by Protocol Buffers). Provides type safety.

**Pros:**
*   **Asynchronous API:** Built with Coroutines and Flow, avoiding UI thread blocking.
*   **Safe:** Handles data corruption and migration better than SharedPreferences.
*   **Consistency:** Atomic operations ensure data integrity.

**Cons:**
*   Not suitable for large or complex datasets (use Room instead).
*   Proto DataStore requires Protocol Buffers setup (learning curve).

**Current Status in Kaimera:**
*   `androidx.datastore:datastore-preferences:1.0.0` is already included in `build.gradle.kts`.
*   **Recommendation:** Use **Preferences DataStore** for simple applet settings (e.g., Browser home URL, Camera grid toggle).

### 2. Room Database (Recommended for Structured Data)

Room is an abstraction layer over SQLite, providing a robust way to manage relational data.

**Pros:**
*   **Structured Data:** Perfect for lists, relations, and complex queries.
*   **Type Safety:** Compile-time verification of SQL queries.
*   **Observability:** Returns `Flow<T>`, allowing UI to auto-update when DB changes.

**Cons:**
*   **Overhead:** Requires more boilerplate (Entities, DAOs, Database class).
*   **Migration:** Schema changes require migration strategies.

**Current Status in Kaimera:**
*   Not currently a dependency.
*   **Recommendation:** Adopt Room if/when we need to store:
    *   Example: A database of **Notes** (Title, Content, Date, Tags).
    *   Example: A cached index of **Files** for searching.
    *   Example: **Download History** logs.

### 3. SharedPreferences (Legacy)

The older key-value storage mechanism in Android.

**Pros:**
*   Simple, familiar API.
*   Built-in to the framework.

**Cons:**
*   **Blocking:** `get` methods run on the calling thread (often UI thread), causing jank.
*   **No Strong Consistency:** `apply()` is asynchronous but lacks callback/error handling; `commit()` is synchronous.
*   **Runtime Errors:** No type safety guarantees (mostly String/Int).

**Recommendation:** **Avoid.** Use DataStore instead.

### 4. File Storage (Internal / External)

Directly writing files to the app's internal storage or external public storage.

**Pros:**
*   Necessary for media files (images, videos) or user documents.
*   Standard Java/Kotlin IO APIs.

**Cons:**
*   No structure or querying capability.
*   Requires manual permission handling (for external storage).
*   High latency for IO operations.

**Recommendation:**
*   Use for the **Camera Applet** (saving photos/videos).
*   Use for the **Downloads Applet** (saving downloaded files).
*   Use for the **Files Applet** (managing these files).

---

## Kaimera Decision Matrix

| Data Type | Example | Recommended Solution |
| :--- | :--- | :--- |
| **User Preferences** | "Show Grid" in Camera, Homepage in Browser | **Preferences DataStore** |
| **App State** | Last opened folder, window positions | **Preferences DataStore** |
| **Structured User Data** | List of Notes, Todos, Bookmarks | **Room Database** |
| **Media / Blobs** | Photos, Videos, PDF Downloads | **File Storage** |
| **Secret/Auth Tokens** | API Keys | **EncryptedSharedPreferences** (or DataStore + Crypto) |
