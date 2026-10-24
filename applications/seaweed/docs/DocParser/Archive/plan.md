I will now generate the complete, implementation-ready technical specification for your AI coding agent, combining your local-first deterministic document pipeline with the multi-provider AI Configuration UI and fallback routing architecture from your screenshot.

I have updated `ImagePipeline.md` into a comprehensive, end-to-end engineering specification tailored for an AI coding agent.

### Key Highlights of What Was Added:

1. **AI Configuration UI & Multi-Provider Routing (`Section 5`):** Matches your screenshot's exact layout, including the live health indicator chain ($\text{BYOK} \rightarrow \text{Local AI} \rightarrow \text{Firebase} \rightarrow \text{Gemma}$), the 4-provider dropdown (`Gemma 4 Cloud (Free Tier) - Default`, `Firebase AI Logic (Cloud)`, `Bring Your Own Key (BYOK)`, `On-Device Local AI (Gemini Nano)`), the locked `"Test connection to see models"` state machine, the bottom-right diagnostics bug icon, and ready-to-compile Jetpack Compose code.
2. **Concrete Kotlin Contracts & Room/SQLCipher Entities (`Sections 4 & 7`):** Ready-to-use `@Serializable` data classes and `@Entity` definitions using integer minor units (`amount_minor: Long`) to eliminate floating-point rounding bugs.
3. **Curved Receipt & Native PDF Handling (`Sections 2 & 3`):** Includes the mathematical formula for rotation-aware line projection (for curled thermal paper), CIELAB perceptual color distance, and a native digital PDF fast-path.
4. **Phased Agent Execution Plan (`Section 9`):** Gives your AI coder a strict 5-phase implementation and unit-testing checklist so it can build the codebase systematically.

I will now generate the upgraded, production-ready 10/10 engineering specification in `ImagePipeline.md`, incorporating every architectural, security, financial-domain, and testing refinement from your review.

I have upgraded `ImagePipeline.md` into a complete, self-contained 10/10 engineering specification that addresses every architectural, security, domain-modeling, and testing critique from your review:

1. **Hosted Gemma (`GEMMA_4_CLOUD_FREE`) as the True Default & Corrected Model IDs:** Replaced `gemma-4-27b-it` with Google's documented hosted Gemma IDs (`gemma-4-31b-it` and `gemma-4-26b-a4b-it`) routed via an App Check–authenticated backend proxy so no shared API key is ever embedded in the APK.
2. **Provider Capability Separation & Mandatory App Check:** Explicitly defined `FIREBASE_AI_LOGIC` as a Gemini-model route (`gemini-2.5-flash`) with Play Integrity App Check enforcement ahead of the November 2, 2026 requirement, and upgraded `BYOK_GEMINI` storage from deprecated `EncryptedSharedPreferences` to **Jetpack DataStore + Google Tink (`Aead`)** backed by the Android Keystore.
3. **Financial Domain Event Separation & Scale-Aware Money (`CurrencyAmount`):** Separated document families (`RECEIPT`, `INVOICE_OR_UTILITY_BILL`, `BANK_STATEMENT`, `CREDIT_CARD_STATEMENT`, `REFUND_OR_CREDIT_NOTE`) from financial events (`PURCHASE_EXPENSE`, `UNPAID_BILL_LIABILITY`, `INTERNAL_OR_CC_TRANSFER`, `REFUND_OR_CREDIT`, `STATEMENT_BALANCE_IGNORE`) to prevent double-counting credit card bills or unpaid invoices, and added explicit ISO 4217 `currencyScale` support (`0`, `2`, or `3` decimal places).
4. **Homography Transforms, Page-Scoped IDs & Safe Diagnostics:** Added the $3 \times 3$ inverse homography matrix ($M_{\text{ocr}\rightarrow\text{raw}}$) for accurate overlay alignment on skewed/cropped captures, page-scoped evidence IDs (`P01_T0014`), semantic-role validation, a 12 KB / 15-row `BundleTrimmer`, and safe-by-default diagnostics.