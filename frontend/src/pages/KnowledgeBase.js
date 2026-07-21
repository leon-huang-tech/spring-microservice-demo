import { useEffect, useState }

  from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

function KnowledgeBase() {
  const [documents, setDocuments] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [newContent, setNewContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const PAGE_SIZE = 10;

  const token = () => `Bearer ${localStorage.getItem('token')}`;

  const fetchDocuments = (page = 0) => {
    setLoading(true);
    axios.get(`http://localhost:8080/api/ai/rag/documents?page=${page}&size=${PAGE_SIZE}`, {
      headers: { Authorization: token() }
    })
      .then(res => {
        setDocuments(res.data.data.content);
        setTotalPages(res.data.data.totalPages);
        setCurrentPage(page);
      })
      .catch(err => {
        console.error('Knowledge base error:', err);
        setError('Failed to fetch documents.');
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    const t = localStorage.getItem('token');
    if (!t) { navigate('/login'); return; }
    fetchDocuments(0);
  }, [navigate]);

  const handleAdd = () => {
    if (!newContent.trim()) {
      setError('Content cannot be empty.');
      return;
    }
    setSubmitting(true);
    setError('');
    axios.post('http://localhost:8080/api/ai/rag/documents',
      { content: newContent },
      { headers: { Authorization: token() } }
    )
      .then(() => {
        setNewContent('');
        fetchDocuments(0);
      })
      .catch(err => setError('Failed to add document: ' + err.message))
      .finally(() => setSubmitting(false));
  };

  const handleDelete = (id) => {
    if (!window.confirm('Delete this knowledge entry?')) return;
    axios.delete(`http://localhost:8080/api/ai/rag/documents/${id}`, {
      headers: { Authorization: token() }
    })
      .then(() => fetchDocuments(currentPage))
      .catch(err => setError('Failed to delete: ' + err.message));
  };

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h2 style={styles.title}>Knowledge Base</h2>
        <div>
          <button style={styles.navButton} onClick={() => navigate('/users')}>
            Users
          </button>
          <button style={styles.navButton} onClick={() => navigate('/orders')}>
            Orders
          </button>
          <button style={styles.navButton} onClick={() => navigate('/chat')}>
            AI Chat
          </button>
          <button style={{ ...styles.navButton, backgroundColor: '#ff4d4f' }}
            onClick={() => { localStorage.removeItem('token'); navigate('/login'); }}>
            Logout
          </button>
        </div>
      </div>

      {error && <p style={styles.error}>{error}</p>}

      <div style={styles.form}>
        <label style={styles.label}>Add a new knowledge entry</label>
        <textarea
          style={styles.textarea}
          value={newContent}
          onChange={e => setNewContent(e.target.value)}
          placeholder="e.g. Orders can have status PENDING, PROCESSING, or COMPLETED.\"
        rows={3}
        />
        <button
          style={{ ...styles.navButton, backgroundColor: '#52c41a', marginLeft: 0 }}
          onClick={handleAdd}
          disabled={submitting}>
          {submitting ? 'Adding...' : '+ Add to Knowledge Base'}
        </button>
      </div>

      {loading ? (
        <p>Loading...</p>
      ) : (
        <>
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>ID</th>
                <th style={styles.th}>Content</th>
                <th style={styles.th}>Created</th>
                <th style={styles.th}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {documents.map(doc => (
                <tr key={doc.id}>
                  <td style={styles.td}>{doc.id}</td>
                  <td style={styles.td}>{doc.content}</td>
                  <td style={styles.td}>
                    {new Date(doc.createdAt).toLocaleString()}
                  </td>
                  <td style={styles.td}>
                    <button
                      style={{ ...styles.actionBtn, backgroundColor: '#ff4d4f' }}
                      onClick={() => handleDelete(doc.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {documents.length === 0 && (
                <tr>
                  <td style={styles.td} colSpan={4}>
                    No knowledge entries yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          <div style={styles.pagination}>
            <button style={styles.pageBtn}
              disabled={currentPage === 0}
              onClick={() => fetchDocuments(currentPage - 1)}>
              Previous
            </button>
            <span style={{ margin: '0 16px' }}>
              Page {currentPage + 1} of {Math.max(totalPages, 1)}
            </span>
            <button style={styles.pageBtn}
              disabled={currentPage >= totalPages - 1}
              onClick={() => fetchDocuments(currentPage + 1)}>
              Next
            </button>
          </div>
        </>
      )}
    </div>
  );
}

const styles = {
  container: { padding: '24px', maxWidth: '1000px', margin: '0 auto' },
  header: {
    display: 'flex', justifyContent: 'space-between',
    alignItems: 'center', marginBottom: '16px', flexWrap: 'wrap', gap: 8
  },
  title: { margin: 0, color: '#333' },
  navButton: {
    padding: '8px 12px', backgroundColor: '#1890ff',
    color: 'white', border: 'none', borderRadius: '4px',
    cursor: 'pointer', marginLeft: 8
  },
  form: {
    backgroundColor: 'white', padding: '20px',
    borderRadius: '8px', marginBottom: '16px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
  },
  label: {
    display: 'block', marginBottom: 8,
    fontWeight: 'bold', fontSize: 13
  },
  textarea: {
    width: '100%', padding: '8px', marginBottom: '12px',
    borderRadius: '4px', border: '1px solid #ddd',
    boxSizing: 'border-box', fontFamily: 'inherit', fontSize: 14
  },
  table: {
    width: '100%', borderCollapse: 'collapse',
    backgroundColor: 'white', boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    borderRadius: '8px', overflow: 'hidden'
  },
  th: {
    padding: '12px 16px', backgroundColor: '#1890ff',
    color: 'white', textAlign: 'left'
  },
  td: { padding: '10px 16px', borderBottom: '1px solid #f0f0f0' },
  actionBtn: {
    padding: '4px 10px', color: 'white', border: 'none',
    borderRadius: '4px', cursor: 'pointer', fontSize: 12
  },
  pagination: {
    display: 'flex', justifyContent: 'center',
    alignItems: 'center', marginTop: 16
  },
  pageBtn: {
    padding: '8px 16px', backgroundColor: '#1890ff',
    color: 'white', border: 'none', borderRadius: '4px',
    cursor: 'pointer'
  },
  error: { color: 'red' },
};

export default KnowledgeBase;