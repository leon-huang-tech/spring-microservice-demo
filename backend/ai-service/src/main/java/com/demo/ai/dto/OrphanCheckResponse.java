package com.demo.ai.dto;

import com.demo.ai.model.KnowledgeDocument;
import java.util.List;

/**
 * Result of an orphan-data check between the pgvector table and the
 * knowledge_documents metadata table.
 *
 * @param vectorsWithoutMetadata vector store IDs that have no matching row
 *                               in knowledge_documents (vector exists,
 *                               metadata is missing)
 * @param metadataWithoutVector  knowledge_documents rows whose vectorStoreId
 *                               does not exist in the vector store anymore
 *                               (metadata exists, vector is missing)
 */
public record OrphanCheckResponse(
    List<String> vectorsWithoutMetadata,
    List<KnowledgeDocument> metadataWithoutVector
) {
}
