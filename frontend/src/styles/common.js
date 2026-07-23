export const colors = {
  primary: '#1890ff',
  success: '#52c41a',
  danger: '#ff4d4f',
  warning: '#faad14',
  textDark: '#333',
  textMuted: '#999',
  border: '#ddd',
  bgPage: '#f0f2f5',
  bgWhite: '#ffffff',
  bgSubtle: '#fafafa',
};

export const layout = {
  page: {
    padding: '24px',
    maxWidth: '1000px',
    margin: '0 auto',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '16px',
    flexWrap: 'wrap',
    gap: 8,
  },
  title: {
    margin: 0,
    color: colors.textDark,
  },
};

export const button = {
  base: {
    padding: '8px 14px',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: 14,
  },
  primary: { backgroundColor: colors.primary },
  success: { backgroundColor: colors.success },
  danger: { backgroundColor: colors.danger },
  small: {
    padding: '4px 10px',
    fontSize: 12,
    marginRight: 4,
  },
};

export const form = {
  card: {
    backgroundColor: colors.bgWhite,
    padding: '20px',
    borderRadius: '8px',
    marginBottom: '16px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  label: {
    display: 'block',
    marginBottom: 6,
    fontWeight: 'bold',
    fontSize: 13,
  },
  input: {
    width: '100%',
    padding: '8px',
    marginBottom: '12px',
    borderRadius: '4px',
    border: `1px solid ${colors.border}`,
    boxSizing: 'border-box',
    fontSize: 14,
  },
  textarea: {
    width: '100%',
    padding: '8px',
    marginBottom: '12px',
    borderRadius: '4px',
    border: `1px solid ${colors.border}`,
    boxSizing: 'border-box',
    fontFamily: 'inherit',
    fontSize: 14,
  },
};

export const table = {
  table: {
    width: '100%',
    borderCollapse: 'collapse',
    backgroundColor: colors.bgWhite,
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    borderRadius: '8px',
    overflow: 'hidden',
  },
  th: {
    padding: '12px 16px',
    backgroundColor: colors.primary,
    color: 'white',
    textAlign: 'left',
  },
  td: {
    padding: '10px 16px',
    borderBottom: '1px solid #f0f0f0',
  },
  badge: {
    padding: '2px 8px',
    borderRadius: '4px',
    color: 'white',
    fontSize: '12px',
  },
};

export const pagination = {
  wrapper: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 16,
  },
  pageBtn: {
    padding: '8px 16px',
    backgroundColor: colors.primary,
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
  },
};

export const text = {
  error: { color: colors.danger },
  placeholder: { color: colors.textMuted, textAlign: 'center', marginTop: 40 },
};

export const statusColor = (status) => {
  switch (status) {
    case 'COMPLETED':
      return colors.success;
    case 'PENDING':
      return colors.warning;
    case 'PROCESSING':
      return colors.primary;
    default:
      return colors.textMuted;
  }
};
