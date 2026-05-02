import { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async () => {
        try {
            const response = await axios.post(
                'http://localhost:8080/api/users/login', 
                {
                //method: 'POST',
                //headers: { 'Content-Type': 'application/json' },
                //body: JSON.stringify({ email, password }),
                email, password, }

            );
            localStorage.setItem('token', response.data.token);
            navigate('/users');
        } catch (err) {
            setError('Login failed, email or password is incorrect');
        }
    }

    return (
        <div style={styles.container}>
            <div style={styles.box}>
                <h2 style={styles.title}>Login</h2>

                <input
                    style={styles.input}
                    type="email"
                    placeholder="email"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                />
                <input
                    style={styles.input}
                    type="password"
                    placeholder="password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                />
                {error && <p style={styles.error}>{error}</p>}

                <button style={styles.button} onClick={handleLogin}>
                    Login
                </button>
            </div>
        </div>
    );
}
const styles = {
    container: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        backgroundColor: '#f0f2f5',
    },
    box: {
        backgroundColor: 'white',
        padding: '40px',
        borderRadius: '8px',
        boxShadow: '0 2px 10px rgba(0, 0, 0, 0.1)',
        width: '320px',
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
    },
    title: {
        textAlign: 'center',
        margin: 0,
        color: '#333',
    },
    input: {
        padding: '10px',
        borderRadius: '4px',
        border: '1px solid #ddd',
        fontSize: '14px',
    },
    button: {
        padding: '10px',
        backgroundColor: '#1890ff',
        color: 'white',
        border: 'none',
        borderRadius: '4px',
        fontSize: '14px',
        cursor: 'pointer',
    },
    error: {
        color: 'red',
        fontSize: '13px',
        margin: 0,
    },
};
export default Login;