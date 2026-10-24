# Seaweed App: Security, Safety & Privacy Architecture

This document details the comprehensive security, safety, and privacy infrastructure engineered into the Seaweed financial application. The architecture strictly enforces a "local-first, least-privilege, mathematically verifiable" approach to handling highly sensitive user financial data, transaction history, and document scans.

---

## 1. Zero-Knowledge Local Encryption (Data at Rest)

Seaweed ensures that all persistent financial data and document images are heavily encrypted at rest, securely bound to the device's hardware hardware-backed Keystore.

*   **SQLCipher Database Encryption**: 
    *   The primary Room database is encrypted using SQLCipher (AES-256).
    *   The 256-bit passphrase is mathematically generated on first launch and wrapped by a dedicated Google Tink `Aead` keyset (`android-keystore://finance_db_master_key`). The raw passphrase is never written to disk.
*   **Per-File Image Encryption (Google Tink `StreamingAead`)**:
    *   Raw receipt/document bitmaps (`raw_display_page.enc` and `clean_ocr_page.enc`) are encrypted using `AES256_GCM_HKDF_4KB`.
    *   **Cryptographic Binding (Associated Data)**: Each file encryption stream requires associated data (`AD = "$documentId:$pageNumber:$variant"`). This prevents malicious local actors or compromised file systems from silently swapping one encrypted receipt image with another.

## 2. Privacy-Preserving AI & PII Redaction

To process financial documents without exposing sensitive personal identifiable information (PII) to cloud LLMs, Seaweed implements aggressive redaction gates before any data leaves the device.

*   **Local-First Preprocessing**: OCR, table layout reconstruction, homography coordinate transforms, and perceptual color analysis execute 100% on the local device hardware.
*   **Aggressive PII Redaction (`BundleTrimmer.kt`)**: 
    *   Before sending any fallback chunk to the AI, the engine strips data matching explicit PII `SemanticRole` tags (e.g., `CUSTOMER_ADDRESS`, `ACCOUNT_HOLDER_NAME`, `CUSTOMER_PHONE`, `CUSTOMER_EMAIL`).
    *   A rigorous Regex pipeline scrubs all remaining free-text fields (including notes and generic line items), replacing sensitive sequences with `[REDACTED_EMAIL]`, `[REDACTED_PHONE]`, or masking bank/credit accounts to `****1234`.
*   **Multi-Provider AI Isolation**: 
    *   **Hosted Cloud Proxy**: By default, cloud interactions route through Firebase AI Logic via an App Check-authenticated backend proxy. No raw Gemini API keys are embedded in the APK.
    *   **Bring Your Own Key (BYOK)**: If users supply their own Gemini key, it is immediately encrypted into the Tink-secured vault.
    *   **Local AI (Gemini Nano)**: For supported devices, transactions route entirely through the local Edge TPU/NPU, guaranteeing zero network egress.

## 3. Automated Data Retention & Erasure Lifecycle

To comply with data minimization and stringent privacy policies, Seaweed enforces a strictly automated data lifecycle.

*   **30-Day Automated Purge (`PurgeExpiredEvidenceWorker`)**: A background `CoroutineWorker` continuously monitors the repository. Documents verified for more than 30 days trigger an irreversible deletion sequence that securely deletes the encrypted `.enc` image files and drops raw OCR text rows.
*   **Honest Post-Purge UI Badging**: To maintain absolute transparency, once the automated purge completes, the UI explicitly drops the visual image canvas and replaces it with a badge reading: *"Provenance Metadata Retained (Original scan & OCR text purged per 30-day privacy policy; source visual re-verification unavailable)"*.
*   **Foreign-Key Event Safety**: Deleting a source document strictly cascades metadata deletion (`onDelete = ForeignKey.CASCADE`), but retains the finalized, ledger-reconciled financial event with a nullified document reference (`onDelete = ForeignKey.SET_NULL`). 

## 4. Cryptographic Provenance & Safety Gates

Because raw OCR evidence is purged after 30 days, Seaweed must be able to prove that a financial transaction hasn't been corrupted or altered without keeping the raw text around.

*   **HMAC-SHA256 Digest Verification (`ManifestHmacGenerator.kt`)**:
    *   Instead of keeping raw receipt text forever, the system computes a keyed hash (`HMAC_SHA256_256BITTAG` via Google Tink) combining the `$documentId`, `$evidenceId`, `$semanticRole`, and the parsed `$normalizedValue`.
    *   This hash allows Seaweed to verify that a $15.00 lunch transaction originated exactly from a specific coordinate cell on the receipt, even after the receipt image is physically destroyed.
*   **Failure-Safe Homography Projection**: Mathematical transforms bounding box coordinates safely reject singular matrices (determinant = 0), preventing divide-by-zero crashes and silent spatial corruptions.
*   **Strict Non-Rounding Currency Math**: Decimal parsing strictly rejects precision mismatches (e.g., parsing `"12.345 USD"`) instead of silently rounding it to `12.35`. It flags this as `ExcessPrecisionFlagged` and forces user review.

## 5. OS-Level Device Protections

*   **Android Backup Exclusions**: Configured `data_extraction_rules.xml` and `backup_rules.xml` to explicitly exclude the SQLCipher `.db` files, the encrypted image directory, and the Android Keystore-linked SharedPreferences from cloud backups and device-to-device ADB transfers. This ensures ciphertext never leaves the device hardware where its Keystore master key resides.
*   **Hardware-Backed Android Keystore**: All Tink primary keys and SQLCipher passphrases are encrypted by an overarching Master Key (`android-keystore://finance_engine_master_key`) rooted securely in the device's Trusted Execution Environment (TEE) or Secure Element (SE).