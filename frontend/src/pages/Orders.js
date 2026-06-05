import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function Orders() {
    const [orders, setOrders] = useState([]);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/login');
            return;
        }

        axios.get('http://localhost:8080/api/orders', {
            headers: { Authorization: `Bearer ${token}` }
        })
            .then(res => setOrders(res.data))
            .catch((err) => {
                //setError('Failed to fetch orders. Please log in again.');
                // localStorage.removeItem('token');
                // navigate('/login');
                console.error('Orders error:', err);
                setError('Failed to fetch orders: ' + err.message);
            });
    }, [navigate]);

    const statusColors = (status) => {
        switch (status) {
            case 'COMPLETED': return '#52c41a';
            case 'PENDING': return '#faad14';
            case 'PROCESSING': return '#1890ff';
            default: return '#999';
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.header} >
                <h2 style={styles.title}>Order List</h2>
                <button style={styles.navButton}
                    onClick={() => navigate('/users')}>
                    View Users
                </button>
                <button style={styles.navButton}
                    onClick={() => navigate('/chat')}>
                    AI Chat
                </button>
                <button style={{ ...styles.navButton, backgroundColor: '#ff4d4f', marginLeft: 8 }}
                    onClick={() => {
                        localStorage.removeItem('token');
                        navigate('/login');
                    }}>
                    Logout
                </button>
            </div>

            {error && <p style={styles.error}>{error}</p>}

            <table style={styles.table}>
                <thead>
                    <tr>
                        <th style={styles.th}>ID</th>
                        <th style={styles.th}>User ID</th>
                        <th style={styles.th}>Product</th>
                        <th style={styles.th}>Amount</th>
                        <th style={styles.th}>Status</th>
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
                                    backgroundColor: statusColors(order.status),
                                }}>
                                    {order.status}
                                </span>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

const styles = {
    container: {
        padding: '40px',
        maxwidth: '900px',
        margin: '0 auto',
    },
    header: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '24px',
    },
    title: {
        margin: 0,
        color: '#333',
    },
    navButton: {
        padding: '8px 16px',
        backgroundColor: '#1890ff',
        color: 'white',
        border: 'none',
        borderRadius: '4px',
        cursor: 'pointer',
    },
    table: {
        width: '100%',
        borderCollapse: 'collapse',
        backgroundColor: 'white',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
        borderRadius: '8px',
        overflow: 'hidden',
    },
    th: {
        padding: '12px 16px',
        backgroundColor: '#1890ff',
        color: 'white',
        textAlign: 'left',
    },
    td: {
        padding: '12px 16px',
        borderBottom: '1px solid #f0f0f0',
    },
    badge: {
        padding: '2px 8px',
        borderRadius: '4px',
        color: 'white',
        fontSize: '12px',
    },
    error: {
        color: 'red',
    },
};

export default Orders;
