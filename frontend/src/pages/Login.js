import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../axiosClient';
import { colors, button, form, text } from '../styles/common';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async () => {
    try {
      const response = await axiosClient.post('/api/users/login', {
        email,
        password,
      });
      localStorage.setItem('token', response.data.token);
      const payload = JSON.parse(atob(response.data.token.split('.')[1]));
      localStorage.setItem('userEmail', payload.sub);
      navigate('/users');
    } catch (err) {
      setError('Login failed, email or password is incorrect.');
      console.error(err.message);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.box}>
        <h2 style={styles.title}>Login</h2>

        <input
          style={form.input}
          type="email"
          placeholder="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          style={form.input}
          type="password"
          placeholder="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
        />
        {error && <p style={text.error}>{error}</p>}

        <button
          style={{ ...button.base, ...button.primary }}
          onClick={handleLogin}
        >
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
    backgroundColor: colors.bgPage,
  },
  box: {
    backgroundColor: colors.bgWhite,
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
    color: colors.textDark,
  },
};

export default Login;
