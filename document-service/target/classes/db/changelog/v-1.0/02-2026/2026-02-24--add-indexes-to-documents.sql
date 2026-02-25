--liquibase formatted sql
--changeset Alexandr Makutsevich:2026-02-18--add-indexes-to-documents.sql
CREATE INDEX idx_history_document_id ON history (document_id);

CREATE INDEX idx_documents_created_at ON documents (created_at DESC);

CREATE INDEX idx_document_status_authot_created_at ON documents(status, author, created_at);
