package com.zoewave.probase.seaweed.database.finance

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction

@Dao
interface FinancialDocumentDao {

    @Query("DELETE FROM ocr_elements WHERE documentId = :documentId")
    suspend fun deleteOcrElementsForDocument(documentId: String)

    @Query("UPDATE document_pages SET rawDisplayEncryptedPath = NULL, cleanOcrEncryptedPath = NULL WHERE documentId = :documentId")
    suspend fun clearPageImagePaths(documentId: String)

    @Query("UPDATE evidence_manifests SET quadRawDisplayNormalizedJson = NULL WHERE documentId = :documentId")
    suspend fun clearManifestQuads(documentId: String)

    @Query("UPDATE financial_documents SET rawEvidencePurged = 1 WHERE documentId = :documentId")
    suspend fun markDocumentRawEvidencePurged(documentId: String)

    @Query("DELETE FROM financial_events WHERE documentId = :documentId AND persistenceState = 'NEEDS_USER_REVIEW_DRAFT'")
    suspend fun deleteUnverifiedDraftEventsForDocument(documentId: String)

    @Query("DELETE FROM financial_events WHERE documentId = :documentId")
    suspend fun deleteAllEventsForDocument(documentId: String)

    @Query("DELETE FROM evidence_manifests WHERE documentId = :documentId")
    suspend fun deleteAllEvidenceManifestsForDocument(documentId: String)

    @Query("DELETE FROM financial_documents WHERE documentId = :documentId")
    suspend fun deleteFinancialDocumentRow(documentId: String)

    /**
     * Invoked by PurgeExpiredEvidenceWorker 30 days after document verification
     * (after encrypted bitmap files on disk have been securely deleted).
     */
    @Transaction
    suspend fun purgeRawEvidenceMetadata(documentId: String) {
        deleteOcrElementsForDocument(documentId)
        clearPageImagePaths(documentId)
        clearManifestQuads(documentId)
        markDocumentRawEvidencePurged(documentId)
    }

    /**
     * Option 1 in User Delete Dialog: Deletes the document, pages, OCR rows, and unverified drafts,
     * while verified FinancialEventEntity rows survive with documentId = NULL via ForeignKey.SET_NULL.
     * Keeps evidence_manifests if dictated by policy, but can cascade.
     */
    @Transaction
    suspend fun deleteSourceDocumentPreservingVerifiedEvents(documentId: String) {
        deleteUnverifiedDraftEventsForDocument(documentId)
        deleteFinancialDocumentRow(documentId)
    }

    /**
     * Option 2 in User Delete Dialog: Deletes the document AND all derived financial events.
     */
    @Transaction
    suspend fun deleteDocumentAndAllDerivedEvents(documentId: String) {
        deleteAllEventsForDocument(documentId)
        deleteAllEvidenceManifestsForDocument(documentId)
        deleteFinancialDocumentRow(documentId)
    }
}
