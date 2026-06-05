import { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const STATUS_OPTIONS = ['PENDING', 'PROCESSING', 'COMPLETED'];
const USER_OPTIONS = [
  { id: 1, name: 'Alice' },
  { id: 2, name: 'Bob' },
  { id: 3, name: 'Charlie' },
];

function Orders() {
  const [orders, setOrders] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editOrder, setEditOrder] = useState(null);
  const [form, setForm] = useState({
    userId: 1,
    product: '',
    amount: '',
    status: 'PENDING'
  });
  const navigate = useNavigate();
  const PAGE_SIZE = 5;

  const token = () => `Bearer ${localStorage.getItem('token')}`;

  const fetchOrders = (page = 0) => {
    setLoading(true);
    axios.get(`http://localhost:8080/api/orders/paged?page=${page}&size=${PAGE_SIZE}`, {
      headers: { Authorization: token() }
    })
    .then(res => {
      setOrders(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
      setCurrentPage(page);
    })
    .catch(err => {
      console.error('Orders error:', err);
      setError('Failed to fetch orders.');
    })
    .finally(() => setLoading(false));
  };

  useEffect(() => {
    const t = localStorage.getItem('token');
    if (!t) { navigate('/login'); return; }
    fetchOrders(0);
  }, [navigate]);

  const resetForm = () => {
    setForm({ userId: 1, product: '', amount: '', status: 'PENDING' });
    setEditOrder(null);
    setShowForm(false);
  };

  const handleSubmit = () => {
    if (!form.product || !form.amount) {
      setError('Product and amount are required.');
      return;
    }
    const payload = {
      userId: Number(form.userId),
      product: form.product,
      amount: Number(form.amount),
      status: form.status
    };

    const request = editOrder
      ? axios.put(`http://localhost:8080/api/orders/${editOrder.id}`,
          payload, { headers: { Authorization: token() } })
      : axios.post('http://localhost:8080/api/orders',
          payload, { headers: { Authorization: token() } });

    request
      .then(() => { resetForm(); fetchOrders(currentPage); })
      .catch(err => setError('Failed to save order: ' + err.message));
  };

  const handleEdit = (order) => {
    setEditOrder(order);
    setForm({
      userId: order.userId,
      product: order.product,
      amount: order.amount,
      status: order.status
    });
    setShowForm(true);
  };

  const handleDelete = (id) => {
    if (!window.confirm('Delete this order?')) return;
    axios.delete(`http://localhost:8080/api/orders/${id}`, {
      headers: { Authorization: token() }
    })
    .then(() => fetchOrders(currentPage))
    .catch(err => setError('Failed to delete: ' + err.message));
  };

  const statusColor = (status) => {
    switch (status) {
      case 'COMPLETED':  return '#52c41a';
      case 'PENDING':    return '#faad14';
      case 'PROCESSING': return '#1890ff';
      default:           return '#999';
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h2 style={styles.title}>Order List</h2>
        <div>
          <button style={styles.navButton} onClick={() => navigate('/users')}>
            View Users
          </button>
          <button style={styles.navButton} onClick={() => navigate('/chat')}>
            AI Chat
          </button>
          <button style={{...styles.navButton, backgroundColor: '#52c41a'}}
            onClick={() => { resetForm(); setShowForm(true); }}>
            + New Order
          </button>
          <button style={{...styles.navButton, backgroundColor: '#ff4d4f'}}
            onClick={() => { localStorage.removeItem('token'); navigate('/login'); }}>
            Logout
          </button>
        </div>
      </div>

      {error && <p style={styles.error}>{error}</p>}

      {showForm && (
        <div style={styles.form}>
          <h3>{editOrder ? 'Edit Order' : 'New Order'}</h3>

          <label style={styles.label}>User</label>
          <select style={styles.input}
            value={form.userId}
            onChange={e => setForm({...form, userId: e.target.value})}>
            {USER_OPTIONS.map(u => (
              <option key={u.id} value={u.id}>{u.name}</option>
            ))}
          </select>

          <label style={styles.label}>Product</label>
          <input style={styles.input}
            value={form.product}
            onChange={e => setForm({...form, product: e.target.value})}
            placeholder="Product name" />

          <label style={styles.label}>Amount ($)</label>
          <input style={styles.input}
            type="number"
            value={form.amount}
            onChange={e => setForm({...form, amount: e.target.value})}
            placeholder="0.00" />

          <label style={styles.label}>Status</label>
          <select style={styles.input}
            value={form.status}
            onChange={e => setForm({...form, status: e.target.value})}>
            {STATUS_OPTIONS.map(s => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>

          <div style={{display: 'flex', gap: 8, marginTop: 8}}>
            <button style={{...styles.navButton, backgroundColor: '#52c41a'}}
              onClick={handleSubmit}>
              {editOrder ? 'Update' : 'Create'}
            </button>
            <button style={{...styles.navButton, backgroundColor: '#999'}}
              onClick={resetForm}>
              Cancel
            </button>
          </div>
        </div>
      )}

      {loading ? (
        <p>Loading...</p>
      ) : (
        <>
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>ID</th>
                <th style={styles.th}>User ID</th>
                <th style={styles.th}>Product</th>
                <th style={styles.th}>Amount</th>
                <th style={styles.th}>Status</th>
                <th style={styles.th}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => (
                <tr key={order.id}>
                  <td style={styles.td}>{order.id}</td>
                  <td style={styles.td}>{order.userId}</td>
                  <td style={styles.td}>{order.product}</td>
                  <td style={styles.td}>${order.amount}</td>
                  <td style={styles.td}>
                    <span style={{
                      ...styles.badge,
                      backgroundColor: statusColor(order.status)
                    }}>
                      {order.status}
                    </span>
                  </td>
                  <td style={styles.td}>
                    <button style={{...styles.actionBtn, backgroundColor: '#1890ff'}}
                      onClick={() => handleEdit(order)}>
                      Edit
                    </button>
                    <button style={{...styles.actionBtn, backgroundColor: '#ff4d4f'}}
                      onClick={() => handleDelete(order.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div style={styles.pagination}>
            <button style={styles.pageBtn}
              disabled={currentPage === 0}
              onClick={() => fetchOrders(currentPage - 1)}>
              Previous
            </button>
            <span style={{margin: '0 16px'}}>
              Page {currentPage + 1} of {totalPages}
            </span>
            <button style={styles.pageBtn}
              disabled={currentPage >= totalPages - 1}
              onClick={() => fetchOrders(currentPage + 1)}>
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
  header: { display: 'flex', justifyContent: 'space-between',
    alignItems: 'center', marginBottom: '16px', flexWrap: 'wrap', gap: 8 },
  title: { margin: 0, color: '#333' },
  navButton: { padding: '8px 12px', backgroundColor: '#1890ff',
    color: 'white', border: 'none', borderRadius: '4px',
    cursor: 'pointer', marginLeft: 8 },
  form: { backgroundColor: 'white', padding: '20px',
    borderRadius: '8px', marginBottom: '16px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)' },
  label: { display: 'block', marginBottom: 4,
    fontWeight: 'bold', fontSize: 13 },
  input: { width: '100%', padding: '8px', marginBottom: '12px',
    borderRadius: '4px', border: '1px solid #ddd',
    boxSizing: 'border-box' },
  table: { width: '100%', borderCollapse: 'collapse',
    backgroundColor: 'white', boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    borderRadius: '8px', overflow: 'hidden' },
  th: { padding: '12px 16px', backgroundColor: '#1890ff',
    color: 'white', textAlign: 'left' },
  td: { padding: '10px 16px', borderBottom: '1px solid #f0f0f0' },
  badge: { padding: '2px 8px', borderRadius: '4px',
    color: 'white', fontSize: '12px' },
  actionBtn: { padding: '4px 10px', color: 'white', border: 'none',
    borderRadius: '4px', cursor: 'pointer', marginRight: 4, fontSize: 12 },
  pagination: { display: 'flex', justifyContent: 'center',
    alignItems: 'center', marginTop: 16 },
  pageBtn: { padding: '8px 16px', backgroundColor: '#1890ff',
    color: 'white', border: 'none', borderRadius: '4px',
    cursor: 'pointer' },
  error: { color: 'red' },
};

export default Orders;