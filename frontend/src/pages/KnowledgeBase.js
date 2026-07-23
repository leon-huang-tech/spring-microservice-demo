import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axiosClient from '../axiosClient';
import Pagination from '../components/Pagination';
import { layout, form, table, button, text } from '../styles/common';

const PAGE_SIZE = 10;

// ---------- API Request Functions (Decoupled from UI) ----------
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

function KnowledgeBase() {
  const queryClient = useQueryClient();

  const [currentPage, setCurrentPage] = useState(0);
  const [newContent, setNewContent] = useState('');
  const [error, setError] = useState('');

  // ---------- Data Fetching & Caching (TanStack Query) ----------
  const { data, isLoading, isError } = useQuery({
    queryKey: ['knowledge', currentPage],
    queryFn: () => fetchDocuments(currentPage),
    keepPreviousData: true,
    staleTime: 30_000,
  });

  const documents = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  // ---------- Mutation ----------
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

export default KnowledgeBase;
