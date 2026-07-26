import { NavLink, useNavigate } from 'react-router-dom';

const NAV_LINKS = [
  { to: '/users', label: 'Users' },
  { to: '/orders', label: 'Orders' },
  { to: '/chat', label: 'AI Chat' },
  { to: '/knowledge', label: 'Knowledge Base' },
];

function Navbar() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('userEmail');
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <nav style={styles.nav}>
      <div style={styles.left}>
        {NAV_LINKS.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            style={({ isActive }) => ({
              ...styles.link,
              ...(isActive ? styles.activeLink : {}),
            })}
          >
            {link.label}
          </NavLink>
        ))}
      </div>
      <div style={styles.right}>
        <span style={styles.loginUserEmail}> &nbsp;{localStorage.getItem('userEmail')} </span>
        <button style={styles.logoutBtn} onClick={handleLogout}>
          Logout
        </button>
      </div>
    </nav>
  );
}

const styles = {
  nav: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '12px 24px',
    backgroundColor: 'white',
    boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
    marginBottom: '20px',
    flexWrap: 'wrap',
    gap: 8,
  },
  left: { display: 'flex', gap: 4, flexWrap: 'wrap' },
  right: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
  },
  link: {
    padding: '8px 14px',
    borderRadius: '4px',
    color: '#555',
    textDecoration: 'none',
    fontSize: 14,
    fontWeight: 500,
  },
  activeLink: {
    backgroundColor: '#1890ff',
    color: 'white',
  },
  logoutBtn: {
    padding: '8px 14px',
    backgroundColor: '#ff4d4f',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: 14,
  },
  loginUserEmail: {
  display: 'inline-flex',
  alignItems: 'center',
  padding: '4px 12px',
  fontSize: '13px',
  fontWeight: 500,
  color: '#e6f7ff',
  backgroundColor: '#111d2c',
  border: '1px solid #15395b',
  borderRadius: '16px',
  lineHeight: '1.4',
},
};

export default Navbar;
