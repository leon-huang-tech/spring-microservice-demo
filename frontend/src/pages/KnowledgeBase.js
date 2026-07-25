import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import axiosClient from '../axiosClient';
import Pagination from '../components/Pagination';
import { layout, form, table, button, text } from '../styles/common';

const PAGE_SIZE = 10;

// ---------- API request functions (decoupled from UI) ----------
const fetchDocuments = async (page) => {
  const res = await axiosClient.get(
    `/api/ai/rag/documents?page=${page}&size=${PAGE_SIZE}`
  );
  return res.data.data; // { content, totalPages, ... }
};

const createDocument = (content) =>
  axiosClient.post('/api/ai/rag/documents', { content });

const deleteDocumentReq = (id) =>
  axiosClient.delete(`/api/ai/rag/documents/${id}`);

// Calls the RAG-only Q&A endpoint (retrieval + answer, no Function Calling).
// Used here purely to verify that a given knowledge entry is actually
// retrievable, independent of the main AI Chat page.
const askKnowledgeBase = async (message) => {
  const res = await axiosClient.post('/api/ai/rag/chat', { message });
  return res.data.data; // plain answer string
};

function KnowledgeBase() {
  const queryClient = useQueryClient();

  const [currentPage, setCurrentPage] = useState(0);
  const [newContent, setNewContent] = useState('');
  const [error, setError] = useState('');

  // Separate state for the "test the knowledge base" Q&A box below,
  // kept independent from the "add document" form above.
  const [testMessage, setTestMessage] = useState('');
  const [testAnswer, setTestAnswer] = useState('');

  // ---------- Data fetching + caching (TanStack Query) ----------
  const { data, isLoading, isError } = useQuery({
    queryKey: ['knowledge', currentPage],
    queryFn: () => fetchDocuments(currentPage),
    keepPreviousData: true,
    staleTime: 30_000,
  });

  const documents = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  // ---------- Write operations (Mutations) ----------
  const invalidateDocuments = () =>
    queryClient.invalidateQueries({ queryKey: ['knowledge'] });

  const createMutation = useMutation({
    mutationFn: createDocument,
    onSuccess: () => {
      setNewContent('');
      setCurrentPage(0);
      invalidateDocuments();
    },
    onError: (err) => setError('Failed to add document: ' + err.message),
  });

  const deleteMutation = useMutation({
    mutationFn: deleteDocumentReq,
    onSuccess: () => invalidateDocuments(),
    onError: (err) => setError('Failed to delete: ' + err.message),
  });

  const testKnowledgeBaseMutation = useMutation({
    mutationFn: askKnowledgeBase,
    onSuccess: (answer) => setTestAnswer(answer),
    onError: (err) => setTestAnswer('Error: ' + err.message),
  });

  const handleAdd = () => {
    if (!newContent.trim()) {
      setError('Content cannot be empty.');
      return;
    }
    setError('');
    createMutation.mutate(newContent);
  };

  const handleDelete = (id) => {
    if (!window.confirm('Delete this knowledge entry?')) return;
    deleteMutation.mutate(id);
  };

  const handleTestQuery = () => {
    if (!testMessage.trim()) return;
    setTestAnswer('');
    testKnowledgeBaseMutation.mutate(testMessage);
  };

  return (
    <>
      <h2 style={layout.title}>Knowledge Base</h2>

      {(error || isError) && (
        <p style={text.error}>{error || 'Failed to fetch documents.'}</p>
      )}

      <div style={form.card}>
        <label style={form.label}>Add a new knowledge entry</label>
        <textarea
          style={form.textarea}
          value={newContent}
          onChange={(e) => setNewContent(e.target.value)}
          placeholder="e.g. Orders can have status PENDING, PROCESSING, or COMPLETED."
          rows={3}
        />
        <button
          style={{ ...button.base, ...button.success }}
          onClick={handleAdd}
          disabled={createMutation.isPending}
        >
          {createMutation.isPending ? 'Adding...' : '+ Add to Knowledge Base'}
        </button>
      </div>

      {/*
        Test Knowledge Base — a standalone RAG-only Q&A box, separate from
        the main AI Chat page. It calls /api/ai/rag/chat directly (retrieval
        + answer, no Function Calling), so you can verify a specific entry
        is actually retrievable without the extra variable of tool-calling
        behavior in the main Chat page.
      */}
      <div style={form.card}>
        <label style={form.label}>Test Knowledge Base</label>
        <input
          style={form.input}
          value={testMessage}
          onChange={(e) => setTestMessage(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleTestQuery()}
          placeholder="e.g. What does Alice like?"
        />
        <button
          style={{ ...button.base, ...button.primary }}
          onClick={handleTestQuery}
          disabled={testKnowledgeBaseMutation.isPending}
        >
          {testKnowledgeBaseMutation.isPending ? 'Asking...' : 'Ask'}
        </button>
        {testAnswer && (
          <p style={styles.testAnswer}>
            <strong>Answer:</strong> {testAnswer}
          </p>
        )}
      </div>

      {isLoading ? (
        <p>Loading...</p>
      ) : (
        <>
          <table style={table.table}>
            <thead>
              <tr>
                <th style={table.th}>ID</th>
                <th style={table.th}>Content</th>
                <th style={table.th}>Created</th>
                <th style={table.th}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {documents.map((doc) => (
                <tr key={doc.id}>
                  <td style={table.td}>{doc.id}</td>
                  <td style={table.td}>{doc.content}</td>
                  <td style={table.td}>
                    {new Date(doc.createdAt).toLocaleString()}
                  </td>
                  <td style={table.td}>
                    <button
                      style={{ ...button.base, ...button.danger, ...button.small }}
                      onClick={() => handleDelete(doc.id)}
                      disabled={deleteMutation.isPending}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {documents.length === 0 && (
                <tr>
                  <td style={table.td} colSpan={4}>
                    No knowledge entries yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        </>
      )}
    </>
  );
}

// Page-specific style not covered by styles/common.js
const styles = {
  testAnswer: {
    marginTop: 12,
    padding: '10px 12px',
    backgroundColor: '#f0f7ff',
    borderRadius: '4px',
    fontSize: 14,
  },
};

export default KnowledgeBase;