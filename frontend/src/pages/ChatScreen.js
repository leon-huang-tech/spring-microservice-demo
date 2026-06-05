import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function ChatScreen() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const sessionId = useRef('session_' + Date.now());
  const messagesEndRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
    }
  }, [navigate]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = async () => {
    if (!input.trim()) return;

    const userMessage = input;
    setInput('');
    setMessages(prev => [...prev, { role: 'user', content: userMessage }]);
    setLoading(true);

    const token = localStorage.getItem('token');
    const url = `http://localhost:8080/api/ai/chat/stream?message=${encodeURIComponent(userMessage)}&sessionId=${sessionId.current}`;

    try {
      const response = await fetch(url, {
        headers: { Authorization: `Bearer ${token}` }
      });

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let aiMessage = '';

      setMessages(prev => [...prev, { role: 'ai', content: '' }]);

      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        console.log('RAW CHUNK:', JSON.stringify(buffer));
        const lines = buffer.split('\n');

        // Keep the last line as it may be incomplete
        buffer = lines.pop() || '';

        for (const line of lines) {
          const trimmed = line.trim();
          if (trimmed.startsWith('data:')) {
            const text = trimmed.substring(5).trimStart();
            if (text) {
              aiMessage += text;
              setMessages(prev => {
                const updated = [...prev];
                updated[updated.length - 1] = { role: 'ai', content: aiMessage };
                return updated;
              });
            }
          }
        }
      }
    } catch (e) {
      setMessages(prev => [...prev,
      { role: 'ai', content: 'Error: Failed to get response.' }
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h2 style={styles.title}>AI Assistant</h2>
        <div>
          <button style={styles.navButton}
            onClick={() => navigate('/users')}>
            Users
          </button>
          <button style={{ ...styles.navButton, marginLeft: 8 }}
            onClick={() => navigate('/orders')}>
            Orders
          </button>

          <button style={{ ...styles.navButton, backgroundColor: '#ff4d4f', marginLeft: 8 }}
            onClick={() => {
              localStorage.removeItem('token');
              navigate('/login');
            }}>
            Logout
          </button>
        </div>
      </div>

      <div style={styles.chatBox}>
        {messages.length === 0 && (
          <p style={styles.placeholder}>
            Ask me about your orders or account...
          </p>
        )}
        {messages.map((msg, index) => (
          <div key={index} style={{
            ...styles.message,
            alignSelf: msg.role === 'user' ? 'flex-end' : 'flex-start',
            backgroundColor: msg.role === 'user' ? '#1890ff' : '#f0f0f0',
            color: msg.role === 'user' ? 'white' : 'black',
          }}>
            {msg.content}
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      <div style={styles.inputArea}>
        <input
          style={styles.input}
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && !loading && sendMessage()}
          placeholder="Type a message..."
          disabled={loading}
        />
        <button
          style={{
            ...styles.sendButton,
            opacity: loading ? 0.6 : 1
          }}
          onClick={sendMessage}
          disabled={loading}
        >
          {loading ? '...' : 'Send'}
        </button>
      </div>
    </div>
  );
}

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    height: '100vh',
    padding: '16px',
    maxWidth: '800px',
    margin: '0 auto',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '16px',
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
  chatBox: {
    flex: 1,
    overflowY: 'auto',
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    padding: '16px',
    backgroundColor: '#fafafa',
    borderRadius: '8px',
    marginBottom: '16px',
  },
  message: {
    padding: '10px 14px',
    borderRadius: '12px',
    maxWidth: '70%',
    wordBreak: 'break-word',
  },
  placeholder: {
    color: '#999',
    textAlign: 'center',
    marginTop: '40px',
  },
  inputArea: {
    display: 'flex',
    gap: '8px',
  },
  input: {
    flex: 1,
    padding: '10px',
    borderRadius: '4px',
    border: '1px solid #ddd',
    fontSize: '14px',
  },
  sendButton: {
    padding: '10px 20px',
    backgroundColor: '#1890ff',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
  },
};

export default ChatScreen;
