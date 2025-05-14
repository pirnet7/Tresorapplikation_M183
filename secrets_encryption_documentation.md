# Secrets Encryption: Core Concepts

This document outlines the essential cryptographic concepts for encrypting secrets within the Tresor Application.

## Symmetric Encryption: AES (Advanced Encryption Standard)

AES is a widely-adopted symmetric-key block cipher. "Symmetric" means the same key is used for both encrypting and decrypting data.

*   **Key Sizes**: AES supports cryptographic keys of 128, 192, or 256 bits. Longer keys generally provide stronger security.
*   **Block Size**: AES operates on 128-bit (16-byte) blocks of data.

### AES Modes of Operation

To securely apply AES to more than one block of data, a mode of operation is used. Key considerations include:

*   **ECB (Electronic Code Book)**: The simplest mode. Each block is encrypted independently with the same key. **This mode is insecure** because identical plaintext blocks result in identical ciphertext blocks, revealing patterns. It should not be used.
*   **CBC (Cipher Block Chaining)**: Each plaintext block is XORed with the previous ciphertext block before encryption. An Initialization Vector (IV) is used for the first block. This ensures identical plaintext blocks produce different ciphertext blocks. Requires padding (e.g., PKCS5Padding) if the plaintext isn't a multiple of the block size.
*   **GCM (Galois/Counter Mode)**: A modern and highly recommended mode. It provides both:
    *   **Confidentiality (Encryption)**: Through a counter mode variant.
    *   **Authenticity (Integrity)**: Through an accompanying authentication tag (GMAC). This helps detect if the ciphertext has been tampered with.
    GCM does not require padding and is efficient due to its parallelizable nature. It is critical that the IV is unique for every encryption operation performed with the same key.

### Initialization Vector (IV)

An IV is a random or pseudo-random value used with modes like CBC and GCM. Its purpose is to ensure that encrypting the same plaintext multiple times (even with the same key) produces unique ciphertexts.

*   **Uniqueness**: The IV **must be unique** for each encryption operation performed with the same key. Reusing an IV with the same key can severely compromise security (especially in GCM).
*   **Non-Secret**: The IV does not need to be kept secret. It is typically prepended to the ciphertext or stored alongside it.
*   **Size**: For AES/CBC, a 16-byte (128-bit) IV is common. For AES/GCM, a 12-byte (96-bit) IV is often recommended for performance and security.

### Padding

Block ciphers like AES in modes such as CBC require the input plaintext to be a multiple of the block size (128 bits for AES). If the plaintext is shorter, padding (e.g., PKCS5Padding, PKCS7Padding) is added to fill the last block before encryption. Modes like GCM operate as stream ciphers and do not require padding.

## Key Derivation Functions (KDFs)

Directly using a user's password as an encryption key is highly insecure because passwords often lack the randomness and length required for strong cryptographic keys. KDFs are used to derive a cryptographically strong key of a desired length from a password or other input.

### Purpose:

*   **Key Stretching**: Make the key derivation process computationally intensive (slow) to hinder brute-force attacks against the original password.
*   **Key Generation**: Produce a key of a specific length suitable for the chosen encryption algorithm (e.g., 256 bits for AES-256).

### Common KDFs:

*   **PBKDF2 (Password-Based Key Derivation Function 2)**: A widely used standard. It applies a pseudorandom function (e.g., HMAC-SHA256) to the password and salt, iterated many times.
    *   `PBKDF2WithHmacSHA256` is a common implementation.
*   **scrypt**: Designed to be memory-hard in addition to being CPU-intensive, offering better resistance against custom hardware attacks.
*   **Argon2**: The winner of the Password Hashing Competition (which also applies to key derivation). It is highly configurable and resistant to various attack vectors. `Argon2id` is the recommended variant.

### Salt for KDFs

A salt is a random, non-secret value that is used as an input to the KDF along with the password.

*   **Purpose**: To ensure that even if two users choose the same password, their derived encryption keys will be different (because their salts will be different). This protects against precomputed attacks (like rainbow tables adapted for key derivation).
*   **Uniqueness**: Each user (or each key derivation instance if keys are derived per secret) should have a unique salt.
*   **Storage**: Salts are not secret and must be stored. For user-specific encryption keys, the salt can be stored in the user's record in the database. If deriving keys per secret, the salt would be stored with that secret.

### Iteration Count / Cost Factor

KDFs include a parameter (iteration count for PBKDF2, cost factors for scrypt/Argon2) that controls how computationally expensive, and therefore slow, the key derivation process is. A higher value increases security against brute-force attacks but also increases the time taken for legitimate key derivation.

---

## Implementation in TresorBackend

The encryption of secrets in the `TresorBackend` application has been implemented as follows, adhering to the principle of deriving a unique encryption key per user:

### 1. Key Derivation and User-Specific Salts

*   **User-Specific Keys**: To ensure each user's secrets are encrypted with a unique key, the encryption key is derived from the user's provided `encryptPassword` (the password they use for accessing their secrets) combined with a unique, randomly generated salt stored per user.
*   **Salt Generation & Storage (`SaltGenerator.java`, `UserController.java`, `User.java`, `tresordb.sql`):
    *   A new utility class `SaltGenerator` with a method `generateSaltHex(int numBytes)` creates a cryptographically secure random salt (e.g., 16 bytes) and encodes it as a hexadecimal string.
    *   When a new user is created (`UserController.createUser`), a unique salt is generated using `SaltGenerator.generateSaltHex(16)`.
    *   This salt is stored in a new `user_salt` column (e.g., `VARCHAR(48)`) in the `user` table in the database.
    *   The `User.java` entity has been updated with a `userSalt` field to map to this database column.
*   **Key Derivation Function (`EncryptUtil.java`):
    *   The `EncryptUtil` class constructor now accepts the user's `encryptPassword` and their hex-encoded `userSalt`.
    *   It uses `PBKDF2WithHmacSHA256` as the Key Derivation Function.
    *   A 256-bit AES key is derived using the password, the user's salt, and a standard iteration count (65536).

### 2. Encryption and Decryption Process (`EncryptUtil.java`)

*   **Algorithm**: AES (Advanced Encryption Standard) is used for encryption.
*   **Mode and Padding**: `AES/GCM/NoPadding` is the chosen transformation. This provides:
    *   **Confidentiality**: Through AES encryption in GCM mode.
    *   **Authenticity/Integrity**: GCM includes an authentication tag (128-bit by default) to verify that the ciphertext has not been tampered with.
    *   **No Padding Required**: GCM operates as a stream cipher, so no explicit padding scheme is needed.
*   **Initialization Vector (IV) (`EncryptUtil.java`):
    *   A new, cryptographically secure random 12-byte (96-bit) IV is generated for *every single encryption operation* within the `encrypt` method.
    *   This IV is prepended to the actual ciphertext before Base64 encoding. The stored format is effectively `Base64Encode(IV + Ciphertext)`.
*   **Encryption (`encrypt` method in `EncryptUtil`):
    1.  Generates a fresh 12-byte IV.
    2.  Initializes the AES/GCM cipher in `ENCRYPT_MODE` with the derived key and the new IV.
    3.  Encrypts the plaintext data.
    4.  Concatenates the IV and the ciphertext.
    5.  Returns the Base64 encoded string of `(IV + Ciphertext)`.
*   **Decryption (`decrypt` method in `EncryptUtil`):
    1.  Base64 decodes the input string.
    2.  Extracts the 12-byte IV from the beginning of the decoded data.
    3.  The remaining bytes are the actual ciphertext.
    4.  Initializes the AES/GCM cipher in `DECRYPT_MODE` with the derived key and the extracted IV.
    5.  Decrypts the ciphertext. If the authentication tag (checked implicitly by GCM during decryption) is invalid (e.g., data tampered or wrong key/IV), decryption will fail.
    6.  Returns the plaintext string.

### 3. Controller Logic (`SecretController.java`)

*   **Secret Creation/Update**:
    *   The user's `email` and `encryptPassword` are received (along with the secret `title` and `content`).
    *   The `User` object is fetched using the email to retrieve their `userSalt`.
    *   An `EncryptUtil` instance is created with the `encryptPassword` and the `userSalt`.
    *   The `encrypt` method of `EncryptUtil` is called to encrypt the secret's content.
    *   The `title` and the resulting Base64 encoded string (IV + ciphertext) are stored in the `secret` table.
*   **Secret Retrieval/Decryption**:
    *   When secrets are requested (e.g., `getSecretsByUserId`, `getSecretsByEmail`), the user's `encryptPassword` and `email` (or `userId`) are provided.
    *   The `User` object is fetched to get their `userSalt`.
    *   An `EncryptUtil` instance is created with the `encryptPassword` and `userSalt`.
    *   For each retrieved secret, its `content` (the Base64 string of IV + ciphertext) is passed to the `decrypt` method of `EncryptUtil`.
    *   The decrypted plaintext content is then set on the `Secret` object before being returned to the client.
*   **Error Handling**: Appropriate error handling has been added for cases like user/salt not found, and encryption/decryption failures (e.g., wrong password leading to failed GCM tag verification).

### 4. Database Schema Changes (`tresordb.sql`)

*   **`user` table**: Added a `user_salt VARCHAR(48) NOT NULL` column to store the hex-encoded salt for each user.
*   **`secret` table**: 
    *   Added `title VARCHAR(255) NOT NULL` column.
    *   Changed the `content` column type from `JSON` to `TEXT` to accommodate the Base64 encoded encrypted data (IV + ciphertext).

### 5. Data Models (`User.java`, `Secret.java`, `NewSecret.java`)

*   `User.java`: Added `userSalt` field.
*   `Secret.java`: Added `title` field. Column definition for `content` updated to `TEXT`.
*   `NewSecret.java` (DTO): Added `title` field to be received from the client. 