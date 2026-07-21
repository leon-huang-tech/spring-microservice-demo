import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function Users() {
    const [users, setUsers] = useState([]);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/login');
            return;
        }

        axios.get('http://localhost:8080/api/users', {
            headers: { Authorization: `Bearer ${token}` }
        })
            .then(res => setUsers(res.data))
            .catch((err) => {
                console.error('Users error:', err);
                setError('Failed to fetch users. Please try again.');
            });
    }, [navigate]);

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <h2 style={styles.title}>User List</h2>
                <button style={styles.navButton} onClick={() => navigate('/orders')}> View Orders </button>
                <button style={styles.navButton} onClick={() => navigate('/chat')}> AI Chat </button>
                <button style={styles.navButton} onClick={() => navigate('/knowledge')}> Knowledge Base </button>

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
                        <th style={styles.th}>Name</th>
                        <th style={styles.th}>Email</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(user => (
                        <tr key={user.id} style={styles.tr}>
                            <td style={styles.td}>{user.id}</td>
                            <td style={styles.td}>{user.name}</td>
                            <td style={styles.td}>{user.email}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div >
    );
};
const styles = {
    container: {
        padding: '40px',
        maxwidth: '800px',
        margin: '0 auto',
    },
    header: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '20px',
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
        boxShadow: '0 2px 10px rgba(0, 0, 0, 0.1)',
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
    tr: {
        transition: 'background-color 0.2s',
    },
    error: {
        color: 'red',
    },
};

export default Users;
