To make the entire 8-gear pipeline run for real instead of as a simulation, we need to bridge the gap between the math/logic we just built (in the :model layer) and the actual image data coming from the camera.
Right now, the app is taking a "Shortcut" (V1): It takes the raw Bitmap, sends the entire image directly to Firebase Vertex AI (Gemini Multimodal), and asks the AI to do all the reading, math, and parsing in one go.
To use the True Deterministic Pipeline (V2) that we just built, the system must process the math locally before talking to the AI.
Here is exactly what is missing to wire it up for real:
1. Upgraded ML Kit OCR Engine (Bounding Boxes)
   Currently, LocalOcrEngine.kt just extracts a giant String of text from the image.
   •
   What's missing: We need it to return the actual Rect bounding boxes, corner points, and rotationDegrees from ML Kit so we can convert them into NormalizedRect objects to feed into the SpatialLayoutEngine (Gear 3).
2. The Local Candidate Generator (Heuristics)
   •
   What's missing: A class that loops over the OCR text blocks and runs Regex to find dates, amounts, and merchants, and feeds them into the StrictMoneyParser (Gear 4) to generate the AmountCandidate and DateCandidate lists.
3. The Real PipelineOrchestrator
   Currently, AddTransactionViewModel simulates the logs because the real orchestration class doesn't exist yet.
   •
   What's missing: A new DocumentPipelineOrchestrator that executes the 8 gears in strict order:
   i.
   Encrypts image to disk using TinkStreamingFileEncryptor.
   ii.
   Calls ML Kit to get the bounding boxes.
   iii.
   Runs CoordinateTransformMapper and SpatialLayoutEngine to do the math.
   iv.
   Runs DeterministicConfidenceGate (Gear 5).
   v.
   If it needs AI, runs BundleTrimmer (Gear 6) to strip PII.
   vi.
   Sends the text-only redacted JSON to Firebase AI instead of the raw image.
   vii.
   Runs PostLlmSemanticValidator (Gear 7) on the result.
4. Advanced Database Wiring
   Currently, AddTransactionViewModel.saveTransaction() just saves a basic Transaction data class.
   •
   What's missing: It needs to interact with the new FinancialDocumentDao to save the FinancialDocumentEntity, OcrElementEntity, and generate the ManifestHmacGenerator hashes (Gear 8).