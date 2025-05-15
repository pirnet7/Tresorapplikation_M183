# Password Hashing, Salting, and Peppering Essentials

## Password Hashing

Password hashing is a one-way process of transforming a plaintext password into a fixed-length string of characters, called a hash. This process is irreversible, meaning the original password cannot be obtained from the hash. This is crucial for security, as even if a database containing password hashes is compromised, the actual passwords remain unknown.

### Recommended Hashing Algorithms:

*   **Argon2id**:
    *   Winner of the 2015 Password Hashing Competition.
    *   Resistant to GPU cracking and side-channel attacks.
    *   Recommended minimum configuration: 19 MiB memory, 2 iterations, 1 degree of parallelism.
*   **scrypt**:
    *   Use if Argon2id is not available.
    *   Memory-hard, making custom hardware attacks more costly.
    *   Recommended minimum configuration: CPU/memory cost (2^17), block size 8 (1024 bytes), parallelism 1.
*   **bcrypt**:
    *   Suitable for legacy systems if Argon2id/scrypt are unavailable.
    *   Requires a work factor of 10+ and has a 72-byte password input limit.
*   **PBKDF2**:
    *   Preferred when FIPS-140 compliance is necessary.
    *   Recommended configuration: Work factor of 600,000+ with HMAC-SHA-256.

### Key Characteristics:

*   **One-way Function**: Unlike encryption, hashing cannot be reversed.
*   **Deterministic**: The same input password will always produce the same hash output (when using the same salt).
*   **Collision Resistant**: It should be computationally infeasible to find two different passwords that produce the same hash.
*   **Slow**: Hashing algorithms are intentionally designed to be computationally intensive (slow) to make brute-force attacks impractical. This is controlled by a "work factor" or "iteration count".

## Salting

A **salt** is a unique, randomly generated string of characters that is added to each user's password *before* it is hashed. The salt is then typically stored alongside the hashed password in the database.

### Importance of Salting:

*   **Defeats Rainbow Table Attacks**: Rainbow tables are precomputed lists of hashes for common passwords. Since each password has a unique salt, an attacker cannot use a generic rainbow table. They would need to compute a separate table for every unique salt, which is infeasible.
*   **Prevents Identical Hashes for Identical Passwords**: If two users happen to choose the same password, their unique salts will ensure their stored password hashes are different. This prevents an attacker from identifying users with common passwords.

Most modern password hashing algorithms (Argon2id, scrypt, bcrypt) automatically generate and manage salts.

## Peppering

A **pepper** is a secret key that is added to the password (or the salted password) before hashing, similar to a salt. However, unlike salts, a pepper is:

*   **Shared**: The same pepper is used for all passwords in the system.
*   **Secret and Stored Separately**: The pepper is a secret value that should **not** be stored in the same database as the password hashes. It should be stored securely, for example, in a hardware security module (HSM), a configuration file with restricted access outside the webroot, or a secrets management system.

### Purpose of Peppering:

*   **Added Security Layer**: If an attacker gains access only to the database (e.g., via SQL injection) but not the pepper, they still cannot compute the correct hashes to try and crack them offline, even if they have the salts. The pepper adds an extra unknown variable.

While salting is essential, peppering provides an additional defense-in-depth measure.

## Work Factor / Iteration Count

Password hashing algorithms incorporate a "work factor" (e.g., in bcrypt) or an "iteration count" (e.g., in Argon2id, scrypt, PBKDF2). This parameter controls how computationally expensive (and therefore how slow) the hashing process is.

*   **Slowing Down Attackers**: A higher work factor significantly increases the time it takes for an attacker to try to guess passwords through brute-force or dictionary attacks.
*   **Balancing Security and Performance**: The work factor must be chosen carefully. It should be high enough to deter attackers but not so high that it unacceptably degrades server performance during legitimate login attempts. A slow login process can also be a vector for Denial of Service (DoS) attacks.
*   **Adjustable**: The work factor should be increased over time as computing power generally increases, to maintain the same level of security. This can often be done by re-hashing the user's password with the new, higher work factor when they next log in.

---
This documentation covers the core concepts of password hashing, salting, and peppering, fulfilling the initial documentation tasks.

## Implementation in TresorBackend

The password security enhancements have been implemented in the Spring Boot `tresorbackend` application as follows:

### 1. Hashing Mechanism (`PasswordEncryptionService`)

*   A `PasswordEncryptionService` is utilized for all password hashing and verification operations.
*   This service uses `org.springframework.security.crypto.bcrypt.BCrypt` for hashing.
*   **Salting**: BCrypt automatically handles the generation of unique salts for each password and embeds the salt within the resulting hash string. The work factor is set to 12 (e.g., `BCrypt.gensalt(12)`).
*   **Peppering**: A system-wide secret, the "pepper", is concatenated with the plaintext password *before* hashing. 
    *   The pepper is configured in `src/main/resources/application.properties` via the `password.pepper` property.
    *   Example: `password.pepper=v7cnK9pBdTzxu1nLrqK7lzITymYy7X8ZmFuDgiW6BBI=` (This should be a unique, strong random secret for each deployment).

### 2. User Registration (`UserController`)

*   When a new user registers, the `UserController`'s `createUser` method is called.
*   The provided plaintext password is first combined with the configured pepper.
*   The `PasswordEncryptionService.hashPassword()` method is then used to compute the BCrypt hash of the peppered password.
*   The resulting hash (which includes the salt) is stored in the `password` column of the `user` table in the database. The column type has been changed from `longtext` to `VARCHAR(72)` to efficiently store BCrypt hashes.

### 3. User Login (`UserController`)

*   A new `/api/users/login` POST endpoint has been added to `UserController`.
*   The user submits their email and plaintext password.
*   The system retrieves the stored BCrypt hash for the given email.
*   The submitted plaintext password is combined with the same system-wide pepper.
*   `PasswordEncryptionService.verifyPassword()` is used. This method internally uses `BCrypt.checkpw()`, which extracts the salt from the stored hash and compares the peppered, newly hashed input password with the stored hash.
*   Access is granted if the verification is successful.

### 4. Migration of Existing Passwords (`PasswordMigrationRunner`)

*   A `CommandLineRunner` named `PasswordMigrationRunner` has been implemented.
*   On application startup, this runner fetches all users from the database.
*   For each user, it checks if their current password appears to be plaintext (i.e., not a BCrypt hash).
*   If it's plaintext, the password is treated as the original password, peppered, and then hashed using `PasswordEncryptionService`.
*   The user record is updated in the database with the new hashed password.
*   This runner is designed to execute once to secure existing plaintext passwords. The `user` table's `password` column in `tresordb.sql` has been updated to `VARCHAR(72)`.

This summarizes the core implementation details for secure password handling in the Tresor Application backend. 